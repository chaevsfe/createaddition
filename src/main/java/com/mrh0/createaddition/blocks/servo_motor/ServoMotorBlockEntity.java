package com.mrh0.createaddition.blocks.servo_motor;

import java.util.List;

import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorBlockEntity;
import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.InternalEnergyStorage;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.transfer.EnergyTransferable;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.IControlContraption.RotationMode;
import com.zurrtum.create.content.contraptions.bearing.BearingContraption;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerKineticScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import team.reborn.energy.api.EnergyStorage;

public class ServoMotorBlockEntity extends MechanicalBearingBlockEntity implements EnergyTransferable {

	static final BehaviourType<ServoMovementMode> MOVEMENT_MODE_TYPE = new BehaviourType<>();
	static final BehaviourType<ServoAngleLimit> MIN_ANGLE_TYPE = new BehaviourType<>();
	static final BehaviourType<ServoAngleLimit> MAX_ANGLE_TYPE = new BehaviourType<>();

	static class ServoMovementMode extends ServerScrollOptionBehaviour<RotationMode> {
		ServoMovementMode(SmartBlockEntity be) {
			super(RotationMode.class, be);
		}

		@Override
		public BehaviourType<?> getType() {
			return MOVEMENT_MODE_TYPE;
		}

		@Override
		public int netId() {
			return 1;
		}

		@Override
		public void write(ValueOutput view, boolean clientPacket) {
			view.putInt("RotationMode", value);
		}

		@Override
		public void read(ValueInput view, boolean clientPacket) {
			value = view.getIntOr("RotationMode", 0);
		}
	}

	static class ServoAngleLimit extends ServerScrollValueBehaviour {
		private final BehaviourType<ServoAngleLimit> myType;
		private final int myNetId;
		private final String nbtKey;
		private final int defaultVal;
		private final boolean negated;

		ServoAngleLimit(SmartBlockEntity be, BehaviourType<ServoAngleLimit> type, int netId, String nbtKey, int defaultVal, boolean negated) {
			super(be);
			this.myType = type;
			this.myNetId = netId;
			this.nbtKey = nbtKey;
			this.defaultVal = defaultVal;
			this.negated = negated;
			between(0, 180);
			value = defaultVal;
		}

		public float getAngle() {
			return negated ? -value : value;
		}

		@Override
		public BehaviourType<?> getType() {
			return myType;
		}

		@Override
		public int netId() {
			return myNetId;
		}

		@Override
		public void write(ValueOutput view, boolean clientPacket) {
			view.putInt(nbtKey, value);
		}

		@Override
		public void read(ValueInput view, boolean clientPacket) {
			value = view.getIntOr(nbtKey, defaultVal);
		}
	}

	static class ServoSpeedBehaviour extends ServerKineticScrollValueBehaviour {
		ServoSpeedBehaviour(SmartBlockEntity be) {
			super(be);
			value = 32;
		}
	}

	protected float motorSpeed;
	protected ServerScrollValueBehaviour generatedSpeed;
	protected ServoAngleLimit minAngle;
	protected ServoAngleLimit maxAngle;
	protected final InternalEnergyStorage energy;

	private boolean active = false;
	private boolean firstTick = true;
	private boolean allowAssemble = false;
	private int lastNetSignal = Integer.MIN_VALUE;
	private float targetAngle = 0;

	public ServoMotorBlockEntity(BlockEntityType<? extends ServoMotorBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		energy = new InternalEnergyStorage(CACommonConfig.COMMON.ELECTRIC_MOTOR_CAPACITY.get(),
				CACommonConfig.COMMON.ELECTRIC_MOTOR_MAX_INPUT.get(), 0);
		setLazyTickRate(20);
	}

	@Override
	public @Nullable EnergyStorage getEnergyStorage(@Nullable Direction direction) {
		return energy;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
		super.addBehaviours(behaviours);

		for (int i = 0; i < behaviours.size(); i++) {
			if (behaviours.get(i) == movementMode) {
				movementMode = new ServoMovementMode(this);
				behaviours.set(i, movementMode);
				break;
			}
		}

		generatedSpeed = new ServoSpeedBehaviour(this);
		generatedSpeed.between(-CACommonConfig.COMMON.ELECTRIC_MOTOR_RPM_RANGE.get(),
				CACommonConfig.COMMON.ELECTRIC_MOTOR_RPM_RANGE.get());
		generatedSpeed.withCallback(rpm -> {
			motorSpeed = rpm;
			updateGeneratedRotation();
		});
		behaviours.add(generatedSpeed);

		maxAngle = new ServoAngleLimit(this, MAX_ANGLE_TYPE, 2, "MaxAngle", 90, false);
		behaviours.add(maxAngle);

		minAngle = new ServoAngleLimit(this, MIN_ANGLE_TYPE, 3, "MinAngle", 90, true);
		behaviours.add(minAngle);
	}

	@Override
	public float getInterpolatedAngle(float partialTicks) {
		if (!running)
			return angle;
		if (movedContraption != null)
			return movedContraption.getAngle(partialTicks + 1f);
		return angle;
	}

	@Override
	public float getAngularSpeed() {
		if (running)
			return 0;
		return super.getAngularSpeed();
	}

	@Override
	public float getGeneratedSpeed() {
		if (!getBlockState().is(CABlocks.SERVO_MOTOR))
			return 0;
		return convertToDirection(active ? motorSpeed : 0, getBlockState().getValue(ServoMotorBlock.FACING));
	}

	@Override
	public float calculateAddedStressCapacity() {
		float capacity = CACommonConfig.COMMON.MAX_STRESS.get() / 256f;
		this.lastCapacityProvided = capacity;
		return capacity;
	}

	@Override
	protected Block getStressConfigKey() {
		return CABlocks.SERVO_MOTOR;
	}

	@Override
	public void assemble() {
		if (!allowAssemble)
			return;
		allowAssemble = false;

		if (!(level.getBlockState(worldPosition).getBlock() instanceof ServoMotorBlock))
			return;

		Direction direction = getBlockState().getValue(ServoMotorBlock.FACING);
		BearingContraption contraption = new BearingContraption(false, direction);
		try {
			if (!contraption.assemble(level, worldPosition))
				return;
			lastException = null;
		} catch (AssemblyException e) {
			lastException = e;
			sendData();
			return;
		}

		contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
		movedContraption = ControlledContraptionEntity.create(level, this, contraption);
		BlockPos anchor = worldPosition.relative(direction);
		movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
		movedContraption.setRotationAxis(direction.getAxis());
		level.addFreshEntity(movedContraption);

		AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(level, worldPosition);

		if (contraption.containsBlockBreakers())
			award(AllAdvancements.CONTRAPTION_ACTORS);

		running = true;
		angle = 0;
		lastNetSignal = Integer.MIN_VALUE;
		sendData();
		updateGeneratedRotation();
	}

	@Override
	public void tick() {
		if (firstTick) {
			motorSpeed = generatedSpeed.getValue();
			firstTick = false;
		}

		if (!level.isClientSide()) {
			int con = ElectricMotorBlockEntity.getEnergyConsumptionRate(motorSpeed);
			if (!running) {
				boolean hasEnergy = energy.getAmount() >= con;
				if (active != hasEnergy) {
					active = hasEnergy;
					updateGeneratedRotation();
				}
			} else {
				boolean moving = Math.abs(targetAngle - angle) > 0.001f && Math.abs(motorSpeed) > 0f;
				int consumption = moving ? con : con / 10;
				if (!active) {
					if (energy.getAmount() >= con) {
						active = true;
						updateGeneratedRotation();
					}
				} else {
					if (energy.internalConsumeEnergy(consumption) < consumption) {
						active = false;
						updateGeneratedRotation();
					}
				}
			}
		}

		super.tick();

		if (!level.isClientSide() && active) {
			Direction facing = getBlockState().getValue(ServoMotorBlock.FACING);
			Direction maxFace = facing.getAxis() == Axis.Y ? Direction.EAST : Direction.UP;
			Direction minFace = facing.getAxis() == Axis.Y ? Direction.WEST : Direction.DOWN;
			int maxSignal = level.getSignal(worldPosition.relative(maxFace), maxFace);
			int minSignal = level.getSignal(worldPosition.relative(minFace), minFace);
			int net = maxSignal - minSignal;
			if (net != lastNetSignal) {
				lastNetSignal = net;
				targetAngle = net >= 0
						? (net / 15.0f) * maxAngle.getAngle()
						: (-net / 15.0f) * minAngle.getAngle();
				if (net != 0 && !running)
					triggerAssemble();
				sendData();
			}

			if (running) {
				float diff = targetAngle - angle;
				float degreesPerTick = Math.abs(motorSpeed) * 360f / 1200f;
				if (Math.abs(diff) > 0.001f && degreesPerTick > 0f) {
					angle += Math.signum(diff) * Math.min(Math.abs(diff), degreesPerTick);
					applyRotation();
					sendData();
				}
				RotationMode mode = movementMode.get();
				if (mode == RotationMode.ROTATE_PLACE) {
					if (Math.abs(angle - maxAngle.getAngle()) < 0.1f
							|| Math.abs(angle - minAngle.getAngle()) < 0.1f) {
						angle = Math.round(angle / 90f) * 90f;
						applyRotation();
						disassemble();
					}
				} else if (mode == RotationMode.ROTATE_PLACE_RETURNED) {
					if (Math.abs(angle) < 0.1f)
						disassemble();
				}
			}
		}
	}

	@Override
	public void write(ValueOutput view, boolean clientPacket) {
		super.write(view, clientPacket);
		if (!clientPacket)
			energy.write(view);
		view.putBoolean("servo_active", active);
		view.putFloat("TargetAngle", targetAngle);
	}

	@Override
	protected void read(ValueInput view, boolean clientPacket) {
		super.read(view, clientPacket);
		if (!clientPacket)
			energy.read(view);
		active = view.getBooleanOr("servo_active", false);
		targetAngle = view.getFloatOr("TargetAngle", targetAngle);
		if (clientPacket && running)
			angle = view.getFloatOr("Angle", angle);
	}

	public void triggerAssemble() {
		allowAssemble = true;
		assembleNextTick = true;
	}

	public float getRPM() {
		return motorSpeed;
	}

	public void setRPM(float rpm) {
		int range = CACommonConfig.COMMON.ELECTRIC_MOTOR_RPM_RANGE.get();
		int clamped = (int) Math.max(-range, Math.min(range, rpm));
		generatedSpeed.setValue(clamped);
		motorSpeed = clamped;
		updateGeneratedRotation();
		sendData();
	}

	public float getCurrentAngle() {
		return angle;
	}

	public float getTargetAngle() {
		return targetAngle;
	}

	public float getMinAngleDegrees() {
		return minAngle.getAngle();
	}

	public void setMinAngleDegrees(int degrees) {
		minAngle.setValue(Math.abs(degrees));
	}

	public float getMaxAngleDegrees() {
		return maxAngle.getAngle();
	}

	public void setMaxAngleDegrees(int degrees) {
		maxAngle.setValue(Math.abs(degrees));
	}

	public int getEnergyConsumption() {
		return ElectricMotorBlockEntity.getEnergyConsumptionRate(motorSpeed);
	}
}
