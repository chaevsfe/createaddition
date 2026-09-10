package com.mrh0.createaddition.compat.rei;

import dev.chaevsfe.createreiviewer.api.CreateReiApi;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;

public class CAReiClientPlugin implements REIClientPlugin {
    @Override
    public double getPriority() {
        return CreateReiApi.PLUGIN_PRIORITY;
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        if (!CAReiSupport.available()) {
            return;
        }
        CAReiClientCategories.register(registry);
    }
}
