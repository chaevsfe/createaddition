package com.mrh0.createaddition.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;

public class CAReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        if (!CAReiSupport.available()) {
            return;
        }
        CAReiClientCategories.register(registry);
    }
}
