package com.mrh0.createaddition.blocks.connector.base;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.*;
import com.mrh0.createaddition.energy.network.EnergyNetwork;
import com.mrh0.createaddition.network.EnergyNetworkPacketPayload;
import com.mrh0.createaddition.network.IObserveBlockEntity;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.transfer.EnergyTransferable;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public abstract class AbstractConnectorBlockEntity extends SmartBlockEntity implements EnergyTransferable, IWireNode, IObserveBlockEntity {

	private final Set<LocalNode> wireCache = new HashSet<>();
	private final LocalNode[] localNodes;
	private final IWireNode[] nodeCache;
	private EnergyNetwork network;

	private boolean wasContraption = false;
	private boolean firstTick = true;

	public EnergyStorage internal = new InterfaceEnergyHandler();
	protected BlockApiCache<EnergyStorage, Direction> externalStorageCache;

	public AbstractConnectorBlockEntity(BlockEntityType<?> blockEntityTypeIn, BlockPos pos, BlockState state) {
		super(blockEntityTypeIn, pos, state);

		this.localNodes = new LocalNode[getNodeCount()];
		this.nodeCache = new IWireNode[getNodeCount()];
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}

	@Nullable
	@Override
	public EnergyStorage getEnergyStorage(@Nullable Direction direction) {
		if(isEnergyInput(direction) || isEnergyOutput(direction)) return internal;
		return null;
	}

	public abstract long getMaxIn();
	public abstract long getMaxOut();
	public long getCapacityOutside() {
		return Math.min(getMaxIn(), getMaxOut());
	}

	private class InterfaceEnergyHandler extends SnapshotParticipant<long[]> implements EnergyStorage {

		private long pendingIn;
		private long pendingOut;

		@Override
		public long insert(long maxAmount, TransactionContext transaction) {
			if(!CACommonConfig.COMMON.CONNECTOR_ALLOW_PASSIVE_IO.get()) return 0;
			if(getMode() != ConnectorMode.Pull) return 0;
			if (network == null) return 0;
			maxAmount = Math.min(maxAmount, getMaxIn() - pendingIn);
			if (maxAmount <= 0) return 0;
			long accepted = network.push(maxAmount, true);
			if (accepted <= 0) return 0;
			updateSnapshots(transaction);
			pendingIn += accepted;
			return accepted;
		}

		@Override
		public long extract(long maxAmount, TransactionContext transaction) {
			if(!CACommonConfig.COMMON.CONNECTOR_ALLOW_PASSIVE_IO.get()) return 0;
			if(getMode() != ConnectorMode.Push) return 0;
			if (network == null) return 0;
			maxAmount = Math.min(maxAmount, getMaxOut() - pendingOut);
			if (maxAmount <= 0) return 0;
			long extracted = network.pull(maxAmount, true);
			if (extracted <= 0) return 0;
			updateSnapshots(transaction);
			pendingOut += extracted;
			return extracted;
		}

		@Override
		protected long[] createSnapshot() {
			return new long[]{pendingIn, pendingOut};
		}

		@Override
		protected void readSnapshot(long[] snapshot) {
			pendingIn = snapshot[0];
			pendingOut = snapshot[1];
		}

		@Override
		protected void onFinalCommit() {
			if (network != null) {
				if (pendingIn > 0) network.push(pendingIn);
				if (pendingOut > 0) network.pull(pendingOut);
			}
			pendingIn = 0;
			pendingOut = 0;
		}

		@Override
		public long getAmount() {
			if (network == null) return 0;
			return Math.min(getCapacity(), network.getBuff());
		}

		@Override
		public long getCapacity() {
			return getCapacityOutside();
		}
	}

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

		if (network != null) network.invalidate();
	}

	@Override
	public void removeNode(int index, boolean dropWire) {
		LocalNode old = this.localNodes[index];
		this.localNodes[index] = null;
		this.nodeCache[index] = null;

		invalidateNodeCache();
		notifyUpdate();

		if (network != null) network.invalidate();
		if (dropWire && old != null) this.wireCache.add(old);
	}

	@Override
	public BlockPos getPos() {
		return getBlockPos();
	}

	@Override
	public void setNetwork(int node, EnergyNetwork network) {
		this.network = network;
	}

	@Override
	public EnergyNetwork getNetwork(int node) {
		return network;
	}

	public boolean isEnergyInput(Direction side) {
		return getBlockState().getValue(AbstractConnectorBlock.FACING) == side;
	}

	public boolean isEnergyOutput(Direction side) {
		return getBlockState().getValue(AbstractConnectorBlock.FACING) == side;
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
			if(level == null) return;
			if (rotation != NodeRotation.NONE)
				level.setBlock(getBlockPos(), getBlockState().setValue(NodeRotation.ROTATION, NodeRotation.NONE), 0);
			for (LocalNode localNode : this.localNodes) {
				if (localNode == null) continue;
				localNode.updateRelative(rotation);
			}
		}

		if (hasNodes && this.network != null) this.network.invalidate();
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
			if (this.network != null) this.network.invalidate();
		}
	}

	public void firstTick() {
		this.firstTick = false;
		if(level == null) return;
		if (this.wasContraption && !level.isClientSide()) {
			this.wasContraption = false;
			validateNodes();
		}

		updateExternalEnergyStorage();
	}

	protected void specialTick() {}

	@Override
	public void tick() {
		if (this.firstTick) firstTick();
		if (level == null) return;
		if (!level.isLoaded(getBlockPos())) return;

		if (!this.wireCache.isEmpty() && !isRemoved()) handleWireCache(level, this.wireCache);

		specialTick();

		if (getMode() == ConnectorMode.None) return;
		super.tick();

		if(level == null) return;
		if(level.isClientSide()) return;
		if(awakeNetwork(level)) notifyUpdate();

		networkTick(network);
	}

	private void networkTick(EnergyNetwork network) {
		ConnectorMode mode = getMode();
		if(level == null) return;
		if(level.isClientSide()) return;

		EnergyStorage externalStorage = getExternalEnergyStorage();
		if(externalStorage == null) {
			return;
		}

		if (mode == ConnectorMode.Push) {
			long pulled;
			try (Transaction t = Transaction.openOuter()) {
				pulled = network.pull(network.demand(externalStorage.insert(getMaxOut(), t)));
			}
			try (Transaction t = Transaction.openOuter()) {
				externalStorage.insert(pulled, t);
				t.commit();
			}
		}

		if (mode == ConnectorMode.Pull) {
			long toPush;
			try (Transaction t = Transaction.openOuter()) {
				toPush = externalStorage.extract(network.push(getMaxIn(), true), t);
				t.commit();
			}
			network.push(toPush);
		}
	}

	@Override
	public void remove() {
		if(level == null) return;
		if (level.isClientSide()) return;
		for (int i = 0; i < getNodeCount(); i++) {
			LocalNode localNode = getLocalNode(i);
			if (localNode == null) continue;
			IWireNode otherNode = getWireNode(i);
			if(otherNode == null) continue;

			int ourNode = localNode.getOtherIndex();
			if (localNode.isInvalid())
				otherNode.removeNode(ourNode);
			else
				otherNode.removeNode(ourNode, true);
		}

		invalidateNodeCache();

		if (network != null) network.invalidate();
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

	public ConnectorMode getMode() {
		return getBlockState().getValue(AbstractConnectorBlock.MODE);
	}

	@Override
	public void onObserved(ServerPlayer player, ObservePacketPayload pack) {
		if(isNetworkValid(0))
			EnergyNetworkPacketPayload.send(worldPosition, (int) getNetwork(0).getPulled(), (int) getNetwork(0).getPushed(), player);
	}

	public boolean ignoreCapSide() {
		return this.getBlockState().getValue(AbstractConnectorBlock.MODE).isActive();
	}

	public void updateExternalEnergyStorage() {
		if (!(level instanceof ServerLevel serverLevel)) return;
		if (!level.isLoaded(getBlockPos())) return;
		Direction side = getBlockState().getValue(AbstractConnectorBlock.FACING);
		externalStorageCache = BlockApiCache.create(EnergyStorage.SIDED, serverLevel, worldPosition.relative(side));
	}

	@Nullable
	public EnergyStorage getExternalEnergyStorage() {
		if (externalStorageCache == null) {
			updateExternalEnergyStorage();
			if (externalStorageCache == null) return null;
		}
		Direction side = getBlockState().getValue(AbstractConnectorBlock.FACING);
		EnergyStorage es = externalStorageCache.find(side.getOpposite());
		if (ignoreCapSide() && es == null) es = externalStorageCache.find(null);
		return es;
	}
}
