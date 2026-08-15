package com.mrh0.createaddition.compat.jei;

import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

import java.util.List;

public class RollingMillCategory extends CARecipeCategory<RecipeHolder<RollingRecipe>> {

	public RollingMillCategory() {
		super("rolling");
	}

	public static List<RecipeHolder<RollingRecipe>> getRecipes(RecipeMap preparedRecipes) {
		return List.copyOf(preparedRecipes.byType(CARecipes.ROLLING_TYPE));
	}

	@Override
	public IRecipeType<RecipeHolder<RollingRecipe>> getRecipeType() {
		return CreateAdditionJEI.ROLLING;
	}

	@Override
	public IDrawable getIcon() {
		return new IconRenderer(CABlocks.ROLLING_MILL.asItem());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<RollingRecipe> entry, IFocusGroup focuses) {
		RollingRecipe recipe = entry.value();
		builder.addInputSlot(15, 9).setBackground(SLOT, -1, -1).add(recipe.ingredient());
		List<ProcessingOutput> results = recipe.results();
		int size = results.size();
		if (size == 1) {
			addChanceSlot(builder, 139, 27, results.getFirst());
		} else {
			for (int i = 0; i < size; i++) {
				addChanceSlot(builder, i % 2 == 0 ? 133 : 152, 27 + i / 2 * -19, results.get(i));
			}
		}
	}

	@Override
	public void draw(
		RecipeHolder<RollingRecipe> entry,
		IRecipeSlotsView recipeSlotsView,
		GuiGraphicsExtractor graphics,
		double mouseX,
		double mouseY
	) {
		AllGuiTextures.JEI_ARROW.render(graphics, 85, 32);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 43, 4);
		AllGuiTextures.JEI_SHADOW.render(graphics, 32, 40);
		AnimatedRollingMill.draw(graphics, 42, 19);
	}
}
