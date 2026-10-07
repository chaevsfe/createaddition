package com.mrh0.createaddition.energy.network;

import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.energy.LocalNode;
import com.mrh0.createaddition.energy.WireType;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

final class WireVertex {
	final long pos;
	final WireNodeKind kind;
	final Int2ObjectMap<WireSlot> slots = new Int2ObjectArrayMap<>();
	boolean powered;

	boolean verified;
	long demand;
	long throughput;

	WireVertex(long pos, WireNodeKind kind) {
		this.pos = pos;
		this.kind = kind;
	}

	static WireVertex of(IWireNode node, @Nullable BlockState state) {
		WireVertex vertex = new WireVertex(node.getPos().asLong(), node.getWireNodeKind());
		for (int i = 0; i < node.getNodeCount(); i++) {
			LocalNode local = node.getLocalNode(i);
			if (local == null || local.getType() == null) continue;
			vertex.slots.put(i, new WireSlot(i, local.getPos().asLong(), local.getOtherIndex(), local.getType()));
		}
		vertex.powered = vertex.kind.isPowered(state);
		return vertex;
	}

	static WireVertex fromBlockEntityTag(BlockPos pos, WireNodeKind kind, CompoundTag tag) {
		WireVertex vertex = new WireVertex(pos.asLong(), kind);
		tag.getListOrEmpty(LocalNode.NODES).compoundStream().forEach(node -> {
			WireType type = WireType.fromIndex(node.getIntOr(LocalNode.TYPE, 0));
			if (type == null) return;
			int index = node.getIntOr(LocalNode.ID, 0);
			BlockPos other = pos.offset(node.getIntOr(LocalNode.X, 0), node.getIntOr(LocalNode.Y, 0), node.getIntOr(LocalNode.Z, 0));
			vertex.slots.put(index, new WireSlot(index, other.asLong(), node.getIntOr(LocalNode.OTHER, 0), type));
		});
		return vertex;
	}

	boolean pointsTo(int index, long otherPos, int otherIndex) {
		WireSlot slot = slots.get(index);
		return slot != null && slot.otherPos() == otherPos && slot.otherIndex() == otherIndex;
	}

	boolean sameWiring(WireVertex other) {
		return kind == other.kind && slots.equals(other.slots);
	}

	PortKey port(int port) {
		return new PortKey(pos, port);
	}

	CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putLong("Pos", pos);
		tag.putString("Kind", kind.getSerializedName());
		if (kind.isBridge()) tag.putBoolean("Powered", powered);
		ListTag list = new ListTag();
		for (WireSlot slot : slots.values()) {
			CompoundTag slotTag = new CompoundTag();
			slotTag.putInt("Index", slot.index());
			slotTag.putLong("Other", slot.otherPos());
			slotTag.putInt("OtherIndex", slot.otherIndex());
			slotTag.putInt("Type", slot.type().getIndex());
			list.add(slotTag);
		}
		tag.put("Slots", list);
		return tag;
	}

	@Nullable
	static WireVertex read(CompoundTag tag) {
		WireNodeKind kind = WireNodeKind.byName(tag.getStringOr("Kind", ""));
		if (kind == null || tag.getLong("Pos").isEmpty()) return null;
		WireVertex vertex = new WireVertex(tag.getLongOr("Pos", 0L), kind);
		vertex.powered = tag.getBooleanOr("Powered", false);
		tag.getListOrEmpty("Slots").compoundStream().forEach(slotTag -> {
			WireType type = WireType.fromIndex(slotTag.getIntOr("Type", -1));
			if (type == null || slotTag.getLong("Other").isEmpty()) return;
			int index = slotTag.getIntOr("Index", 0);
			vertex.slots.put(index, new WireSlot(index, slotTag.getLongOr("Other", 0L), slotTag.getIntOr("OtherIndex", 0), type));
		});
		return vertex;
	}
}
