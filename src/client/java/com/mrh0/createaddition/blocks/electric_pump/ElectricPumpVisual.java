package com.mrh0.createaddition.blocks.electric_pump;

import java.util.function.Consumer;

import com.mrh0.createaddition.index.CAPartials;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;

import net.minecraft.core.Direction;
import org.joml.Quaternionf;

public class ElectricPumpVisual extends AbstractBlockEntityVisual<ElectricPumpBlockEntity> implements SimpleDynamicVisual {

	private final TransformedInstance[] partials = new TransformedInstance[ElectricPumpBlockEntity.PARTIAL_OFFSETS.length];

	public ElectricPumpVisual(VisualizationContext context, ElectricPumpBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		for (int i = 0; i < partials.length; i++)
			partials[i] = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(CAPartials.ELECTRIC_PUMP_PARTIAL))
					.createInstance();

		animatePartials(partialTick);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		animatePartials(ctx.partialTick());
	}

	private void animatePartials(float partialTick) {
		float renderTime = AnimationTickHolder.getRenderTime(blockEntity.getLevel());
		Direction facing = blockEntity.getBlockState().getValue(ElectricPumpBlock.FACING);
		Quaternionf rotation = ElectricPumpBlockEntity.getFacingRotation(facing);

		for (int i = 0; i < partials.length; i++) {
			float[] offset = ElectricPumpBlockEntity.PARTIAL_OFFSETS[i];
			float scale = ElectricPumpBlockEntity.getPulseScale(blockEntity.isActive(), blockEntity.getPumpSpeed(),
					renderTime, ElectricPumpBlockEntity.PARTIAL_PHASE_OFFSETS[i]);
			partials[i].setIdentityTransform()
					.translate(getVisualPosition())
					.rotateCentered(rotation)
					.translate(offset[0], offset[1], offset[2])
					.scale(scale)
					.setChanged();
		}
	}

	@Override
	public void updateLight(float partialTick) {
		relight(partials);
	}

	@Override
	protected void _delete() {
		for (TransformedInstance partial : partials)
			partial.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		for (TransformedInstance partial : partials)
			consumer.accept(partial);
	}
}
