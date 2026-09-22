package com.mrh0.createaddition.compat.viewer;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import dev.chaevsfe.createreiviewer.api.CreateViewerPlugin;
import dev.chaevsfe.createreiviewer.api.ViewerIngredient;
import dev.chaevsfe.createreiviewer.api.ViewerRecipeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;

public class CAViewerPlugin implements CreateViewerPlugin {
    public static final Identifier CHARGING = CreateAddition.asResource("charging");
    public static final Identifier ROLLING = CreateAddition.asResource("rolling");
    public static final Identifier LIQUID_BURNING = CreateAddition.asResource("liquid_burning");

    @Override
    public void registerRecipes(ViewerRecipeRegistry registry) {
        registry.add(CHARGING, CARecipes.CHARGING_TYPE, ChargingRecipe.class, (holder, builder) -> builder
            .input(holder.value().ingredient())
            .results(holder.value().results())
            .duration(holder.value().energy())
            .build());
        registry.add(ROLLING, CARecipes.ROLLING_TYPE, RollingRecipe.class, (holder, builder) -> builder
            .input(holder.value().ingredient())
            .results(holder.value().results())
            .build());
        registry.add(LIQUID_BURNING, CARecipes.LIQUID_BURNING_TYPE, LiquidBurningRecipe.class,
            holder -> !holder.value().fluidIngredient().getMatchingFluids().isEmpty(),
            (holder, builder) -> builder
                .input(CAItems.STRAW)
                .input(ViewerIngredient.ofItems(buckets(holder.value())))
                .fluidInput(holder.value().fluidIngredient())
                .duration(holder.value().burnTime())
                .heat(holder.value().superheated() ? HeatCondition.SUPERHEATED : HeatCondition.HEATED)
                .build());
        registry.synchronize(CARecipes.CHARGING, CARecipes.ROLLING, CARecipes.LIQUID_BURNING);
    }

    private static List<ItemStack> buckets(LiquidBurningRecipe recipe) {
        List<ItemStack> buckets = new ArrayList<>();
        for (Fluid fluid : recipe.fluidIngredient().getMatchingFluids()) {
            ItemStack bucket = new ItemStack(fluid.getBucket());
            if (!bucket.isEmpty() && !bucket.is(Items.AIR)) {
                buckets.add(bucket);
            }
        }
        return buckets;
    }
}
