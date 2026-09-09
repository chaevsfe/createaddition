package com.mrh0.createaddition.compat.rei;

import net.fabricmc.loader.api.FabricLoader;

public final class CAReiSupport {
    public static final String VIEWER_MOD_ID = "createreiviewer";
    public static final String REI_MOD_ID = "roughlyenoughitems";

    private CAReiSupport() {
    }

    public static boolean available() {
        FabricLoader loader = FabricLoader.getInstance();
        return loader.isModLoaded(VIEWER_MOD_ID) && loader.isModLoaded(REI_MOD_ID);
    }
}
