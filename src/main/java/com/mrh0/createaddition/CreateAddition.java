package com.mrh0.createaddition;

import com.mrh0.createaddition.compat.computercraft.Peripherals;
import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.event.GameEvents;
import com.mrh0.createaddition.commands.CCApiCommand;
import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.index.CACreativeTabs;
import com.mrh0.createaddition.index.CADamageTypes;
import com.mrh0.createaddition.index.CAEffects;
import com.mrh0.createaddition.index.CAFluids;
import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.index.CARegistration;
import com.mrh0.createaddition.index.CASounds;
import com.mrh0.createaddition.index.CATransfer;
import com.mrh0.createaddition.network.CANetwork;
import com.mrh0.createaddition.trains.schedule.CASchedule;
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
        CreateAdditionPlugin.verifyEarlyRegistrationComplete();
        CAItems.register();
        CAFluids.registerItems();
        CACreativeTabs.register();
        CABlockEntities.register();
        CAEffects.register();
        CASounds.register();
        CADamageTypes.register();
        CARecipes.register();
        CATransfer.register();
        CARegistration.register();
        CASchedule.register();
        CANetwork.register();
        GameEvents.initCommon();
        if (CC_ACTIVE) Peripherals.register();
        CCApiCommand.register();
        LOGGER.info("Create Crafts & Additions Initialized!");
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
