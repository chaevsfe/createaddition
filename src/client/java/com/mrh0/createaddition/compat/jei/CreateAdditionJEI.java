package com.mrh0.createaddition.compat.jei;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.category.SequencedAssemblyCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.common.Internal;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

public class CreateAdditionJEI implements IModPlugin {

	public static final Identifier ID = CreateAddition.asResource("jei_plugin");

	public static final IRecipeType<RecipeHolder<RollingRecipe>> ROLLING = recipeHolderType("rolling");
	public static final IRecipeType<RecipeHolder<ChargingRecipe>> CHARGING = recipeHolderType("charging");
	public static final IRecipeType<RecipeHolder<LiquidBurningRecipe>> LIQUID_BURNING = recipeHolderType("liquid_burning");

	@SuppressWarnings("unchecked")
	private static <T> IRecipeType<T> recipeHolderType(String path) {
		return (IRecipeType<T>) IRecipeType.create(CreateAddition.asResource(path), RecipeHolder.class);
	}

	@Override
	public Identifier getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(
			new ChargingCategory(),
			new RollingMillCategory(),
			new LiquidBurningCategory()
		);
		SequencedAssemblyCategory.registerRenderer(CARecipes.ROLLING_TYPE, new RollingMillSequencedRenderer());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		RecipeMap preparedRecipes = Internal.getClientSyncedRecipes();
		registration.addRecipes(CHARGING, ChargingCategory.getRecipes(preparedRecipes));
		registration.addRecipes(ROLLING, RollingMillCategory.getRecipes(preparedRecipes));
		registration.addRecipes(LIQUID_BURNING, LiquidBurningCategory.getRecipes(preparedRecipes));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addCraftingStation(CHARGING, CABlocks.TESLA_COIL);
		registration.addCraftingStation(ROLLING, CABlocks.ROLLING_MILL);
		registration.addCraftingStation(LIQUID_BURNING, AllItems.BLAZE_BURNER);
		registration.addCraftingStation(JeiClientPlugin.SANDPAPER_POLISHING, CAItems.DIAMOND_GRIT_SANDPAPER);
	}
}
