package com.mrh0.createaddition.blocks.portable_energy_interface;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.transfer.EnergyTransferable;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class PortableEnergyInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity implements EnergyTransferable {

	protected final InterfaceEnergyHandler capability = new InterfaceEnergyHandler(EnergyStorage.EMPTY);

	public PortableEnergyInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
		capability.setWrapped(PortableEnergyManager.get(contraption));
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void stopTransferring() {
		capability.setWrapped(EnergyStorage.EMPTY);
		super.stopTransferring();
	}

	@Override
	public @Nullable EnergyStorage getEnergyStorage(@Nullable Direction side) {
		return capability;
	}

	public float getConnectionDistance() {
		return distance;
	}

	public @Nullable Entity getConnectedEntity() {
		return connectedEntity;
	}

	public int getTransferTimer() {
		return transferTimer;
	}

	public long getEnergy() {
		return capability.getAmount();
	}

	public long getCapacity() {
		return capability.getCapacity();
	}

	public class InterfaceEnergyHandler implements EnergyStorage {

		private EnergyStorage wrapped;

		public InterfaceEnergyHandler(EnergyStorage wrapped) {
			this.wrapped = wrapped;
		}

		public void setWrapped(@Nullable EnergyStorage wrapped) {
			this.wrapped = wrapped == null ? EnergyStorage.EMPTY : wrapped;
		}

		@Override
		public long insert(long maxAmount, TransactionContext transaction) {
			if (!PortableEnergyInterfaceBlockEntity.this.canTransfer()) return 0;
			maxAmount = Math.min(maxAmount, CACommonConfig.COMMON.PEI_MAX_INPUT.get());
			long received = wrapped.insert(maxAmount, transaction);
			if (received != 0) transaction.addOuterCloseCallback(result -> {
				if (result.wasCommitted()) keepAlive();
			});
			return received;
		}

		@Override
		public long extract(long maxAmount, TransactionContext transaction) {
			if (!PortableEnergyInterfaceBlockEntity.this.canTransfer()) return 0;
			maxAmount = Math.min(maxAmount, CACommonConfig.COMMON.PEI_MAX_OUTPUT.get());
			long extracted = wrapped.extract(maxAmount, transaction);
			if (extracted != 0) transaction.addOuterCloseCallback(result -> {
				if (result.wasCommitted()) keepAlive();
			});
			return extracted;
		}

		@Override
		public long getAmount() {
			return wrapped.getAmount();
		}

		@Override
		public long getCapacity() {
			return wrapped.getCapacity();
		}

		public void keepAlive() {
			PortableEnergyInterfaceBlockEntity.this.onContentTransferred();
		}
	}
}
