package com.mrh0.createaddition.index;

import com.mrh0.createaddition.transfer.EnergyTransferable;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedInventoryBehaviour;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class CATransfer {

	public static void register() {
		EnergyStorage.SIDED.registerForBlockEntities(
				CATransfer::findEnergy,
				CABlockEntities.ELECTRIC_MOTOR,
				CABlockEntities.SERVO_MOTOR,
				CABlockEntities.ALTERNATOR,
				CABlockEntities.CREATIVE_ENERGY,
				CABlockEntities.SMALL_CONNECTOR,
				CABlockEntities.SMALL_LIGHT_CONNECTOR,
				CABlockEntities.LARGE_CONNECTOR,
				CABlockEntities.TESLA_COIL,
				CABlockEntities.MODULAR_ACCUMULATOR,
				CABlockEntities.PORTABLE_ENERGY_INTERFACE);
		BlockEntityBehaviour.add(CABlockEntities.ROLLING_MILL, be -> new CachedInventoryBehaviour<>(be, mill -> mill.capability));
		ItemStorage.SIDED.registerForBlockEntity(CachedInventoryBehaviour::get, CABlockEntities.ROLLING_MILL);
	}

	@Nullable
	private static EnergyStorage findEnergy(BlockEntity be, @Nullable Direction direction) {
		if (be instanceof EnergyTransferable transferable)
			return transferable.getEnergyStorage(direction);
		return null;
	}
}
