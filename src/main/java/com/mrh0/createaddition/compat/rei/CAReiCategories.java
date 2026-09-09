package com.mrh0.createaddition.compat.rei;

import com.mrh0.createaddition.CreateAddition;
import dev.chaevsfe.createreiviewer.api.CreateReiApi;
import dev.chaevsfe.createreiviewer.display.CreateReiDisplay;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import java.util.List;

public final class CAReiCategories {
    public static final CategoryIdentifier<CreateReiDisplay> CHARGING = of("charging");
    public static final CategoryIdentifier<CreateReiDisplay> ROLLING = of("rolling");
    public static final CategoryIdentifier<CreateReiDisplay> LIQUID_BURNING = of("liquid_burning");
    public static final List<CategoryIdentifier<? extends CreateReiDisplay>> ALL = List.of(CHARGING, ROLLING, LIQUID_BURNING);

    private CAReiCategories() {
    }

    private static CategoryIdentifier<CreateReiDisplay> of(String path) {
        return CreateReiApi.category(CreateAddition.MODID, path);
    }

    public static String titleKey(String path) {
        return CreateAddition.MODID + ".recipe." + path;
    }
}
