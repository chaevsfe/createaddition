package com.mrh0.createaddition.compat.rei;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.compat.rei.category.ChargingReiCategory;
import com.mrh0.createaddition.compat.rei.category.LiquidBurningReiCategory;
import com.mrh0.createaddition.compat.rei.category.RollingReiCategory;
import com.mrh0.createaddition.index.CABlocks;
import com.zurrtum.create.AllItems;
import dev.chaevsfe.createreiviewer.api.CreateReiClientReport;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;

public final class CAReiClientCategories {
    private CAReiClientCategories() {
    }

    public static void register(CategoryRegistry registry) {
        registry.add(new ChargingReiCategory(), new RollingReiCategory(), new LiquidBurningReiCategory());
        registry.addWorkstations(CAReiCategories.CHARGING, EntryStacks.of(CABlocks.TESLA_COIL));
        registry.addWorkstations(CAReiCategories.ROLLING, EntryStacks.of(CABlocks.ROLLING_MILL));
        registry.addWorkstations(CAReiCategories.LIQUID_BURNING, EntryStacks.of(AllItems.BLAZE_BURNER));
        CreateReiClientReport.register("Crafts & Additions", CAReiCategories.ALL);
        CreateAddition.LOGGER.info("Registered {} Crafts & Additions REI categories", CAReiCategories.ALL.size());
    }
}
