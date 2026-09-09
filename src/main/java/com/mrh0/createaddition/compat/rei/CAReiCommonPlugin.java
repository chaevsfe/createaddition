package com.mrh0.createaddition.compat.rei;

import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;

public class CAReiCommonPlugin implements REICommonPlugin {
    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        if (!CAReiSupport.available()) {
            return;
        }
        CAReiDisplays.register(registry);
    }

    @Override
    public void postStage(PluginManager<REICommonPlugin> manager, ReloadStage stage) {
        if (!CAReiSupport.available()) {
            return;
        }
        CAReiDisplays.report(manager, stage);
    }
}
