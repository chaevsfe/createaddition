package com.mrh0.createaddition;

import com.mrh0.createaddition.config.CACommonConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateAddition implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "createaddition";

    public static boolean CC_ACTIVE = false;

    @Override
    public void onInitialize() {
        CACommonConfig.register();
        CC_ACTIVE = FabricLoader.getInstance().isModLoaded("computercraft");
        LOGGER.info("Create Crafts & Additions Initialized!");
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
