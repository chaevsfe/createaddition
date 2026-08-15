package com.mrh0.createaddition.blocks.portable_energy_interface;

import java.util.function.Consumer;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PEIVisual extends AbstractBlockEntityVisual<PortableEnergyInterfaceBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual, ShaderLightVisual {

	private final PEIInstance instance;

	public PEIVisual(VisualizationContext visualizationContext, PortableEnergyInterfaceBlockEntity blockEntity, float partialTick) {
		super(visualizationContext, blockEntity, partialTick);

		instance = new PEIInstance(visualizationContext.instancerProvider(), blockState, getVisualPosition(), isLit());
		instance.beginFrame(blockEntity.getExtensionDistance(partialTick));
	}

	@Override
	public void setSectionCollector(SectionCollector sectionCollector) {
		switch (blockState.getValue(BlockStateProperties.FACING)) {
			case UP -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 1, 0);
			case DOWN -> setSectionCollector(sectionCollector, 0, -1, 0, 0, 0, 0);
			case NORTH -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 0, 0);
			case SOUTH -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 0, 1);
			case WEST -> setSectionCollector(sectionCollector, -1, 0, 0, 0, 0, 0);
			case EAST -> setSectionCollector(sectionCollector, 0, 0, 0, 1, 0, 0);
		}
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
}
