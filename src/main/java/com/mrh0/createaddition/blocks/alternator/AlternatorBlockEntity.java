package com.mrh0.createaddition.blocks.alternator;

import java.util.EnumMap;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.InternalEnergyStorage;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.transfer.EnergyTransferable;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

public class AlternatorBlockEntity extends KineticBlockEntity implements EnergyTransferable {

	protected final InternalEnergyStorage energy;
	private final EnumMap<Direction, BlockApiCache<EnergyStorage, Direction>> escacheMap = new EnumMap<>(Direction.class);

	public AlternatorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		energy = new InternalEnergyStorage(CACommonConfig.COMMON.ALTERNATOR_CAPACITY.get(), 0, CACommonConfig.COMMON.ALTERNATOR_MAX_OUTPUT.get());
	}

	@Override
	public float calculateStressApplied() {
		float impact = CACommonConfig.COMMON.MAX_STRESS.get()/256f;
		this.lastStressApplied = impact;
		return impact;
	}

	public boolean isEnergyInput(Direction side) {
		return false;
	}

	public boolean isEnergyOutput(Direction side) {
		return true;
	}

	@Override
	protected void read(ValueInput tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		energy.read(tag);
	}

	@Override
	protected void write(ValueOutput tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		energy.write(tag);
	}

	@Override
	public void writeSafe(ValueOutput tag) {
		super.writeSafe(tag);
		energy.write(tag);
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null) return;
		if (level.isClientSide()) return;

		if (Math.abs(getSpeed()) > 0 && isSpeedRequirementFulfilled())
			energy.internalProduceEnergy(getEnergyProductionRate((int)getSpeed()));

		for (Direction d : Direction.values()) {
			if(!isEnergyOutput(d)) continue;
			EnergyStorage ies = getCachedEnergy(d);
			if(ies == null) continue;
			try(Transaction t = Transaction.openOuter()) {
				EnergyStorageUtil.move(energy, ies, CACommonConfig.COMMON.ALTERNATOR_MAX_OUTPUT.get(), t);
				t.commit();
			}
		}
	}

	public static int getEnergyProductionRate(int rpm) {
		rpm = Math.abs(rpm);
		return (int)((double) CACommonConfig.COMMON.FE_RPM.get() * ((double)Math.abs(rpm) / 256d) * CACommonConfig.COMMON.ALTERNATOR_EFFICIENCY.getF());
	}

	@Override
	protected Block getStressConfigKey() {
		return CABlocks.ALTERNATOR;
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
