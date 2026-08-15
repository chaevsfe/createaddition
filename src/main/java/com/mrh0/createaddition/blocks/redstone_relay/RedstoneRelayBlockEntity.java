package com.mrh0.createaddition.blocks.redstone_relay;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mrh0.createaddition.blocks.connector.ConnectorType;
import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.energy.LocalNode;
import com.mrh0.createaddition.energy.NodeRotation;
import com.mrh0.createaddition.energy.WireType;
import com.mrh0.createaddition.energy.network.EnergyNetwork;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.network.EnergyNetworkPacketPayload;
import com.mrh0.createaddition.network.IObserveBlockEntity;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RedstoneRelayBlockEntity extends SmartBlockEntity implements IWireNode, IObserveBlockEntity {

	private final Set<LocalNode> wireCache = new HashSet<>();
	private final LocalNode[] localNodes;
	private final IWireNode[] nodeCache;
	private EnergyNetwork networkIn;
	private EnergyNetwork networkOut;
	private long demand = 0;
	private long throughput = 0;

	private boolean wasContraption = false;
	private boolean firstTick = true;

	public static Vec3 OFFSET_NORTH = new Vec3(	0f, 	-1f/16f, 	-5f/16f);
	public static Vec3 OFFSET_WEST = new Vec3(	-5f/16f, 	-1f/16f, 	0f);
	public static Vec3 OFFSET_SOUTH = new Vec3(	0f, 	-1f/16f, 	5f/16f);
	public static Vec3 OFFSET_EAST = new Vec3(	5f/16f, 	-1f/16f, 	0f);

	public static Vec3 IN_VERTICAL_OFFSET_NORTH = new Vec3(	5f/16f, 	0f, 	-1f/16f);
	public static Vec3 IN_VERTICAL_OFFSET_WEST = new Vec3(	-1f/16f, 	0f, 	-5f/16f);
	public static Vec3 IN_VERTICAL_OFFSET_SOUTH = new Vec3(	-5f/16f, 	0f, 	1f/16f);
	public static Vec3 IN_VERTICAL_OFFSET_EAST = new Vec3(	1f/16f, 	0f, 	5f/16f);

	public static Vec3 OUT_VERTICAL_OFFSET_NORTH = new Vec3(	-5f/16f, 	0f, 	-1f/16f);
	public static Vec3 OUT_VERTICAL_OFFSET_WEST = new Vec3(	-1f/16f, 	0f, 	5f/16f);
	public static Vec3 OUT_VERTICAL_OFFSET_SOUTH = new Vec3(	5f/16f, 	0f, 	1f/16f);
	public static Vec3 OUT_VERTICAL_OFFSET_EAST = new Vec3(	1f/16f, 	0f, 	-5f/16f);

	public static final int NODE_COUNT = 8;

	public RedstoneRelayBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);

		this.localNodes = new LocalNode[getNodeCount()];
		this.nodeCache = new IWireNode[getNodeCount()];
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}

	@Override
	public @Nullable IWireNode getWireNode(int index) {
		return IWireNode.getWireNodeFrom(index, this, this.localNodes, this.nodeCache, level);
	}

	@Override
	public @Nullable LocalNode getLocalNode(int index) {
		return this.localNodes[index];
	}

	@Override
	public void setNode(int index, int other, BlockPos pos, WireType type) {
		this.localNodes[index] = new LocalNode(this, index, other, type, pos);

		notifyUpdate();

		if (networkIn != null) networkIn.invalidate();
		if (networkOut != null) networkOut.invalidate();
	}

	@Override
	public void removeNode(int index, boolean dropWire) {
		LocalNode old = this.localNodes[index];
		this.localNodes[index] = null;

		invalidateNodeCache();
		notifyUpdate();

		if (networkIn != null) networkIn.invalidate();
		if (networkOut != null) networkOut.invalidate();
		if (dropWire && old != null) this.wireCache.add(old);
	}

	@Override
	public int getNodeCount() {
		return NODE_COUNT;
	}

	@Override
	public Vec3 getNodeOffset(int node) {
		boolean vertical = getBlockState().getValue(RedstoneRelayBlock.VERTICAL);
		Direction direction = getBlockState().getValue(RedstoneRelayBlock.HORIZONTAL_FACING);
		if(node > 3) {
			return switch (direction) {
				case NORTH -> vertical ? OUT_VERTICAL_OFFSET_NORTH : OFFSET_NORTH;
				case WEST -> vertical ? OUT_VERTICAL_OFFSET_WEST : OFFSET_WEST;
				case SOUTH -> vertical ? OUT_VERTICAL_OFFSET_SOUTH : OFFSET_SOUTH;
				case EAST -> vertical ? OUT_VERTICAL_OFFSET_EAST : OFFSET_EAST;
				default -> OFFSET_NORTH;
			};
		}
		return switch (direction) {
			case NORTH -> vertical ? IN_VERTICAL_OFFSET_NORTH : OFFSET_SOUTH;
			case WEST -> vertical ? IN_VERTICAL_OFFSET_WEST : OFFSET_EAST;
			case SOUTH -> vertical ? IN_VERTICAL_OFFSET_SOUTH : OFFSET_NORTH;
			case EAST -> vertical ? IN_VERTICAL_OFFSET_EAST : OFFSET_WEST;
			default -> OFFSET_NORTH;
		};
	}

	@Override
	public int getAvailableNode(Vec3 pos) {
		Direction dir = level.getBlockState(worldPosition).getValue(RedstoneRelayBlock.HORIZONTAL_FACING);
		boolean vertical = level.getBlockState(worldPosition).getValue(RedstoneRelayBlock.VERTICAL);
		boolean upper = true;
		pos = pos.subtract(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
		if (vertical) {
			switch (dir) {
				case NORTH -> upper = pos.x < 0.5d;
				case WEST -> upper = pos.z > 0.5d;
				case SOUTH -> upper = pos.x > 0.5d;
				case EAST -> upper = pos.z < 0.5d;
				default -> {}
			}
		} else {
			switch (dir) {
				case NORTH -> upper = pos.z < 0.5d;
				case WEST -> upper = pos.x < 0.5d;
				case SOUTH -> upper = pos.z > 0.5d;
				case EAST -> upper = pos.x > 0.5d;
				default -> {}
			}
		}


		for(int i = upper ? 4 : 0; i < (upper ? 8 : 4); i++) {
			if(hasConnection(i)) continue;
			return i;
		}
		return -1;
	}

	@Override
	public boolean isNodeInput(int node) {
		return node < 4;
	}

	@Override
	public boolean isNodeOutput(int node) {
		return !isNodeInput(node);
	}

	@Override
	public BlockPos getPos() {
		return getBlockPos();
	}

	@Override
	public EnergyNetwork getNetwork(int node) {
		return isNodeInput(node) ? networkIn : networkOut;
	}

	@Override
	public void setNetwork(int node, EnergyNetwork network) {
		if(isNodeInput(node)) networkIn = network;
		if(isNodeOutput(node)) networkOut = network;
	}

	@Override
	protected void read(ValueInput view, boolean clientPacket) {
		super.read(view, clientPacket);

		invalidateLocalNodes();
		invalidateNodeCache();
		boolean hasNodes = false;
		for (ValueInput node : view.childrenListOrEmpty(LocalNode.NODES)) {
			LocalNode localNode = new LocalNode(this, node);
			this.localNodes[localNode.getIndex()] = localNode;
			hasNodes = true;
		}

		if (!clientPacket && view.getBooleanOr("contraption", false)) {
			this.wasContraption = true;
			NodeRotation rotation = getBlockState().getValue(NodeRotation.ROTATION);
			if (rotation != NodeRotation.NONE)
				level.setBlock(getBlockPos(), getBlockState().setValue(NodeRotation.ROTATION, NodeRotation.NONE), 0);
			for (LocalNode localNode : this.localNodes) {
				if (localNode == null) continue;
				localNode.updateRelative(rotation);
			}
		}

		if (hasNodes && this.networkIn != null && this.networkOut != null) {
			this.networkIn.invalidate();
			this.networkOut.invalidate();
		}
	}

	@Override
	protected void write(ValueOutput view, boolean clientPacket) {
		super.write(view, clientPacket);
		ValueOutput.ValueOutputList nodes = view.childrenList(LocalNode.NODES);
		for (int i = 0; i < getNodeCount(); i++) {
			LocalNode localNode = this.localNodes[i];
			if (localNode == null) continue;
			localNode.write(nodes.addChild());
		}
	}

	private void validateNodes() {
		boolean changed = validateLocalNodes(this.localNodes);

		notifyUpdate();

		if (changed) {
			invalidateNodeCache();
			if (networkIn != null) networkIn.invalidate();
			if (networkOut != null) networkOut.invalidate();
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (this.firstTick) {
			this.firstTick = false;
			if (this.wasContraption && !level.isClientSide()) {
				this.wasContraption = false;
				validateNodes();
			}
		}

		if (!this.wireCache.isEmpty() && !isRemoved()) handleWireCache(level, this.wireCache);

		if (level.isClientSide()) return;
		networkTick();
	}

	private void networkTick() {
		if(awakeNetwork(level)) notifyUpdate();
		BlockState bs = getBlockState();
		throughput = 0;
		if(!bs.is(CABlocks.REDSTONE_RELAY)) return;
		if(bs.getValue(RedstoneRelayBlock.POWERED)) {
			throughput = networkOut.push(networkIn.pull(demand));
			demand = networkIn.demand(networkOut.getDemand());
		}
	}

	public long getThroughput() {
		return throughput;
	}

	@Override
	public void remove() {
		if (level == null) return;
		if (level.isClientSide()) return;
		for (int i = 0; i < getNodeCount(); i++) {
			LocalNode localNode = getLocalNode(i);
			if (localNode == null) continue;
			IWireNode otherNode = getWireNode(i);
			if (otherNode == null) continue;

			int ourNode = localNode.getOtherIndex();
			if (localNode.isInvalid()) otherNode.removeNode(ourNode);
			else otherNode.removeNode(ourNode, true);
		}

		invalidateNodeCache();

		if (networkIn != null) networkIn.invalidate();
		if (networkOut != null) networkOut.invalidate();
	}

	public void invalidateLocalNodes() {
		for(int i = 0; i < getNodeCount(); i++)
			this.localNodes[i] = null;
	}

	@Override
	public void invalidateNodeCache() {
		for(int i = 0; i < getNodeCount(); i++)
			this.nodeCache[i] = null;
	}

	@Override
	public boolean isNodeIndeciesConnected(int in, int other) {
		return isNodeInput(in) == isNodeInput(other);
	}

	@Override
	public ConnectorType getConnectorType() {
		return ConnectorType.Small;
	}

	public long getDemand() {
		return demand;
	}

	@Override
	public void onObserved(ServerPlayer player, ObservePacketPayload pack) {
		int node = pack.node();
		if (node < 0 || node >= getNodeCount()) return;
		if(isNetworkValid(node))
			EnergyNetworkPacketPayload.send(worldPosition, (int) getNetwork(node).getPulled(), (int) getNetwork(node).getPushed(), player);
	}

	@Override
	public int getMaxWireLength() {
		return CACommonConfig.COMMON.SMALL_CONNECTOR_MAX_LENGTH.get();
	}
}
