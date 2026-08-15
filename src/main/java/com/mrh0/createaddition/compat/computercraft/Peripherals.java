package com.mrh0.createaddition.compat.computercraft;

import com.mrh0.createaddition.index.CABlockEntities;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class Peripherals {
	private static final Map<BlockEntity, WeakReference<IPeripheral>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

	public static void register() {
		register(CABlockEntities.ELECTRIC_MOTOR, be -> new ElectricMotorPeripheral("electric_motor", be));
		register(CABlockEntities.SERVO_MOTOR, ServoMotorPeripheral::new);
		register(CABlockEntities.PORTABLE_ENERGY_INTERFACE, be -> new PortableEnergyInterfacePeripheral("portable_energy_interface", be));
		register(CABlockEntities.MODULAR_ACCUMULATOR, be -> new ModularAccumulatorPeripheral("modular_accumulator", be));
		register(CABlockEntities.REDSTONE_RELAY, be -> new RedstoneRelayPeripheral("redstone_relay", be));
		register(CABlockEntities.DIGITAL_ADAPTER, be -> new DigitalAdapterPeripheral("digital_adapter", be));
	}

	private static <T extends BlockEntity> void register(BlockEntityType<T> type, Function<T, IPeripheral> factory) {
		PeripheralLookup.get().registerForBlockEntity((blockEntity, direction) -> {
			WeakReference<IPeripheral> reference = CACHE.get(blockEntity);
			IPeripheral peripheral = reference == null ? null : reference.get();
			if (peripheral == null) {
				peripheral = factory.apply(blockEntity);
				CACHE.put(blockEntity, new WeakReference<>(peripheral));
			}
			return peripheral;
		}, type);
	}
}
