package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.alternator.AlternatorBlockEntity;
import com.mrh0.createaddition.blocks.connector.LargeConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.SmallConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.SmallLightConnectorBlockEntity;
import com.mrh0.createaddition.blocks.creative_energy.CreativeEnergyBlockEntity;
import com.mrh0.createaddition.blocks.digital_adapter.DigitalAdapterBlockEntity;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorBlockEntity;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockEntity;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.mrh0.createaddition.blocks.redstone_relay.RedstoneRelayBlockEntity;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillBlockEntity;
import com.mrh0.createaddition.blocks.servo_motor.ServoMotorBlockEntity;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlockEntity;

import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CABlockEntities {

	public static final BlockEntityType<ElectricMotorBlockEntity> ELECTRIC_MOTOR = register(
			"electric_motor",
			(pos, state) -> new ElectricMotorBlockEntity(CABlockEntities.ELECTRIC_MOTOR, pos, state),
			CABlocks.ELECTRIC_MOTOR);

	public static final BlockEntityType<ServoMotorBlockEntity> SERVO_MOTOR = register(
			"servo_motor",
			(pos, state) -> new ServoMotorBlockEntity(CABlockEntities.SERVO_MOTOR, pos, state),
			CABlocks.SERVO_MOTOR);

	public static final BlockEntityType<AlternatorBlockEntity> ALTERNATOR = register(
			"alternator",
			(pos, state) -> new AlternatorBlockEntity(CABlockEntities.ALTERNATOR, pos, state),
			CABlocks.ALTERNATOR);

	public static final BlockEntityType<RollingMillBlockEntity> ROLLING_MILL = register(
			"rolling_mill",
			(pos, state) -> new RollingMillBlockEntity(CABlockEntities.ROLLING_MILL, pos, state),
			CABlocks.ROLLING_MILL);

	public static final BlockEntityType<CreativeEnergyBlockEntity> CREATIVE_ENERGY = register(
			"creative_energy",
			(pos, state) -> new CreativeEnergyBlockEntity(CABlockEntities.CREATIVE_ENERGY, pos, state),
			CABlocks.CREATIVE_ENERGY);

	public static final BlockEntityType<SmallConnectorBlockEntity> SMALL_CONNECTOR = register(
			"connector",
			(pos, state) -> new SmallConnectorBlockEntity(CABlockEntities.SMALL_CONNECTOR, pos, state),
			CABlocks.SMALL_CONNECTOR);

	public static final BlockEntityType<SmallLightConnectorBlockEntity> SMALL_LIGHT_CONNECTOR = register(
			"small_light_connector",
			(pos, state) -> new SmallLightConnectorBlockEntity(CABlockEntities.SMALL_LIGHT_CONNECTOR, pos, state),
			CABlocks.SMALL_LIGHT_CONNECTOR);

	public static final BlockEntityType<LargeConnectorBlockEntity> LARGE_CONNECTOR = register(
			"large_connector",
			(pos, state) -> new LargeConnectorBlockEntity(CABlockEntities.LARGE_CONNECTOR, pos, state),
			CABlocks.LARGE_CONNECTOR);

	public static final BlockEntityType<RedstoneRelayBlockEntity> REDSTONE_RELAY = register(
			"redstone_relay",
			(pos, state) -> new RedstoneRelayBlockEntity(CABlockEntities.REDSTONE_RELAY, pos, state),
			CABlocks.REDSTONE_RELAY);

	public static final BlockEntityType<TeslaCoilBlockEntity> TESLA_COIL = register(
			"tesla_coil",
			(pos, state) -> new TeslaCoilBlockEntity(CABlockEntities.TESLA_COIL, pos, state),
			CABlocks.TESLA_COIL);

	public static final BlockEntityType<LiquidBlazeBurnerBlockEntity> LIQUID_BLAZE_BURNER = register(
			"liquid_blaze_burner",
			(pos, state) -> new LiquidBlazeBurnerBlockEntity(CABlockEntities.LIQUID_BLAZE_BURNER, pos, state),
			CABlocks.LIQUID_BLAZE_BURNER);

	public static final BlockEntityType<ModularAccumulatorBlockEntity> MODULAR_ACCUMULATOR = register(
			"modular_accumulator",
			(pos, state) -> new ModularAccumulatorBlockEntity(CABlockEntities.MODULAR_ACCUMULATOR, pos, state),
			CABlocks.MODULAR_ACCUMULATOR);

	public static final BlockEntityType<PortableEnergyInterfaceBlockEntity> PORTABLE_ENERGY_INTERFACE = register(
			"portable_energy_interface",
			(pos, state) -> new PortableEnergyInterfaceBlockEntity(CABlockEntities.PORTABLE_ENERGY_INTERFACE, pos, state),
			CABlocks.PORTABLE_ENERGY_INTERFACE);

	public static final BlockEntityType<DigitalAdapterBlockEntity> DIGITAL_ADAPTER = register(
			"digital_adapter",
			(pos, state) -> new DigitalAdapterBlockEntity(CABlockEntities.DIGITAL_ADAPTER, pos, state),
			CABlocks.DIGITAL_ADAPTER);

	private static <T extends BlockEntity> BlockEntityType<T> register(String id, BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
		return Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				CreateAddition.asResource(id),
				new BlockEntityType<>(factory, Set.of(blocks)));
	}

	public static void register() {
	}
}
