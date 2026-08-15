package com.mrh0.createaddition.transfer;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public interface EnergyTransferable {
	@Nullable
	EnergyStorage getEnergyStorage(@Nullable Direction direction);
}
