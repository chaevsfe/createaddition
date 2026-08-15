package com.mrh0.createaddition.blocks.electric_motor;

import java.util.List;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.InternalEnergyStorage;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.transfer.EnergyTransferable;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerKineticScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class ElectricMotorBlockEntity extends GeneratingKineticBlockEntity implements EnergyTransferable {
	protected float motorSpeed;
	protected ServerScrollValueBehaviour generatedSpeed;
	protected final InternalEnergyStorage energy;

	private boolean cc_update_rpm = false;
	private float cc_new_rpm = 32.0f;

	private boolean active = false;

	public ElectricMotorBlockEntity(BlockEntityType<? extends ElectricMotorBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		energy = new InternalEnergyStorage(CACommonConfig.COMMON.ELECTRIC_MOTOR_CAPACITY.get(), CACommonConfig.COMMON.ELECTRIC_MOTOR_MAX_INPUT.get(), 0);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
		super.addBehaviours(behaviours);
		generatedSpeed = new ServerKineticScrollValueBehaviour(this);
		generatedSpeed.between(-CACommonConfig.COMMON.ELECTRIC_MOTOR_RPM_RANGE.get(), CACommonConfig.COMMON.ELECTRIC_MOTOR_RPM_RANGE.get());
		generatedSpeed.setValue(32);
		generatedSpeed.withCallback(this::updateGeneratedRotation);
		behaviours.add(generatedSpeed);
	}

	@Override
	public float calculateAddedStressCapacity() {
		float capacity = CACommonConfig.COMMON.MAX_STRESS.get()/256f;
		this.lastCapacityProvided = capacity;
		return capacity;
	}

	public void updateGeneratedRotation(int rpm) {
		motorSpeed = rpm;
		super.updateGeneratedRotation();
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
			updateGeneratedRotation();
	}

	@Override
	public float getGeneratedSpeed() {
		if (!getBlockState().is(CABlocks.ELECTRIC_MOTOR))
			return 0;
		return convertToDirection(active ? motorSpeed : 0, getBlockState().getValue(ElectricMotorBlock.FACING));
	}

	@Override
	protected Block getStressConfigKey() {
		return CABlocks.ELECTRIC_MOTOR;
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

	public static int getEnergyConsumptionRate(float rpm) {
		return Math.abs(rpm) > 0 ? (int)Math.max((double) CACommonConfig.COMMON.FE_RPM.get() * ((double)Math.abs(rpm) / 256d), (double) CACommonConfig.COMMON.ELECTRIC_MOTOR_MINIMUM_CONSUMPTION.get()) : 0;
	}

	boolean first = true;

	@Override
	public void tick() {
		super.tick();
		if(first) {
			motorSpeed = generatedSpeed.getValue();
			updateGeneratedRotation();
			first = false;
		}

		if(cc_update_rpm) {
			generatedSpeed.setValue(Math.round(cc_new_rpm));
			motorSpeed = cc_new_rpm;
			cc_update_rpm = false;
			updateGeneratedRotation();
		}

		if(level == null || level.isClientSide()) return;
		int con = getEnergyConsumptionRate(motorSpeed);
		if(!active) {
			if(energy.getAmount() > con * 2 && !getBlockState().getValue(ElectricMotorBlock.POWERED)) {
				active = true;
				updateGeneratedRotation();
			}
		}
		else {
			long ext = energy.internalConsumeEnergy(con);
			if(ext < con || getBlockState().getValue(ElectricMotorBlock.POWERED)) {
				active = false;
				updateGeneratedRotation();
			}
		}
	}

	public static float getDurationAngle(float deg, float initialProgress, float speed) {
		speed = Math.abs(speed);
		deg = Math.abs(deg);
		if(speed < 0.1f) return 0;
		double degreesPerTick = (speed * 360) / 60 / 20;
		return (float) ((1 - initialProgress) * deg / degreesPerTick + 1);
	}

	public static float getDurationDistance(float dis, float initialProgress, float speed) {
		speed = Math.abs(speed);
		dis = Math.abs(dis);
		if(speed < 0.1f) return 0;
		double metersPerTick = speed / 512;
		return (float) ((1 - initialProgress) * dis / metersPerTick);
	}

	public boolean setRPM(float rpm) {
		rpm = Math.max(Math.min(rpm, CACommonConfig.COMMON.ELECTRIC_MOTOR_RPM_RANGE.get()), -CACommonConfig.COMMON.ELECTRIC_MOTOR_RPM_RANGE.get());
		cc_new_rpm = rpm;
		cc_update_rpm = true;
		return true;
	}

	public float getRPM() {
		return motorSpeed;
	}

	public int getGeneratedStress() {
		return (int) calculateAddedStressCapacity();
	}

	public int getEnergyConsumption() {
		return getEnergyConsumptionRate(motorSpeed);
	}

	public boolean isPoweredState() {
		return getBlockState().getValue(ElectricMotorBlock.POWERED);
	}

	@Override
	public EnergyStorage getEnergyStorage(@Nullable Direction direction) {
		return energy;
	}
}
