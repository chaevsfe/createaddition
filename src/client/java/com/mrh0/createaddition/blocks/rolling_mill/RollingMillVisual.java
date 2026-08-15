package com.mrh0.createaddition.blocks.rolling_mill;

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

import org.jspecify.annotations.Nullable;

public class RollingMillVisual extends KineticBlockEntityVisual<RollingMillBlockEntity> {
	protected final RotatingInstance rotatingModel1;
	protected final RotatingInstance rotatingModel2;

	public RollingMillVisual(VisualizationContext context, RollingMillBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		final Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		final Direction.Axis axis = direction.getAxis();

		var instancer = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT));

		rotatingModel1 = instancer.createInstance().rotateToFace(Direction.UP, axis);
		rotatingModel2 = instancer.createInstance().rotateToFace(Direction.UP, axis);

		rotatingModel1.setup(blockEntity, axis, blockEntity.getSpeed())
				.setPosition(getVisualPosition())
				.setChanged();

		rotatingModel2.setup(blockEntity, axis, -blockEntity.getSpeed())
				.setPosition(getVisualPosition())
				.nudge(0, 4f / 16f, 0)
				.setChanged();
	}

	@Override
	public void update(float pt) {
		final Direction direction = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		final Direction.Axis axis = direction.getAxis();
		rotatingModel1.setup(blockEntity, axis, blockEntity.getSpeed()).setChanged();
		rotatingModel2.setup(blockEntity, axis, -blockEntity.getSpeed()).setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(pos, rotatingModel1, rotatingModel2);
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		consumer.accept(rotatingModel1);
		consumer.accept(rotatingModel2);
	}

	@Override
	protected void _delete() {
		rotatingModel1.delete();
		rotatingModel2.delete();
	}
}
