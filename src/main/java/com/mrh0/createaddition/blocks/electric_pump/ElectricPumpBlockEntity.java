package com.mrh0.createaddition.blocks.electric_pump;

import java.util.List;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.InternalEnergyStorage;
import com.mrh0.createaddition.transfer.EnergyTransferable;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.fluids.pump.PumpBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import team.reborn.energy.api.EnergyStorage;

public class ElectricPumpBlockEntity extends PumpBlockEntity implements EnergyTransferable {
	public static final float[][] PARTIAL_OFFSETS = {
			{8 / 16f, 4 / 16f, 8 / 16f},
			{8 / 16f, 12 / 16f, 8 / 16f}
	};

	public static final float[] PARTIAL_PHASE_OFFSETS = {6f, 0f};

	protected float pumpSpeed;
	protected ServerScrollValueBehaviour speedBehaviour;
	protected final InternalEnergyStorage energy;

	private boolean active = false;

	public ElectricPumpBlockEntity(BlockEntityType<? extends ElectricPumpBlockEntity> type, BlockPos pos, BlockState state) {
		super(pos, state);
		energy = new InternalEnergyStorage(CACommonConfig.COMMON.ELECTRIC_PUMP_CAPACITY.get(), CACommonConfig.COMMON.ELECTRIC_PUMP_MAX_INPUT.get(), 0);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
		super.addBehaviours(behaviours);
		speedBehaviour = new ServerScrollValueBehaviour(this);
		speedBehaviour.between(0, CACommonConfig.COMMON.ELECTRIC_PUMP_RPM_RANGE.get());
		speedBehaviour.setValue(32);
		behaviours.add(speedBehaviour);
	}

	protected void applySpeed() {
		float target = active ? pumpSpeed : 0;
		if (target == speed) return;
		float prevSpeed = speed;
		speed = target;
		onSpeedChanged(prevSpeed);
		sendData();
	}

	@Override
	public float getGeneratedSpeed() {
		return active ? pumpSpeed : 0;
	}

	@Override
	public void attachKinetics() {
		updateSpeed = false;
	}

	@Override
	public void setSource(BlockPos source) {
	}

	@Override
	public float calculateStressApplied() {
		return 0f;
	}

	@Override
	public float calculateAddedStressCapacity() {
		return 0f;
	}

	public static int getEnergyConsumptionRate(float speed) {
		float magnitude = Math.abs(speed);
		if (magnitude <= 0) return 0;
		float effectiveSpeed = Math.max(magnitude, 32);
		return (int) Math.max(1, Math.round(CACommonConfig.COMMON.ELECTRIC_PUMP_FE_RPM.get() * effectiveSpeed / 256d));
	}

	@Override
	protected void read(ValueInput tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		energy.read(tag);
		active = tag.getBooleanOr("active", false);
	}

	@Override
	protected void write(ValueOutput tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		energy.write(tag);
		tag.putBoolean("active", active);
	}

	@Override
	public void writeSafe(ValueOutput tag) {
		super.writeSafe(tag);
		energy.write(tag);
		tag.putBoolean("active", active);
	}

	@Override
	public void tick() {
		super.tick();

		if (speedBehaviour != null) {
			float behaviourSpeed = speedBehaviour.getValue();
			if (behaviourSpeed != pumpSpeed) {
				pumpSpeed = behaviourSpeed;
				applySpeed();
			}
		}

		if (level == null || level.isClientSide()) return;

		int con = getEnergyConsumptionRate(pumpSpeed);
		boolean powered = getBlockState().hasProperty(ElectricPumpBlock.POWERED) && getBlockState().getValue(ElectricPumpBlock.POWERED);
		boolean shouldRun = !powered && con > 0 && energy.getAmount() >= con;

		if (shouldRun) energy.internalConsumeEnergy(con);

		if (shouldRun != active) {
			active = shouldRun;
			applySpeed();
		}
	}

	public float getPumpSpeed() {
		return pumpSpeed;
	}

	public int getEnergyConsumption() {
		return getEnergyConsumptionRate(pumpSpeed);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		applySpeed();
	}

	public static float getPulseScale(boolean active, float pumpSpeed, float renderTime, float phaseOffsetTicks) {
		if (!active) return 1f;
		float rate = Math.max(32f, Math.abs(pumpSpeed)) / 32f;
		return 1f + Mth.sin((renderTime + phaseOffsetTicks) * 0.1f * rate) * 0.1f;
	}

	public static Quaternionf getFacingRotation(Direction facing) {
		return new Quaternionf().rotateTo(0, 1, 0, facing.getStepX(), facing.getStepY(), facing.getStepZ());
	}

	@Override
	public EnergyStorage getEnergyStorage(@Nullable Direction direction) {
		return energy;
	}
}
