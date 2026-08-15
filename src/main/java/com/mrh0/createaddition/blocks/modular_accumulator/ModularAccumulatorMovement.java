package com.mrh0.createaddition.blocks.modular_accumulator;

import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyManager;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.nbt.CompoundTag;

public class ModularAccumulatorMovement extends MovementBehaviour {

	public static final int TICK_DELAY = 20;

	@Override
	public void tick(MovementContext context) {
		if (context.contraption.entity == null) return;
		if (context.world.isClientSide()) return;
		TemporaryData data = (TemporaryData) context.temporaryData;
		if (data == null) {
			data = new TemporaryData();
			context.temporaryData = data;

			CompoundTag nbt = context.blockEntityData;
			data.controller = nbt != null && nbt.contains("EnergyContent");
			data.tick = TICK_DELAY;
		}

		if (!data.controller) return;
		if (data.tick >= TICK_DELAY) {
			data.tick = 0;
			PortableEnergyManager.track(context);
		} else data.tick++;
	}

	@Override
	public void startMoving(MovementContext context) {
		if (context.contraption.entity == null) return;
		if (context.world.isClientSide()) return;
		if (context.blockEntityData == null || !context.blockEntityData.contains("EnergyContent")) return;
		context.temporaryData = new TemporaryData(true);

		PortableEnergyManager.track(context);
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.contraption.entity == null) return;
		if (context.world.isClientSide()) return;

		PortableEnergyManager.untrack(context);
	}

	private static class TemporaryData {

		public boolean controller = false;
		private int tick = 0;

		public TemporaryData(boolean controller) {
			this.controller = controller;
		}

		public TemporaryData() {}
	}
}
