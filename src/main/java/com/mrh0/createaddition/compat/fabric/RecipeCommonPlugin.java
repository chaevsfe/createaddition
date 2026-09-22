package com.mrh0.createaddition.compat.fabric;

import com.mrh0.createaddition.index.CARecipes;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;

public class RecipeCommonPlugin {
    public static void register() {
        RecipeSynchronization.synchronizeRecipeSerializer(CARecipes.CHARGING);
        RecipeSynchronization.synchronizeRecipeSerializer(CARecipes.ROLLING);
        RecipeSynchronization.synchronizeRecipeSerializer(CARecipes.LIQUID_BURNING);
    }
}
