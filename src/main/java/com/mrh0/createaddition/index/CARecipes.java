package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class CARecipes {
    public static final RecipeType<RollingRecipe> ROLLING_TYPE = registerType("rolling");
    public static final RecipeType<ChargingRecipe> CHARGING_TYPE = registerType("charging");
    public static final RecipeType<LiquidBurningRecipe> LIQUID_BURNING_TYPE = registerType("liquid_burning");

    public static final RecipeSerializer<RollingRecipe> ROLLING = registerSerializer("rolling", RollingRecipe.SERIALIZER);
    public static final RecipeSerializer<ChargingRecipe> CHARGING = registerSerializer("charging", ChargingRecipe.SERIALIZER);
    public static final RecipeSerializer<LiquidBurningRecipe> LIQUID_BURNING = registerSerializer("liquid_burning", LiquidBurningRecipe.SERIALIZER);

    private static <T extends Recipe<?>> RecipeType<T> registerType(String name) {
        Identifier id = CreateAddition.asResource(name);
        return Registry.register(
            BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
                public String toString() {
                    return id.toString();
                }
            }
        );
    }

    private static <T extends Recipe<?>> RecipeSerializer<T> registerSerializer(String name, RecipeSerializer<T> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CreateAddition.asResource(name), serializer);
    }

    public static void register() {}
}
