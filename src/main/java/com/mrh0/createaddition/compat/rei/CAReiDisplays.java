package com.mrh0.createaddition.compat.rei;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import dev.chaevsfe.createreiviewer.api.CreateReiApi;
import dev.chaevsfe.createreiviewer.api.CreateReiDisplayBuilder;
import dev.chaevsfe.createreiviewer.display.CreateReiDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class CAReiDisplays {
    private static final Logger REPORT_LOGGER = LoggerFactory.getLogger(CreateAddition.MODID);

    private CAReiDisplays() {
    }

    public static void register(ServerDisplayRegistry registry) {
        CreateReiApi.fill(registry, ChargingRecipe.class, CARecipes.CHARGING_TYPE, CAReiDisplays::charging);
        CreateReiApi.fill(registry, RollingRecipe.class, CARecipes.ROLLING_TYPE, CAReiDisplays::rolling);
        CreateReiApi.fill(registry, LiquidBurningRecipe.class, CARecipes.LIQUID_BURNING_TYPE, CAReiDisplays::liquidBurning);
        CreateAddition.LOGGER.info("Recipe fillers registered for {} Crafts & Additions categories", CAReiCategories.ALL.size());
    }

    public static void report(PluginManager<REICommonPlugin> manager, ReloadStage stage) {
        CreateReiApi.report(manager, stage, "Crafts & Additions", CAReiCategories.ALL, REPORT_LOGGER);
    }

    private static CreateReiDisplay charging(RecipeHolder<ChargingRecipe> holder) {
        ChargingRecipe recipe = holder.value();
        return CreateReiDisplayBuilder.of(CAReiCategories.CHARGING)
            .input(recipe.ingredient())
            .results(recipe.results())
            .duration(recipe.energy())
            .location(holder)
            .build();
    }

    private static CreateReiDisplay rolling(RecipeHolder<RollingRecipe> holder) {
        RollingRecipe recipe = holder.value();
        return CreateReiDisplayBuilder.of(CAReiCategories.ROLLING)
            .input(recipe.ingredient())
            .results(recipe.results())
            .location(holder)
            .build();
    }

    private static CreateReiDisplay liquidBurning(RecipeHolder<LiquidBurningRecipe> holder) {
        LiquidBurningRecipe recipe = holder.value();
        return CreateReiDisplayBuilder.of(CAReiCategories.LIQUID_BURNING)
            .input(EntryIngredient.of(EntryStacks.of(CAItems.STRAW)))
            .input(buckets(recipe))
            .fluidInputs(List.of(recipe.fluidIngredient()))
            .duration(recipe.burnTime())
            .heat(recipe.superheated() ? HeatCondition.SUPERHEATED : HeatCondition.HEATED)
            .location(holder)
            .build();
    }

    private static EntryIngredient buckets(LiquidBurningRecipe recipe) {
        List<EntryStack<ItemStack>> stacks = new ArrayList<>();
        for (Fluid fluid : recipe.fluidIngredient().getMatchingFluids()) {
            ItemStack bucket = new ItemStack(fluid.getBucket());
            if (!bucket.isEmpty() && !bucket.is(Items.AIR)) {
                stacks.add(EntryStacks.of(bucket));
            }
        }
        return EntryIngredient.of(stacks);
    }
}
