package com.mrh0.createaddition.compat.jei;

import com.mrh0.createaddition.index.CABlocks;
import com.zurrtum.create.client.foundation.gui.render.ManualBlockRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

public final class AnimatedRollingMill {

	public static final int WIDTH = 27;

	private AnimatedRollingMill() {
	}

	public static void draw(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.guiRenderState.addPicturesInPictureState(new ManualBlockRenderState(
			new Matrix3x2f(graphics.pose()),
			CABlocks.ROLLING_MILL.defaultBlockState(),
			x,
			y
		));
	}
}
