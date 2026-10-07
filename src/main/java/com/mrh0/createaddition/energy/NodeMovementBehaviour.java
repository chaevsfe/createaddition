package com.mrh0.createaddition.energy;

import com.mrh0.createaddition.energy.network.WireGraph;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NodeMovementBehaviour extends MovementBehaviour {

	@Override
	public void startMoving(MovementContext context) {
		if (context.blockEntityData == null) return;
		context.blockEntityData.putBoolean("contraption", true);
		if (context.world instanceof ServerLevel level && context.contraption != null && context.contraption.anchor != null)
			forgetPaidWires(level, context.localPos.offset(context.contraption.anchor), context.blockEntityData);
	}

	private static void forgetPaidWires(ServerLevel level, BlockPos pos, CompoundTag data) {
		IntSet paid = new IntOpenHashSet();
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof IWireNode node && !be.isRemoved()) {
			for (int i = 0; i < node.getNodeCount(); i++) {
				LocalNode local = node.getLocalNode(i);
				if (local != null && !local.isInvalid() && node.getWireNode(i) == null) paid.add(i);
			}
		} else {
			paid.addAll(WireGraph.takeWiresPaidAtRemoval(level, pos));
		}
		if (paid.isEmpty()) return;
		ListTag kept = new ListTag();
		for (Tag tag : data.getListOrEmpty(LocalNode.NODES))
			if (!(tag instanceof CompoundTag node) || !paid.contains(node.getIntOr(LocalNode.ID, 0))) kept.add(tag);
		data.put(LocalNode.NODES, kept);
	}
}
