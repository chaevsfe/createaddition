package com.mrh0.createaddition.blocks.electric_motor;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ElectricMotorVisual extends OrientedRotatingVisual<ElectricMotorBlockEntity> {

	public ElectricMotorVisual(VisualizationContext context, ElectricMotorBlockEntity blockEntity, float partialTick) {
		super(
			context,
			blockEntity,
			partialTick,
			Direction.SOUTH,
			blockEntity.getBlockState().getValue(BlockStateProperties.FACING),
			Models.chunkPartial(AllPartialModels.SHAFT_HALF));
	}
}
