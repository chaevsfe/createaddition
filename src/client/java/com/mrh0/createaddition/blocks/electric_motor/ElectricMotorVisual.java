package com.mrh0.createaddition.blocks.electric_motor;

import java.util.function.Consumer;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ElectricMotorVisual extends KineticBlockEntityVisual<ElectricMotorBlockEntity> {
	protected final RotatingInstance shaft;

	public ElectricMotorVisual(VisualizationContext context, ElectricMotorBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		Direction facing = blockState.getValue(BlockStateProperties.FACING);
		var instancer = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF));

		shaft = instancer.createInstance()
				.rotateToFace(Direction.SOUTH, facing)
				.setup(blockEntity)
				.setPosition(getVisualPosition());
		shaft.setChanged();
	}

	@Override
	public void update(float pt) {
		shaft.setup(blockEntity).setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(shaft);
	}

	@Override
	protected void _delete() {
		shaft.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(shaft);
	}
}
