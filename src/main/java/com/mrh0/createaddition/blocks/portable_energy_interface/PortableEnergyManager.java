package com.mrh0.createaddition.blocks.portable_energy_interface;

import com.mrh0.createaddition.config.CACommonConfig;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PortableEnergyManager {

	private static final Map<UUID, EnergyStorageHolder> CONTRAPTIONS = new ConcurrentHashMap<>();

	public static void tick() {
		CONTRAPTIONS.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().heartbeat > 5_000);
	}

	public static void track(MovementContext context) {
		Contraption contraption = context.contraption;
		EnergyStorageHolder holder = CONTRAPTIONS.get(contraption.entity.getUUID());
		if (holder == null) {
			holder = new EnergyStorageHolder();
			CONTRAPTIONS.put(contraption.entity.getUUID(), holder);
		}
		holder.addEnergySource(context.blockEntityData, context.localPos);
	}

	public static void untrack(MovementContext context) {
		EnergyStorageHolder holder = CONTRAPTIONS.remove(context.contraption.entity.getUUID());
		if (holder == null) return;
		holder.removed = true;
	}

	public static @Nullable EnergyStorage get(Contraption contraption) {
		if (contraption.entity == null) return null;
		return CONTRAPTIONS.get(contraption.entity.getUUID());
	}

	public static class EnergyStorageHolder extends SnapshotParticipant<Long> implements EnergyStorage {

		private long energy = 0;
		private long committedEnergy = 0;
		private long capacity = 0;
		private long heartbeat;
		private boolean removed = false;

		private final long maxReceive = CACommonConfig.COMMON.ACCUMULATOR_MAX_INPUT.get();
		private final long maxExtract = CACommonConfig.COMMON.ACCUMULATOR_MAX_OUTPUT.get();
		private final Map<BlockPos, EnergyData> energyHolders = new HashMap<>();

		public EnergyStorageHolder() {
			this.heartbeat = System.currentTimeMillis();
		}

		protected void addEnergySource(@Nullable CompoundTag nbt, BlockPos pos) {
			this.heartbeat = System.currentTimeMillis();

			if (nbt == null || !nbt.contains("EnergyContent")) return;
			if (this.energyHolders.containsKey(pos)) return;
			EnergyData data = new EnergyData(nbt);
			this.energy += data.energy;
			this.committedEnergy += data.energy;
			this.capacity += data.capacity;
			this.energyHolders.put(pos, data);
		}

		@Override
		protected Long createSnapshot() {
			return this.energy;
		}

		@Override
		protected void readSnapshot(Long snapshot) {
			this.energy = snapshot;
		}

		@Override
		protected void onFinalCommit() {
			long delta = this.energy - this.committedEnergy;
			this.committedEnergy = this.energy;
			if (delta > 0) {
				for (EnergyData data : energyHolders.values()) {
					delta -= data.receiveEnergy(delta);
					if (delta <= 0) break;
				}
			} else if (delta < 0) {
				long left = -delta;
				for (EnergyData data : energyHolders.values()) {
					left -= data.extractEnergy(left);
					if (left <= 0) break;
				}
			}
		}

		@Override
		public long insert(long maxAmount, TransactionContext transaction) {
			if (!this.supportsInsertion()) return 0;
			long energyReceived = Math.min(this.capacity - this.energy, Math.min(this.maxReceive, maxAmount));
			if (energyReceived <= 0) return 0;
			updateSnapshots(transaction);
			this.energy += energyReceived;
			return energyReceived;
		}

		@Override
		public long extract(long maxAmount, TransactionContext transaction) {
			if (!this.supportsExtraction()) return 0;
			long energyExtracted = Math.min(this.energy, Math.min(this.maxExtract, maxAmount));
			if (energyExtracted <= 0) return 0;
			updateSnapshots(transaction);
			this.energy -= energyExtracted;
			return energyExtracted;
		}

		@Override
		public long getAmount() {
			return this.energy;
		}

		@Override
		public long getCapacity() {
			return this.capacity;
		}

		@Override
		public boolean supportsExtraction() {
			return !this.removed;
		}

		@Override
		public boolean supportsInsertion() {
			return !this.removed;
		}
	}

	public static class EnergyData {

		private final CompoundTag nbt;
		private final long capacity;
		private long energy;

		public EnergyData(CompoundTag nbt) {
			CompoundTag energyContent = nbt.getCompound("EnergyContent")
				.orElseThrow(() -> new IllegalArgumentException("EnergyContent is null"));
			this.nbt = nbt;
			this.capacity = nbt.getIntOr("EnergyCapacity", 0);
			this.energy = energyContent.getIntOr("energy", 0);
		}

		public long receiveEnergy(long energy) {
			long energyReceived = Math.min(this.capacity - this.energy, energy);
			if (energyReceived == 0) return 0;
			this.energy += energyReceived;
			save();
			return energyReceived;
		}

		public long extractEnergy(long energy) {
			long energyRemoved = Math.min(this.energy, energy);
			if (energyRemoved == 0) return 0;
			this.energy -= energyRemoved;
			save();
			return energyRemoved;
		}

		private void save() {
			nbt.getCompound("EnergyContent").ifPresent(energyContent -> energyContent.putInt("energy", (int) this.energy));
		}
	}
}
