package com.mrh0.createaddition.compat.jei;

import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

import java.util.List;

public class ChargingCategory extends CARecipeCategory<RecipeHolder<ChargingRecipe>> {

	private static final int TEXT_COLOR = 0xFFFFFFFF;

	public ChargingCategory() {
		super("charging");
	}

	public static List<RecipeHolder<ChargingRecipe>> getRecipes(RecipeMap preparedRecipes) {
		return List.copyOf(preparedRecipes.byType(CARecipes.CHARGING_TYPE));
	}

	@Override
	public IRecipeType<RecipeHolder<ChargingRecipe>> getRecipeType() {
		return CreateAdditionJEI.CHARGING;
	}

	@Override
	public IDrawable getIcon() {
		return new IconRenderer(CABlocks.TESLA_COIL.asItem());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ChargingRecipe> entry, IFocusGroup focuses) {
		ChargingRecipe recipe = entry.value();
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
		RecipeHolder<ChargingRecipe> entry,
		IRecipeSlotsView recipeSlotsView,
		GuiGraphicsExtractor graphics,
		double mouseX,
		double mouseY
	) {
		AllGuiTextures.JEI_ARROW.render(graphics, 85, 32);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 43, 4);
		AllGuiTextures.JEI_SHADOW.render(graphics, 32, 40);
		AnimatedTeslaCoil.draw(graphics, 42, 19);

		graphics.text(Minecraft.getInstance().font, formatEnergy(entry.value().energy()), 86, 9, TEXT_COLOR, false);
	}

	private static String formatEnergy(int energy) {
		String formatted = Util.format(energy);
		return (formatted == null ? String.valueOf(energy) : formatted) + "⚡";
	}
}
