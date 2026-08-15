package com.mrh0.createaddition.blocks.creative_energy;

import java.util.EnumMap;

import com.mrh0.createaddition.energy.CreativeEnergyStorage;
import com.mrh0.createaddition.transfer.EnergyTransferable;
import com.zurrtum.create.content.logistics.crate.CrateBlockEntity;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class CreativeEnergyBlockEntity extends CrateBlockEntity implements EnergyTransferable {

	protected final CreativeEnergyStorage energy;
	private final EnumMap<Direction, BlockApiCache<EnergyStorage, Direction>> escacheMap = new EnumMap<>(Direction.class);

	public CreativeEnergyBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		energy = new CreativeEnergyStorage();
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null) return;
		if (level.isClientSide()) return;

		for (Direction d : Direction.values()) {
			EnergyStorage ies = getCachedEnergy(d);
			if (ies == null) continue;
			try(Transaction t = Transaction.openOuter()) {
				ies.insert(Integer.MAX_VALUE, t);
				t.commit();
			}
		}
	}

	@Nullable
	public EnergyStorage getCachedEnergy(Direction side) {
		if(!(getLevel() instanceof ServerLevel serverLevel))
			return null;
		BlockApiCache<EnergyStorage, Direction> cache = escacheMap.computeIfAbsent(side,
				side1 -> BlockApiCache.create(EnergyStorage.SIDED, serverLevel, getBlockPos().relative(side1)));
		return cache.find(side.getOpposite());
	}

	@Override
	public EnergyStorage getEnergyStorage(@Nullable Direction direction) {
		return energy;
	}
}
