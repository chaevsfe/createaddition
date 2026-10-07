package com.mrh0.createaddition.energy.network;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.mojang.serialization.Codec;
import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.energy.LocalNode;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jetbrains.annotations.Nullable;

public class WireGraph extends SavedData {
	private static final int DATA_VERSION = 1;
	private static final Codec<WireGraph> CODEC = CompoundTag.CODEC.xmap(WireGraph::load, WireGraph::save);
	private static final SavedDataType<WireGraph> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(CreateAddition.MODID, "wires"), WireGraph::new, CODEC, null);
	private static final Map<ServerLevel, WireGraph> GRAPHS = new WeakHashMap<>();
	private static final Map<ServerLevel, LongOpenHashSet> LOADED_CHUNKS = new WeakHashMap<>();

	private ServerLevel level;
	private WireGraphVerifier verifier;

	private final Long2ObjectOpenHashMap<WireVertex> vertices = new Long2ObjectOpenHashMap<>();
	private final Long2ObjectOpenHashMap<LongOpenHashSet> inbound = new Long2ObjectOpenHashMap<>();
	private final Long2ObjectOpenHashMap<LongOpenHashSet> watched = new Long2ObjectOpenHashMap<>();
	private final Long2ObjectOpenHashMap<IWireNode> loadedNodes = new Long2ObjectOpenHashMap<>();
	private final LongOpenHashSet bridges = new LongOpenHashSet();
	private final LongOpenHashSet missing = new LongOpenHashSet();

	private final Map<PortKey, EnergyNetwork> networkByPort = new HashMap<>();
	private final Set<EnergyNetwork> networks = new LinkedHashSet<>();
	private final Set<PortKey> dirtyPorts = new HashSet<>();
	private final Set<WireEnd> danglingCandidates = new LinkedHashSet<>();

	private boolean closed;

	private record WireEnd(long pos, WireSlot slot) {}

	private WireGraph() {}

	private void bind(ServerLevel level) {
		this.level = level;
		this.verifier = new WireGraphVerifier(this, level);
		for (WireVertex vertex : new ArrayList<>(vertices.values()))
			for (WireSlot slot : vertex.slots.values())
				if (!vertices.containsKey(slot.otherPos())) verifier.request(slot.otherPos());
	}

	@Nullable
	public static WireGraph get(@Nullable Level level) {
		return lookup(level, true);
	}

	@Nullable
	private static WireGraph lookup(@Nullable Level level, boolean create) {
		if (!(level instanceof ServerLevel serverLevel)) return null;
		WireGraph graph = GRAPHS.get(serverLevel);
		if (graph != null) return graph;
		if (serverLevel.getServer().getLevel(serverLevel.dimension()) != serverLevel) return null;
		SavedDataStorage storage = serverLevel.getDataStorage();
		graph = create ? storage.computeIfAbsent(TYPE) : storage.get(TYPE);
		if (graph == null) return null;
		GRAPHS.put(serverLevel, graph);
		graph.bind(serverLevel);
		return graph;
	}

	public static void tick(Level level) {
		WireGraph graph = lookup(level, false);
		if (graph != null) graph.tick();
		else if (level instanceof ServerLevel serverLevel) LOADED_CHUNKS.remove(serverLevel);
	}

	public static void nodeLoaded(@Nullable Level level, IWireNode node) {
		WireGraph graph = get(level);
		if (graph != null) graph.onNodeLoaded(node);
	}

	public static void nodeUnloaded(@Nullable Level level, IWireNode node) {
		if (!(level instanceof ServerLevel serverLevel)) return;
		WireGraph graph = GRAPHS.get(serverLevel);
		if (graph != null) graph.forgetLoaded(node);
	}

	public static void nodeChanged(@Nullable Level level, IWireNode node) {
		WireGraph graph = get(level);
		if (graph != null) graph.sync(node);
	}

	public static void nodeRemoved(@Nullable Level level, IWireNode node) {
		WireGraph graph = lookup(level, false);
		if (graph == null) return;
		graph.forgetLoaded(node);
		graph.markMissing(node.getPos().asLong());
	}

	public static void chunkLoaded(ServerLevel level, LevelChunk chunk) {
		LOADED_CHUNKS.computeIfAbsent(level, l -> new LongOpenHashSet()).add(chunk.getPos().pack());
	}

	public static void levelUnloaded(ServerLevel level) {
		LOADED_CHUNKS.remove(level);
		WireGraph graph = GRAPHS.remove(level);
		if (graph != null) graph.closed = true;
	}

	@Nullable
	public static EnergyNetwork getNetwork(@Nullable Level level, IWireNode node, int port) {
		WireGraph graph = get(level);
		return graph == null ? null : graph.networkOf(node, port);
	}

	public static long getBridgeThroughput(@Nullable Level level, BlockPos pos) {
		WireVertex bridge = getVertex(level, pos);
		return bridge == null ? 0 : bridge.throughput;
	}

	public static long getBridgeDemand(@Nullable Level level, BlockPos pos) {
		WireVertex bridge = getVertex(level, pos);
		return bridge == null ? 0 : bridge.demand;
	}

	@Nullable
	private static WireVertex getVertex(@Nullable Level level, BlockPos pos) {
		WireGraph graph = lookup(level, false);
		return graph == null ? null : graph.vertices.get(pos.asLong());
	}

	@Nullable
	public static IWireNode findLoadedNode(ServerLevel level, BlockPos pos) {
		return loadedNodeInChunk(level, pos);
	}

	private void onNodeLoaded(IWireNode node) {
		loadedNodes.put(node.getPos().asLong(), node);
		sync(node);
	}

	private void forgetLoaded(IWireNode node) {
		long pos = node.getPos().asLong();
		if (loadedNodes.get(pos) == node) loadedNodes.remove(pos);
	}

	void sync(IWireNode node) {
		BlockState state = node instanceof BlockEntity be ? be.getBlockState() : null;
		markPresent(WireVertex.of(node, state));
	}

	void markPresent(WireVertex fresh) {
		fresh.verified = true;
		missing.remove(fresh.pos);
		WireVertex old = vertices.get(fresh.pos);
		if (old != null && old.sameWiring(fresh)) {
			old.verified = true;
			if (old.powered != fresh.powered) {
				old.powered = fresh.powered;
				setDirty();
			}
		} else {
			replaceVertex(fresh.pos, old, fresh);
		}
		recheckInbound(fresh.pos);
	}

	void markMissing(long pos) {
		missing.add(pos);
		WireVertex old = vertices.get(pos);
		if (old != null) replaceVertex(pos, old, null);
		recheckInbound(pos);
	}

	private void drainLoadedChunks() {
		LongOpenHashSet chunks = LOADED_CHUNKS.remove(level);
		if (chunks == null) return;
		LongIterator it = chunks.iterator();
		while (it.hasNext()) {
			long chunk = it.nextLong();
			LevelChunk loaded = level.getChunkSource().getChunkNow(ChunkPos.getX(chunk), ChunkPos.getZ(chunk));
			if (loaded != null) onChunkLoaded(loaded);
		}
	}

	private void onChunkLoaded(LevelChunk chunk) {
		LongOpenHashSet expected = watched.get(chunk.getPos().pack());
		if (expected == null) return;
		Map<BlockPos, BlockEntity> blockEntities = chunk.getBlockEntities();
		for (long pos : expected.toLongArray()) {
			BlockPos blockPos = BlockPos.of(pos);
			BlockEntity be = blockEntities.get(blockPos);
			if (be instanceof IWireNode && !be.isRemoved()) continue;
			CompoundTag pending = chunk.getBlockEntityNbt(blockPos);
			if (pending != null && WireNodeKind.fromBlockEntityId(pending.getStringOr("id", "")) != null) continue;
			markMissing(pos);
		}
	}

	private void replaceVertex(long pos, @Nullable WireVertex old, @Nullable WireVertex fresh) {
		List<WireSlot> cut = new ArrayList<>();
		if (old != null) {
			for (WireSlot slot : old.slots.values()) {
				removeInbound(slot.otherPos(), pos);
				markPartnerDirty(slot);
				if (fresh == null || !slot.equals(fresh.slots.get(slot.index()))) cut.add(slot);
			}
			markPortsDirty(old);
			vertices.remove(pos);
			bridges.remove(pos);
		}
		if (fresh != null) {
			if (old != null) {
				fresh.demand = old.demand;
				fresh.throughput = old.throughput;
			}
			vertices.put(pos, fresh);
			if (fresh.kind.isBridge()) bridges.add(pos);
			for (WireSlot slot : fresh.slots.values()) {
				addInbound(slot.otherPos(), pos);
				markPartnerDirty(slot);
			}
			markPortsDirty(fresh);
			for (WireSlot slot : fresh.slots.values()) checkWireEnd(fresh, slot);
		}
		for (WireSlot slot : cut) {
			WireVertex partner = vertices.get(slot.otherPos());
			if (partner == null || !partner.pointsTo(slot.otherIndex(), pos, slot.index())) continue;
			checkWireEnd(partner, partner.slots.get(slot.otherIndex()));
		}
		updateWatch(pos);
		setDirty();
	}

	private void checkWireEnd(WireVertex vertex, WireSlot slot) {
		WireVertex partner = vertices.get(slot.otherPos());
		if (partner != null && partner.pointsTo(slot.otherIndex(), vertex.pos, slot.index())) return;
		if (isDangling(vertex, slot)) danglingCandidates.add(new WireEnd(vertex.pos, slot));
		else verifier.request(slot.otherPos());
	}

	private boolean isDangling(WireVertex vertex, WireSlot slot) {
		WireVertex partner = vertices.get(slot.otherPos());
		if (partner == null) return missing.contains(slot.otherPos());
		return partner.verified && !partner.pointsTo(slot.otherIndex(), vertex.pos, slot.index());
	}

	private void recheckInbound(long pos) {
		LongOpenHashSet sources = inbound.get(pos);
		if (sources == null) return;
		for (long source : sources.toLongArray()) {
			WireVertex vertex = vertices.get(source);
			if (vertex == null) continue;
			for (WireSlot slot : vertex.slots.values())
				if (slot.otherPos() == pos) checkWireEnd(vertex, slot);
		}
	}

	private void pruneDanglingWires() {
		if (danglingCandidates.isEmpty()) return;
		List<WireEnd> candidates = new ArrayList<>(danglingCandidates);
		danglingCandidates.clear();
		for (WireEnd end : candidates) {
			WireVertex vertex = vertices.get(end.pos());
			WireSlot slot = end.slot();
			if (vertex == null || !slot.equals(vertex.slots.get(slot.index())) || !isDangling(vertex, slot)) continue;
			IWireNode node = loadedNode(end.pos());
			if (node != null) {
				LocalNode local = node.getLocalNode(slot.index());
				if (local != null && local.getPos().asLong() == slot.otherPos() && local.getOtherIndex() == slot.otherIndex())
					node.removeNode(slot.index());
				else
					sync(node);
				continue;
			}
			vertex.slots.remove(slot.index());
			vertex.verified = false;
			removeInbound(slot.otherPos(), vertex.pos);
			dirtyPorts.add(vertex.port(vertex.kind.portOf(slot.index())));
			setDirty();
		}
	}

	private EnergyNetwork networkOf(IWireNode node, int port) {
		long pos = node.getPos().asLong();
		if (!vertices.containsKey(pos)) onNodeLoaded(node);
		PortKey key = new PortKey(pos, port);
		EnergyNetwork network = networkByPort.get(key);
		if (network == null || !network.isValid() || dirtyPorts.contains(key)) {
			rebuildNetworks();
			network = networkByPort.get(key);
		}
		if (network == null) {
			network = new EnergyNetwork();
			network.invalidate();
		}
		return network;
	}

	private void rebuildNetworks() {
		if (dirtyPorts.isEmpty()) return;
		Set<PortKey> seeds = new HashSet<>(dirtyPorts);
		dirtyPorts.clear();
		Set<EnergyNetwork> previous = Collections.newSetFromMap(new IdentityHashMap<>());
		for (PortKey port : seeds) {
			EnergyNetwork network = networkByPort.get(port);
			if (network != null) previous.add(network);
		}
		for (EnergyNetwork network : previous) seeds.addAll(network.members);

		Set<PortKey> visited = new HashSet<>();
		List<Set<PortKey>> components = new ArrayList<>();
		for (PortKey seed : seeds)
			if (!visited.contains(seed) && exists(seed)) components.add(walk(seed, visited));
		for (Set<PortKey> component : components)
			for (PortKey port : component) {
				EnergyNetwork network = networkByPort.get(port);
				if (network != null) previous.add(network);
			}
		for (PortKey port : seeds)
			if (!visited.contains(port)) networkByPort.remove(port);

		Set<EnergyNetwork> unchanged = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Set<PortKey> component : components) {
			EnergyNetwork current = networkByPort.get(component.iterator().next());
			if (current != null && current.members.equals(component)) {
				unchanged.add(current);
				continue;
			}
			EnergyNetwork network = new EnergyNetwork();
			network.members = component;
			for (PortKey port : component) networkByPort.put(port, network);
			networks.add(network);
		}
		for (EnergyNetwork old : previous) {
			if (unchanged.contains(old)) continue;
			if (old.hasStoredEnergy()) {
				EnergyNetwork heir = largestShare(old.members);
				if (heir != null && heir != old) heir.absorb(old);
			}
			old.invalidate();
			networks.remove(old);
		}
	}

	private boolean exists(PortKey port) {
		WireVertex vertex = vertices.get(port.pos());
		return vertex != null && port.port() >= 0 && port.port() < vertex.kind.portCount();
	}

	private Set<PortKey> walk(PortKey start, Set<PortKey> visited) {
		Set<PortKey> component = new HashSet<>();
		ArrayDeque<PortKey> queue = new ArrayDeque<>();
		visited.add(start);
		queue.add(start);
		while (!queue.isEmpty()) {
			PortKey port = queue.poll();
			component.add(port);
			WireVertex vertex = vertices.get(port.pos());
			for (WireSlot slot : vertex.slots.values()) {
				if (vertex.kind.portOf(slot.index()) != port.port()) continue;
				WireVertex other = vertices.get(slot.otherPos());
				if (other == null || !other.pointsTo(slot.otherIndex(), vertex.pos, slot.index())) continue;
				PortKey next = other.port(other.kind.portOf(slot.otherIndex()));
				if (visited.add(next)) queue.add(next);
			}
		}
		return component;
	}

	@Nullable
	private EnergyNetwork largestShare(Set<PortKey> ports) {
		Map<EnergyNetwork, Integer> shares = new IdentityHashMap<>();
		EnergyNetwork best = null;
		int bestShare = 0;
		for (PortKey port : ports) {
			EnergyNetwork network = networkByPort.get(port);
			if (network == null) continue;
			int share = shares.merge(network, 1, Integer::sum);
			if (share > bestShare) {
				best = network;
				bestShare = share;
			}
		}
		return best;
	}

	private void tick() {
		if (closed || !level.tickRateManager().runsNormally()) return;
		drainLoadedChunks();
		verifier.tick();
		pruneDanglingWires();
		rebuildNetworks();
		int id = 0;
		boolean stored = false;
		for (EnergyNetwork network : networks) {
			network.tick(id++);
			stored |= network.hasStoredEnergy();
		}
		tickBridges();
		if (stored) setDirty();
	}

	private void tickBridges() {
		LongIterator it = bridges.iterator();
		while (it.hasNext()) {
			WireVertex bridge = vertices.get(it.nextLong());
			if (bridge == null) continue;
			bridge.throughput = 0;
			if (!bridge.powered) continue;
			EnergyNetwork in = networkByPort.get(bridge.port(0));
			EnergyNetwork out = networkByPort.get(bridge.port(1));
			if (in == null || out == null) continue;
			bridge.throughput = out.push(in.pull(bridge.demand));
			bridge.demand = in.demand(out.getDemand());
		}
	}

	boolean isClosed() {
		return closed;
	}

	boolean isVerified(long pos) {
		WireVertex vertex = vertices.get(pos);
		return (vertex != null && vertex.verified) || missing.contains(pos);
	}

	boolean isLoaded(long pos) {
		return loadedNodes.containsKey(pos) || level.getChunkSource().getChunkNow(
				SectionPos.blockToSectionCoord(BlockPos.getX(pos)), SectionPos.blockToSectionCoord(BlockPos.getZ(pos))) != null;
	}

	@Nullable
	IWireNode loadedNode(long pos) {
		IWireNode node = loadedNodes.get(pos);
		if (node instanceof BlockEntity be && !be.isRemoved()) return node;
		return loadedNodeInChunk(level, BlockPos.of(pos));
	}

	@Nullable
	private static IWireNode loadedNodeInChunk(ServerLevel level, BlockPos pos) {
		LevelChunk chunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
		if (chunk == null) return null;
		BlockEntity be = chunk.getBlockEntity(pos);
		return be instanceof IWireNode node && !be.isRemoved() ? node : null;
	}

	private void markPortsDirty(WireVertex vertex) {
		for (int port = 0; port < vertex.kind.portCount(); port++)
			dirtyPorts.add(vertex.port(port));
	}

	private void markPartnerDirty(WireSlot slot) {
		WireVertex partner = vertices.get(slot.otherPos());
		if (partner != null) dirtyPorts.add(partner.port(partner.kind.portOf(slot.otherIndex())));
	}

	private void addInbound(long target, long source) {
		positionsAt(inbound, target).add(source);
		updateWatch(target);
	}

	private void removeInbound(long target, long source) {
		LongOpenHashSet sources = inbound.get(target);
		if (sources == null) return;
		sources.remove(source);
		if (sources.isEmpty()) inbound.remove(target);
		updateWatch(target);
	}

	private void updateWatch(long pos) {
		long chunk = ChunkPos.pack(SectionPos.blockToSectionCoord(BlockPos.getX(pos)), SectionPos.blockToSectionCoord(BlockPos.getZ(pos)));
		if (vertices.containsKey(pos) || inbound.containsKey(pos)) {
			positionsAt(watched, chunk).add(pos);
			return;
		}
		LongOpenHashSet positions = watched.get(chunk);
		if (positions != null && positions.remove(pos) && positions.isEmpty()) watched.remove(chunk);
	}

	private static LongOpenHashSet positionsAt(Long2ObjectOpenHashMap<LongOpenHashSet> map, long key) {
		LongOpenHashSet positions = map.get(key);
		if (positions == null) {
			positions = new LongOpenHashSet();
			map.put(key, positions);
		}
		return positions;
	}

	public Component describe() {
		int verified = 0;
		for (WireVertex vertex : vertices.values())
			if (vertex.verified) verified++;
		return Component.literal(vertices.size() + " wire nodes (" + loadedNodes.size() + " loaded, " + verified
				+ " checked against the world), " + countWires() + " wires, " + networks.size() + " networks, "
				+ verifier.pendingChunkReads() + " chunks waiting to be read from disk");
	}

	public Component describeProblems() {
		return Component.literal(countStaleNodes() + " stale wire nodes, " + countOneSidedWires() + " one-sided wires");
	}

	private int countWires() {
		int ends = 0;
		for (WireVertex vertex : vertices.values())
			for (WireSlot slot : vertex.slots.values()) {
				WireVertex other = vertices.get(slot.otherPos());
				if (other != null && other.pointsTo(slot.otherIndex(), vertex.pos, slot.index())) ends++;
			}
		return ends / 2;
	}

	private int countOneSidedWires() {
		int count = 0;
		for (WireVertex vertex : vertices.values())
			for (WireSlot slot : vertex.slots.values()) {
				WireVertex other = vertices.get(slot.otherPos());
				if (other == null || !other.pointsTo(slot.otherIndex(), vertex.pos, slot.index())) count++;
			}
		return count;
	}

	private int countStaleNodes() {
		int count = 0;
		for (WireVertex vertex : vertices.values()) {
			if (!isLoaded(vertex.pos)) continue;
			IWireNode node = loadedNode(vertex.pos);
			if (node == null || !vertex.sameWiring(WireVertex.of(node, null))) count++;
		}
		return count;
	}

	public int verifyAll() {
		int count = 0;
		for (WireVertex vertex : vertices.values()) {
			if (vertex.verified) continue;
			verifier.request(vertex.pos);
			count++;
		}
		return count;
	}

	public void rebuild() {
		ListTag buffers = writeBuffers();
		List<IWireNode> loaded = new ArrayList<>(loadedNodes.values());
		for (EnergyNetwork network : networks) network.invalidate();
		vertices.clear();
		inbound.clear();
		watched.clear();
		loadedNodes.clear();
		bridges.clear();
		missing.clear();
		networkByPort.clear();
		networks.clear();
		dirtyPorts.clear();
		danglingCandidates.clear();
		verifier.reset();
		for (IWireNode node : loaded)
			if (node instanceof BlockEntity be && !be.isRemoved()) onNodeLoaded(node);
		rebuildNetworks();
		restoreBuffers(buffers);
		setDirty();
	}

	private CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Version", DATA_VERSION);
		ListTag list = new ListTag();
		for (WireVertex vertex : vertices.values()) list.add(vertex.write());
		tag.put("Vertices", list);
		tag.put("Buffers", writeBuffers());
		return tag;
	}

	private static WireGraph load(CompoundTag tag) {
		WireGraph graph = new WireGraph();
		tag.getListOrEmpty("Vertices").compoundStream().forEach(t -> {
			WireVertex vertex = WireVertex.read(t);
			if (vertex != null) graph.addSavedVertex(vertex);
		});
		graph.rebuildNetworks();
		graph.restoreBuffers(tag.getListOrEmpty("Buffers"));
		return graph;
	}

	private void addSavedVertex(WireVertex vertex) {
		vertices.put(vertex.pos, vertex);
		if (vertex.kind.isBridge()) bridges.add(vertex.pos);
		for (WireSlot slot : vertex.slots.values()) addInbound(slot.otherPos(), vertex.pos);
		updateWatch(vertex.pos);
		markPortsDirty(vertex);
	}

	private ListTag writeBuffers() {
		ListTag buffers = new ListTag();
		for (EnergyNetwork network : networks) {
			if (!network.hasStoredEnergy() || network.members.isEmpty()) continue;
			PortKey anchor = network.members.iterator().next();
			CompoundTag buffer = new CompoundTag();
			buffer.putLong("Pos", anchor.pos());
			buffer.putInt("Port", anchor.port());
			buffer.putLong("In", network.getStoredIn());
			buffer.putLong("Out", network.getStoredOut());
			buffers.add(buffer);
		}
		return buffers;
	}

	private void restoreBuffers(ListTag buffers) {
		buffers.compoundStream().forEach(buffer -> {
			EnergyNetwork network = networkByPort.get(new PortKey(buffer.getLongOr("Pos", 0L), buffer.getIntOr("Port", 0)));
			if (network != null) network.restore(buffer.getLongOr("In", 0L), buffer.getLongOr("Out", 0L));
		});
	}
}
