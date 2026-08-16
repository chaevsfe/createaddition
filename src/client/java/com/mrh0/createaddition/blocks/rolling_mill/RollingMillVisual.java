package com.mrh0.createaddition.blocks.rolling_mill;

import java.util.function.Consumer;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;

import net.minecraft.core.Direction;

import org.jspecify.annotations.Nullable;

public class RollingMillVisual extends KineticBlockEntityVisual<RollingMillBlockEntity> implements SimpleTickableVisual {
	private static final float ROLLER_OFFSET = 4f / 16f;

	protected final RotatingInstance rotatingModel1;
	protected final RotatingInstance rotatingModel2;

	public RollingMillVisual(VisualizationContext context, RollingMillBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		Direction.Axis axis = rotationAxis();

		var instancer = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.chunkPartial(AllPartialModels.SHAFT));

		rotatingModel1 = instancer.createInstance()
				.rotateToFace(Direction.UP, axis)
				.setup(blockEntity, axis, blockEntity.getSpeed())
				.setPosition(getVisualPosition());
		rotatingModel1.setChanged();

		rotatingModel2 = instancer.createInstance()
				.rotateToFace(Direction.UP, axis)
				.setup(blockEntity, axis, -blockEntity.getSpeed())
				.setPosition(getVisualPosition())
				.nudge(0, ROLLER_OFFSET, 0);
		rotatingModel2.setChanged();
	}

	@Override
	public void update(float pt) {
		Direction.Axis axis = rotationAxis();
		rotatingModel1.setup(blockEntity, axis, blockEntity.getSpeed()).setChanged();
		rotatingModel2.setup(blockEntity, axis, -blockEntity.getSpeed()).setPosition(getVisualPosition())
				.nudge(0, ROLLER_OFFSET, 0).setChanged();
	}

	@Override
	public void tick(TickableVisual.Context context) {
		applyOverstressEffect(blockEntity, rotatingModel1, rotatingModel2);
	}

	@Override
	public void updateLight(float partialTick) {
		relight(rotatingModel1, rotatingModel2);
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
