package com.mrh0.createaddition.energy.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.energy.LocalNode;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import org.jetbrains.annotations.Nullable;

final class WireGraphVerifier {
	private static final int MAX_READS_IN_FLIGHT = 16;

	private final WireGraph graph;
	private final ServerLevel level;
	private LongOpenHashSet queued = new LongOpenHashSet();
	private final Long2ObjectOpenHashMap<LongOpenHashSet> waitingForDisk = new Long2ObjectOpenHashMap<>();
	private final LongArrayFIFOQueue readQueue = new LongArrayFIFOQueue();
	private final LongOpenHashSet readsInFlight = new LongOpenHashSet();
	private final LongOpenHashSet unresolved = new LongOpenHashSet();
	@Nullable
	private Codec<PalettedContainer<BlockState>> blockStateCodec;

	WireGraphVerifier(WireGraph graph, ServerLevel level) {
		this.graph = graph;
		this.level = level;
	}

	void request(long pos) {
		if (unresolved.contains(pos) || graph.isVerified(pos)) return;
		if (graph.isLoaded(pos)) queued.add(pos);
		else readFromDisk(pos);
	}

	void tick() {
		if (!queued.isEmpty()) {
			LongOpenHashSet now = queued;
			queued = new LongOpenHashSet();
			LongIterator it = now.iterator();
			while (it.hasNext()) checkLoaded(it.nextLong());
		}
		startReads();
	}

	int pendingChunkReads() {
		return waitingForDisk.size();
	}

	void reset() {
		queued.clear();
		waitingForDisk.clear();
		readQueue.clear();
		unresolved.clear();
	}

	private void checkLoaded(long pos) {
		if (graph.isVerified(pos)) return;
		var node = graph.loadedNode(pos);
		if (node != null) graph.sync(node);
		else if (graph.isLoaded(pos)) graph.markMissing(pos);
		else readFromDisk(pos);
	}

	private void readFromDisk(long pos) {
		long chunk = ChunkPos.pack(SectionPos.blockToSectionCoord(BlockPos.getX(pos)), SectionPos.blockToSectionCoord(BlockPos.getZ(pos)));
		LongOpenHashSet positions = waitingForDisk.get(chunk);
		if (positions == null) {
			positions = new LongOpenHashSet();
			waitingForDisk.put(chunk, positions);
			if (!readsInFlight.contains(chunk)) readQueue.enqueue(chunk);
		}
		positions.add(pos);
	}

	private void startReads() {
		while (readsInFlight.size() < MAX_READS_IN_FLIGHT && !readQueue.isEmpty()) {
			long chunk = readQueue.dequeueLong();
			if (!waitingForDisk.containsKey(chunk) || !readsInFlight.add(chunk)) continue;
			level.getChunkSource().chunkMap.read(ChunkPos.unpack(chunk))
					.whenCompleteAsync((tag, error) -> onRead(chunk, tag, error), level.getServer());
		}
	}

	private void onRead(long chunk, @Nullable Optional<CompoundTag> tag, @Nullable Throwable error) {
		readsInFlight.remove(chunk);
		LongOpenHashSet positions = waitingForDisk.remove(chunk);
		if (graph.isClosed() || positions == null) return;
		LongIterator it = positions.iterator();
		if (level.getChunkSource().getChunkNow(ChunkPos.getX(chunk), ChunkPos.getZ(chunk)) != null) {
			while (it.hasNext()) request(it.nextLong());
		} else if (error != null || tag == null || tag.isEmpty()) {
			if (error != null) CreateAddition.LOGGER.warn("Could not read chunk {} to check wire nodes", ChunkPos.unpack(chunk), error);
			unresolved.addAll(positions);
		} else {
			SavedChunk saved = new SavedChunk(tag.get());
			while (it.hasNext()) resolve(it.nextLong(), saved);
		}
		startReads();
	}

	private void resolve(long pos, SavedChunk saved) {
		if (graph.isVerified(pos)) return;
		if (!saved.isReadable()) {
			unresolved.add(pos);
			return;
		}
		BlockPos blockPos = BlockPos.of(pos);
		CompoundTag blockEntity = saved.blockEntity(blockPos);
		WireNodeKind kind = blockEntity == null ? null : WireNodeKind.fromBlockEntityId(blockEntity.getStringOr("id", ""));
		if (kind == null) {
			if (blockEntity != null && blockEntity.contains(LocalNode.NODES)) unresolved.add(pos);
			else graph.markMissing(pos);
			return;
		}
		if (blockEntity.contains("contraption")) {
			unresolved.add(pos);
			return;
		}
		WireVertex vertex = WireVertex.fromBlockEntityTag(blockPos, kind, blockEntity);
		vertex.powered = kind.isPowered(saved.blockState(blockPos));
		graph.markPresent(vertex);
	}

	private Codec<PalettedContainer<BlockState>> blockStateCodec() {
		if (blockStateCodec == null)
			blockStateCodec = PalettedContainerFactory.create(level.registryAccess()).blockStatesContainerCodec();
		return blockStateCodec;
	}

	private final class SavedChunk {
		private final CompoundTag tag;
		@Nullable
		private Map<BlockPos, CompoundTag> blockEntities;

		SavedChunk(CompoundTag tag) {
			this.tag = tag;
		}

		boolean isReadable() {
			return tag.getCompound("Level").isEmpty();
		}

		@Nullable
		CompoundTag blockEntity(BlockPos pos) {
			if (blockEntities == null) {
				blockEntities = new HashMap<>();
				tag.getListOrEmpty("block_entities").compoundStream().forEach(blockEntity -> blockEntities.put(
						new BlockPos(blockEntity.getIntOr("x", 0), blockEntity.getIntOr("y", 0), blockEntity.getIntOr("z", 0)), blockEntity));
			}
			return blockEntities.get(pos);
		}

		@Nullable
		BlockState blockState(BlockPos pos) {
			int sectionY = SectionPos.blockToSectionCoord(pos.getY());
			return tag.getListOrEmpty("sections").compoundStream()
					.filter(section -> section.getByteOr("Y", Byte.MIN_VALUE) == sectionY && section.getCompound("block_states").isPresent())
					.findFirst()
					.flatMap(section -> blockStateCodec().parse(NbtOps.INSTANCE, section.getCompoundOrEmpty("block_states")).result())
					.map(states -> states.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15))
					.orElse(null);
		}
	}
}
