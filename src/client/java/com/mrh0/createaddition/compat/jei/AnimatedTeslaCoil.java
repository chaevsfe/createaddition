package com.mrh0.createaddition.compat.jei;

import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlock;
import com.mrh0.createaddition.index.CABlocks;
import com.zurrtum.create.client.foundation.gui.render.ManualBlockRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;

public final class AnimatedTeslaCoil {

	private static final BlockState STATE = CABlocks.TESLA_COIL.defaultBlockState()
		.setValue(TeslaCoilBlock.FACING, Direction.DOWN)
		.setValue(TeslaCoilBlock.POWERED, true);

	private AnimatedTeslaCoil() {
	}

	public static void draw(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.guiRenderState.addPicturesInPictureState(new ManualBlockRenderState(
			new Matrix3x2f(graphics.pose()),
			STATE,
			x,
			y
		));
	}
}
