package com.mrh0.createaddition.compat.jei;

import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.material.Fluid;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class LiquidBurningCategory extends CARecipeCategory<RecipeHolder<LiquidBurningRecipe>> {

	private static final int TEXT_COLOR = 0xFFFFFFFF;

	public LiquidBurningCategory() {
		super("liquid_burning");
	}

	public static List<RecipeHolder<LiquidBurningRecipe>> getRecipes(RecipeMap preparedRecipes) {
		return List.copyOf(preparedRecipes.byType(CARecipes.LIQUID_BURNING_TYPE));
	}

	@Override
	public IRecipeType<RecipeHolder<LiquidBurningRecipe>> getRecipeType() {
		return CreateAdditionJEI.LIQUID_BURNING;
	}

	@Override
	public IDrawable getIcon() {
		return new TwoIconRenderer(AllItems.BLAZE_BURNER, CAItems.STRAW);
	}

	@Override
	public void setRecipe(
		IRecipeLayoutBuilder builder,
		RecipeHolder<LiquidBurningRecipe> entry,
		IFocusGroup focuses
	) {
		LiquidBurningRecipe recipe = entry.value();
		builder.addInputSlot(32, 3).setBackground(SLOT, -1, -1).add(CAItems.STRAW);
		builder.addInputSlot(52, 3).setBackground(SLOT, -1, -1).addItemStacks(getBuckets(recipe));
		addFluidSlot(builder, 72, 3, recipe.fluidIngredient());
	}

	private static List<ItemStack> getBuckets(LiquidBurningRecipe recipe) {
		List<ItemStack> buckets = new ArrayList<>();
		for (Fluid fluid : recipe.fluidIngredient().getMatchingFluids()) {
			ItemStack bucket = new ItemStack(fluid.getBucket());
			if (bucket.isEmpty() || bucket.is(Items.AIR)) {
				continue;
			}
			buckets.add(bucket);
		}
		return buckets;
	}

	@Override
	public void draw(
		RecipeHolder<LiquidBurningRecipe> entry,
		IRecipeSlotsView recipeSlotsView,
		GuiGraphicsExtractor graphics,
		double mouseX,
		double mouseY
	) {
		LiquidBurningRecipe recipe = entry.value();
		HeatCondition requiredHeat = recipe.superheated() ? HeatCondition.SUPERHEATED : HeatCondition.HEATED;

		AllGuiTextures.JEI_HEAT_BAR.render(graphics, 4, 30);
		AllGuiTextures.JEI_LIGHT.render(graphics, 81, 38);
		graphics.guiRenderState.addPicturesInPictureState(new BasinBlazeBurnerRenderState(
			new Matrix3x2f(graphics.pose()),
			91,
			14,
			requiredHeat.visualizeAsBlazeBurner()
		));

		graphics.text(
			Minecraft.getInstance().font,
			Component.translatable(requiredHeat.getTranslationKey()),
			9,
			36,
			requiredHeat.getColor(),
			false
		);
		graphics.text(Minecraft.getInstance().font, Util.formatTime(recipe.burnTime()), 136, 36, TEXT_COLOR, false);
	}
}
