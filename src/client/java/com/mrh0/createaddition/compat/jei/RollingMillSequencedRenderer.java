package com.mrh0.createaddition.compat.jei;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import com.zurrtum.create.client.compat.jei.category.SequencedAssemblyCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

import java.util.Optional;

public class RollingMillSequencedRenderer extends SequencedAssemblyCategory.SequencedRenderer<RollingRecipe> {

	@Override
	public void render(GuiGraphicsExtractor graphics, int i, int x, int y, Optional<IRecipeSlotView> slot) {
		float scale = 19f / AnimatedRollingMill.WIDTH;
		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		matrices.translate(x, y);
		matrices.scale(scale, scale);
		matrices.translate(-x, -y);
		AnimatedRollingMill.draw(graphics, x - 1, y);
		matrices.popMatrix();
	}

	@Override
	public Component getSequenceName(RollingRecipe recipe, Optional<IRecipeSlotView> slot) {
		return Component.translatable(CreateAddition.MODID + ".recipe.rolling.sequence");
	}

	@Override
	public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, RollingRecipe recipe) {
		return null;
	}
}
