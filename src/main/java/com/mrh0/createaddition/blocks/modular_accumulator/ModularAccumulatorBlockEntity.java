package com.mrh0.createaddition.blocks.modular_accumulator;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.IMultiTileEnergyContainer;
import com.mrh0.createaddition.energy.InternalEnergyStorage;
import com.mrh0.createaddition.network.EnergyNetworkPacketPayload;
import com.mrh0.createaddition.network.IObserveBlockEntity;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.transfer.EnergyTransferable;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchObservable;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public class ModularAccumulatorBlockEntity extends SmartBlockEntity implements EnergyTransferable, IMultiTileEnergyContainer, IObserveBlockEntity, ThresholdSwitchObservable {
	protected InternalEnergyStorage energyCapability;
	protected BlockPos controller;
	protected BlockPos lastKnownPos;
	protected boolean updateConnectivity;
	protected int width;
	protected int height;
	protected boolean updateCapability;

	private static final int SYNC_RATE = 8;
	protected int syncCooldown;
	protected boolean queuedSync;

	private final EnumMap<Direction, BlockApiCache<EnergyStorage, Direction>> escacheMap = new EnumMap<>(Direction.class);

	public ModularAccumulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		energyCapability = createEnergyStorage();
		updateConnectivity = false;
		height = 1;
		width = 1;
		updateCapability = false;
		refreshCapability();
	}

	@Override
	public void onChunkUnloaded() {}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
		super.preRemoveSideEffects(pos, oldState);
		level.removeBlockEntity(pos);
		CAConnectivityHandler.splitMulti(this);
	}

	protected InternalEnergyStorage createEnergyStorage() {
		return new InternalEnergyStorage(getCapacityMultiplier(), CACommonConfig.COMMON.ACCUMULATOR_MAX_INPUT.get(), CACommonConfig.COMMON.ACCUMULATOR_MAX_OUTPUT.get());
	}

	protected void updateConnectivity() {
		updateConnectivity = false;
		if (level == null) return;
		if (level.isClientSide()) return;
		if (!level.isLoaded(getBlockPos())) return;
		if (!isController()) return;
		CAConnectivityHandler.formMulti(this);
	}

	public LerpedFloat gauge = LerpedFloat.linear();

	long lastEnergy = 0;
	int energyChangeTick = 0;

	@Override
	public void tick() {
		super.tick();

		tickOutput();

		if (syncCooldown > 0) {
			syncCooldown--;
			if (syncCooldown == 0 && queuedSync) sendData();
		}

		if (lastKnownPos == null) lastKnownPos = getBlockPos();
		else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
			onPositionChanged();
			return;
		}

		if (updateCapability) {
			updateCapability = false;
			refreshCapability();
		}

		if (updateConnectivity) updateConnectivity();

		if (!isController()) return;

		if (Math.abs(lastEnergy - energyCapability.getAmount()) > 256) {
			lastEnergy = energyCapability.getAmount();
			onEnergyChanged();
		}

		if (energyChangeTick > 0) energyChangeTick--;

		if (level == null) return;
		if (level.isClientSide()) {
			gauge.tickChaser();
			float current = gauge.getValue(1);
			if (current > 1 && level.getRandom().nextFloat() < 1 / 2f)
				gauge.setValueNoUpdate(current + Math.min(-(current - 1) * level.getRandom().nextFloat(), 0));
		}
	}

	public void tickOutput() {
		if (getControllerBE() == null) return;
		BlockState state = this.getBlockState();
		if (!ModularAccumulatorBlock.isAccumulator(state)) return;
		if (state.getValue(ModularAccumulatorBlock.TOP)) tickOutputSide(Direction.UP);
		if (state.getValue(ModularAccumulatorBlock.BOTTOM)) tickOutputSide(Direction.DOWN);
	}

	public void tickOutputSide(Direction side) {
		if (!(level instanceof ServerLevel serverLevel)) return;
		if (!level.isLoaded(getBlockPos())) return;
		if (!level.isLoaded(getBlockPos().relative(side))) return;
		ModularAccumulatorBlockEntity controllerBE = getControllerBE();
		if (controllerBE == null) return;
		BlockApiCache<EnergyStorage, Direction> sideCache = escacheMap.computeIfAbsent(side,
				side1 -> BlockApiCache.create(EnergyStorage.SIDED, serverLevel, getBlockPos().relative(side1)));
		EnergyStorage target = sideCache.find(side.getOpposite());
		if (target == null) return;
		if (target == controllerBE.energyCapability) return;
		try (Transaction t = Transaction.openOuter()) {
			EnergyStorageUtil.move(controllerBE.energyCapability, target, CACommonConfig.COMMON.ACCUMULATOR_MAX_OUTPUT.get(), t);
			t.commit();
		}
	}

	@Override
	public BlockPos getLastKnownPos() {
		return lastKnownPos;
	}

	@Override
	public boolean isController() {
		return controller == null || worldPosition.getX() == controller.getX()
			&& worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
	}

	@Override
	public void initialize() {
		super.initialize();
		sendData();
		if (level == null) return;
		if (level.isClientSide()) invalidateRenderBoundingBox();
	}

	private void onPositionChanged() {
		removeController(true);
		lastKnownPos = worldPosition;
		escacheMap.clear();
	}

	protected void onEnergyChanged() {
		if (level == null) return;
		if (!level.isLoaded(getBlockPos())) return;
		if (!hasLevel()) return;

		energyChangeTick = 20;

		for (int yOffset = 0; yOffset < height; yOffset++) {
			for (int xOffset = 0; xOffset < width; xOffset++) {
				for (int zOffset = 0; zOffset < width; zOffset++) {
					BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
					if (!level.isLoaded(pos)) return;
					ModularAccumulatorBlockEntity acc = CAConnectivityHandler.partAt(getType(), level, pos);
					if (acc == null) continue;
					level.updateNeighbourForOutputSignal(pos, acc.getBlockState().getBlock());
				}
			}
		}

		if (!level.isClientSide()) {
			setChanged();
			sendData();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable ModularAccumulatorBlockEntity getControllerBE() {
		if (isController()) return this;
		if (level == null) return this;
		if (!level.isLoaded(getBlockPos())) return this;
		BlockEntity blockEntity = level.getBlockEntity(controller);
		if (blockEntity instanceof ModularAccumulatorBlockEntity)
			return (ModularAccumulatorBlockEntity) blockEntity;
		return null;
	}

	public void applySize(int blocks) {
		energyCapability.setCapacity((long) blocks * getCapacityMultiplier());
		long overflow = energyCapability.getAmount() - energyCapability.getCapacity();
		if (overflow > 0) energyCapability.internalConsumeEnergy(overflow);
	}

	@Override
	public void removeController(boolean keepEnergy) {
		if (level == null) return;
		if (level.isClientSide()) return;
		if (!level.isLoaded(getBlockPos())) return;
		updateConnectivity = true;
		if (!keepEnergy) applySize(1);
		controller = null;
		width = 1;
		height = 1;
		onEnergyChanged();

		BlockState state = getBlockState();
		if (ModularAccumulatorBlock.isAccumulator(state)) {
			state = state.setValue(ModularAccumulatorBlock.BOTTOM, true);
			state = state.setValue(ModularAccumulatorBlock.TOP, true);
			getLevel().setBlock(worldPosition, state, Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE | Block.UPDATE_KNOWN_SHAPE);
		}

		refreshCapability();
		setChanged();
		sendData();
	}

	public void sendDataImmediately() {
		syncCooldown = 0;
		queuedSync = false;
		sendData();
	}

	@Override
	public void sendData() {
		if (syncCooldown > 0) {
			queuedSync = true;
			return;
		}
		super.sendData();
		queuedSync = false;
		syncCooldown = SYNC_RATE;
	}

	@Override
	public void setController(BlockPos controller) {
		if (level == null) return;
		if (level.isClientSide() && !isVirtual()) return;
		if (controller.equals(this.controller)) return;
		this.controller = controller;
		refreshCapability();
		setChanged();
		sendData();
	}

	void refreshCapability() {
		energyCapability = handlerForCapability();
	}

	private InternalEnergyStorage handlerForCapability() {
		if (isController()) return energyCapability;
		ModularAccumulatorBlockEntity controllerBE = getControllerBE();
		if (controllerBE != null && controllerBE != this) {
			return controllerBE.handlerForCapability();
		}
		return new InternalEnergyStorage(0, CACommonConfig.COMMON.ACCUMULATOR_MAX_INPUT.get(), CACommonConfig.COMMON.ACCUMULATOR_MAX_OUTPUT.get());
	}

	@Override
	public @Nullable EnergyStorage getEnergyStorage(@Nullable Direction side) {
		if (energyCapability == null) refreshCapability();
		return energyCapability;
	}

	@Override
	public BlockPos getController() {
		return isController() ? worldPosition : controller;
	}

	@Override
	protected AABB createRenderBoundingBox() {
		if (isController()) return super.createRenderBoundingBox().expandTowards(width - 1, height - 1, width - 1);
		else return super.createRenderBoundingBox();
	}

	@Override
	protected void read(ValueInput view, boolean clientPacket) {
		super.read(view, clientPacket);

		BlockPos controllerBefore = controller;
		int prevSize = width;
		int prevHeight = height;

		updateConnectivity = view.getBooleanOr("Uninitialized", false);
		lastKnownPos = view.read("LastKnownPos", BlockPos.CODEC).orElse(null);
		controller = view.read("Controller", BlockPos.CODEC).orElse(null);

		if (isController()) {
			width = view.getIntOr("Size", 0);
			height = view.getIntOr("Height", 0);
			energyCapability.setCapacity((long) getTotalAccumulatorSize() * getCapacityMultiplier());
			energyCapability.read(view.childOrEmpty("EnergyContent"));
			long overflow = energyCapability.getAmount() - energyCapability.getCapacity();
			if (overflow > 0) energyCapability.internalConsumeEnergy(overflow);
		}

		updateCapability = true;

		if (!clientPacket) return;

		boolean changeOfController = !Objects.equals(controllerBefore, controller);
		if (changeOfController || prevSize != width || prevHeight != height) {
			if (hasLevel()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
			if (isController()) energyCapability.setCapacity((long) getCapacityMultiplier() * getTotalAccumulatorSize());
			invalidateRenderBoundingBox();
		}

		if (isController()) gauge.chase(getFillState(), 0.125f, LerpedFloat.Chaser.EXP);
	}

	public float getFillState() {
		return (float) energyCapability.getAmount() / energyCapability.getCapacity();
	}

	@Override
	protected void write(ValueOutput view, boolean clientPacket) {
		if (updateConnectivity) view.putBoolean("Uninitialized", true);
		if (lastKnownPos != null) view.store("LastKnownPos", BlockPos.CODEC, lastKnownPos);
		if (!isController()) view.store("Controller", BlockPos.CODEC, controller);
		if (isController()) {
			energyCapability.write(view.child("EnergyContent"));
			view.putLong("EnergyCapacity", (long) getTotalAccumulatorSize() * getCapacityMultiplier());
			view.putInt("Size", width);
			view.putInt("Height", height);
		}
		super.write(view, clientPacket);

		if (!clientPacket) return;
		if (queuedSync) view.putBoolean("LazySync", true);
	}

	@Override
	public void writeSafe(ValueOutput view) {
		if (isController()) {
			view.putInt("Size", width);
			view.putInt("Height", height);
		}
	}

	public float audioPitch() {
		int sizeInBlocks = getTotalAccumulatorSize();
		float pitch = 0.75f;
		if (sizeInBlocks < 4) pitch = 1.25f;
		if (sizeInBlocks < 9) pitch = 1f;
		return pitch;
	}

	public boolean isEnergyChanging() {
		return energyChangeTick != 0;
	}

	public int getTotalAccumulatorSize() {
		return width * width * height;
	}

	public static int getCapacityMultiplier() {
		return CACommonConfig.COMMON.ACCUMULATOR_CAPACITY.get();
	}

	public static int getMaxHeight() {
		return CACommonConfig.COMMON.ACCUMULATOR_MAX_HEIGHT.get();
	}

	@Override
	public int getMaxWidth() {
		return CACommonConfig.COMMON.ACCUMULATOR_MAX_WIDTH.get();
	}

	@Override
	public void preventConnectivityUpdate() {
		updateConnectivity = false;
	}

	@Override
	public void notifyMultiUpdated() {
		BlockState state = this.getBlockState();
		if (ModularAccumulatorBlock.isAccumulator(state)) {
			state = state.setValue(ModularAccumulatorBlock.BOTTOM, getController().getY() == getBlockPos().getY());
			state = state.setValue(ModularAccumulatorBlock.TOP, getController().getY() + height - 1 == getBlockPos().getY());
			if (level == null) return;
			level.setBlock(getBlockPos(), state, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE);
		}
		setChanged();
		if (isController()) sendDataImmediately();
	}

	@Override
	public Direction.Axis getMainConnectionAxis() {
		return Direction.Axis.Y;
	}

	@Override
	public int getMaxLength(Direction.Axis longAxis, int width) {
		if (longAxis == Direction.Axis.Y) return getMaxHeight();
		return getMaxWidth();
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
	}

	@Override
	public void onObserved(ServerPlayer player, ObservePacketPayload pack) {
		ModularAccumulatorBlockEntity controllerBE = getControllerBE();
		if (controllerBE == null) return;

		EnergyNetworkPacketPayload.send(worldPosition, 0, (int) controllerBE.energyCapability.getAmount(), player);
	}

	public boolean hasAccumulator() {
		return true;
	}

	public long getSize(int accumulator) {
		return getCapacityMultiplier();
	}

	public void setSize(int accumulator, int blocks) {
		applySize(blocks);
	}

	public InternalEnergyStorage getEnergy() {
		return energyCapability;
	}

	@Override
	public int getMaxValue() {
		return 100;
	}

	@Override
	public int getMinValue() {
		return 0;
	}

	@Override
	public int getCurrentValue() {
		ModularAccumulatorBlockEntity controllerBE = getControllerBE();
		if (controllerBE == null) return 0;
		return (int) ((float) controllerBE.energyCapability.getAmount() / (float) controllerBE.energyCapability.getCapacity() * 100f);
	}

	@Override
	public MutableComponent format(int i) {
		return Component.literal(i + "%");
	}
}
