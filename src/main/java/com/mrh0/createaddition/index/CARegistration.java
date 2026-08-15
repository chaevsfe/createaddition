package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.digital_adapter.DigitalAdapterDisplaySource;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlock;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlock;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorDisplaySource;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorMovement;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceMovement;
import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.NodeMovementBehaviour;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.boiler.BoilerHeater;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.api.contraption.BlockMovementChecks.CheckResult;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CARegistration {

	public static void register() {
		registerStressValues();
		registerMovementBehaviours();
		registerDisplaySources();
		registerArmInteractionPointTypes();
		registerBoilerHeaters();
		registerMovementChecks();
	}

	private static void registerStressValues() {
		BlockStressValues.setGeneratorSpeed(CABlocks.ELECTRIC_MOTOR, 256, true);
		BlockStressValues.setGeneratorSpeed(CABlocks.SERVO_MOTOR, 256, true);
		BlockStressValues.CAPACITIES.register(CABlocks.ELECTRIC_MOTOR, () -> CACommonConfig.COMMON.MAX_STRESS.get() / 256f);
		BlockStressValues.CAPACITIES.register(CABlocks.SERVO_MOTOR, () -> CACommonConfig.COMMON.MAX_STRESS.get() / 256f);
		BlockStressValues.IMPACTS.register(CABlocks.ALTERNATOR, () -> CACommonConfig.COMMON.MAX_STRESS.get() / 256f);
		BlockStressValues.IMPACTS.register(CABlocks.ROLLING_MILL, () -> CACommonConfig.COMMON.ROLLING_MILL_STRESS.get());
	}

	private static void registerMovementBehaviours() {
		MovementBehaviour.REGISTRY.register(CABlocks.SMALL_CONNECTOR, new NodeMovementBehaviour());
		MovementBehaviour.REGISTRY.register(CABlocks.SMALL_LIGHT_CONNECTOR, new NodeMovementBehaviour());
		MovementBehaviour.REGISTRY.register(CABlocks.LARGE_CONNECTOR, new NodeMovementBehaviour());
		MovementBehaviour.REGISTRY.register(CABlocks.REDSTONE_RELAY, new NodeMovementBehaviour());
		MovementBehaviour.REGISTRY.register(CABlocks.MODULAR_ACCUMULATOR, new ModularAccumulatorMovement());
		MovementBehaviour.REGISTRY.register(CABlocks.PORTABLE_ENERGY_INTERFACE, new PortableEnergyInterfaceMovement());
	}

	private static void registerDisplaySources() {
		ModularAccumulatorDisplaySource accumulatorSource = Registry.register(
				CreateRegistries.DISPLAY_SOURCE,
				CreateAddition.asResource("modular_accumulator"),
				new ModularAccumulatorDisplaySource());
		DisplaySource.BY_BLOCK.add(CABlocks.MODULAR_ACCUMULATOR, accumulatorSource);
		DigitalAdapterDisplaySource adapterSource = Registry.register(
				CreateRegistries.DISPLAY_SOURCE,
				CreateAddition.asResource("digital_adapter"),
				new DigitalAdapterDisplaySource());
		DisplaySource.BY_BLOCK.add(CABlocks.DIGITAL_ADAPTER, adapterSource);
	}

	private static void registerArmInteractionPointTypes() {
		Registry.register(
				CreateRegistries.ARM_INTERACTION_POINT_TYPE,
				CreateAddition.asResource("liquid_blaze_burner"),
				new LiquidBlazeBurnerType());
	}

	private static void registerBoilerHeaters() {
		BoilerHeater.REGISTRY.register(CABlocks.LIQUID_BLAZE_BURNER, (level, pos, state) -> {
			BlazeBurnerBlock.HeatLevel value = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
			if (value == BlazeBurnerBlock.HeatLevel.NONE)
				return -1;
			if (value == BlazeBurnerBlock.HeatLevel.SEETHING)
				return 2;
			if (value.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING))
				return 1;
			return 0;
		});
	}

	private static void registerMovementChecks() {
		BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
			if (state.getBlock() instanceof ModularAccumulatorBlock)
				return CheckResult.of(ConnectivityHandler.isConnected(world, pos, pos.relative(direction)));
			return CheckResult.PASS;
		});
	}

	public static class LiquidBlazeBurnerType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.is(CABlocks.LIQUID_BLAZE_BURNER);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new LiquidBlazeBurnerPoint(this, level, pos, state);
		}
	}

	public static class LiquidBlazeBurnerPoint extends AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint {
		public LiquidBlazeBurnerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
			ItemStack input = stack.copy();
			InteractionResult res = LiquidBlazeBurnerBlock.tryInsert(cachedState, level, pos, input, false, false, simulate);
			ItemStack remainder = ItemStack.EMPTY;
			if (res instanceof InteractionResult.Success success) {
				ItemStack newHandStack = success.heldItemTransformedTo();
				if (newHandStack != null && !newHandStack.isEmpty())
					remainder = newHandStack;
			}
			if (input.isEmpty())
				return remainder;
			if (!simulate)
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
			return input;
		}
	}
}
