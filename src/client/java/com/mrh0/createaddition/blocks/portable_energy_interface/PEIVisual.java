package com.mrh0.createaddition.blocks.portable_energy_interface;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;

import java.util.function.Consumer;

public class PEIVisual extends AbstractBlockEntityVisual<PortableEnergyInterfaceBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

	private final PEIInstance instance;

	public PEIVisual(VisualizationContext visualizationContext, PortableEnergyInterfaceBlockEntity blockEntity, float partialTick) {
		super(visualizationContext, blockEntity, partialTick);

		instance = new PEIInstance(visualizationContext.instancerProvider(), blockState, getVisualPosition(), isLit());
		instance.beginFrame(blockEntity.getExtensionDistance(partialTick));
	}

	@Override
	public void tick(TickableVisual.Context ctx) {
		instance.tick(isLit());
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		instance.beginFrame(blockEntity.getExtensionDistance(ctx.partialTick()));
	}

	@Override
	public void updateLight(float partialTick) {
		relight(instance.middle, instance.top);
	}

	@Override
	protected void _delete() {
		instance.remove();
	}

	private boolean isLit() {
		return blockEntity.isConnected();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		instance.collectCrumblingInstances(consumer);
	}
	/*
	private final PIInstance instance;

	public PortableEnergyInterfaceVisual(VisualizationContext visualizationContext, PortableEnergyInterfaceBlockEntity blockEntity, float partialTick) {
		super(visualizationContext, blockEntity, partialTick);

		instance = new PIInstance(context.instancerProvider(), movementContext.state, movementContext.localPos, false);

		instance.middle.light(localBlockLight(), 0);
		instance.top.light(localBlockLight(), 0);
	}

	@Override
	public void beginFrame() {
		LerpedFloat lf = PortableEnergyInterfaceMovement.getAnimation(context);
		instance.tick(lf.settled());
		instance.beginFrame(lf.getValue(AnimationTickHolder.getPartialTicks()));
	}

	@Override
	protected void _delete() {
		instance.remove();
	}

	public static class PIInstance {
		private final InstancerProvider instancerProvider;
		private final BlockState blockState;
		private final BlockPos instancePos;
		private final float angleX;
		private final float angleY;

		private boolean lit;
		TransformedInstance middle;
		TransformedInstance top;

		public PIInstance(InstancerProvider instancerProvider, BlockState blockState, BlockPos instancePos, boolean lit) {
			this.instancerProvider = instancerProvider;
			this.blockState = blockState;
			this.instancePos = instancePos;
			Direction facing = blockState.getValue(PortableEnergyInterfaceBlock.FACING);
			angleX = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
			angleY = AngleHelper.horizontalAngle(facing);
			this.lit = lit;

			middle = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(PortableEnergyInterfaceRenderer.getMiddleForState(blockState, lit)))
					.createInstance();
			top = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(PortableEnergyInterfaceRenderer.getTopForState(blockState)))
					.createInstance();
		}

		public void beginFrame(float progress) {
			middle.setIdentityTransform()
					.translate(instancePos)
					.center()
					.rotateYDegrees(angleY)
					.rotateXDegrees(angleX)
					.uncenter();

			top.setIdentityTransform()
					.translate(instancePos)
					.center()
					.rotateYDegrees(angleY)
					.rotateXDegrees(angleX)
					.uncenter();

			middle.translate(0, progress * 0.5f + 0.375f, 0);
			top.translate(0, progress, 0);

			middle.setChanged();
			top.setChanged();
		}

		public void tick(boolean lit) {
			if (this.lit != lit) {
				this.lit = lit;
				instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(PortableEnergyInterfaceRenderer.getMiddleForState(blockState, lit)))
						.stealInstance(middle);
			}
		}

		public void remove() {
			middle.delete();
			top.delete();
		}

		public void collectCrumblingInstances(Consumer<Instance> consumer) {
			consumer.accept(middle);
			consumer.accept(top);
		}
	}
	*/
}
