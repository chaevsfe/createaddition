package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CACreativeTabs {

	public static final ResourceKey<CreativeModeTab> MAIN_TAB = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB,
			CreateAddition.asResource(CreateAddition.MODID));

	public static void register() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MAIN_TAB,
				FabricCreativeModeTab.builder().icon(() -> new ItemStack(CABlocks.ELECTRIC_MOTOR))
						.title(Component.translatable("itemGroup.createaddition.main"))
						.displayItems((itemDisplayParameters, output) -> {
							output.accept(CAItems.CAPACITOR);
							output.accept(CAItems.DIAMOND_GRIT);
							output.accept(CAItems.DIAMOND_GRIT_SANDPAPER);
							output.accept(CAItems.BIOMASS, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
							output.accept(CAItems.BIOMASS_PELLET);
							output.accept(CAItems.ELECTRUM_AMULET);
							output.accept(CAItems.ELECTRUM_INGOT);
							output.accept(CAItems.ELECTRUM_NUGGET);
							output.accept(CAItems.ELECTRUM_SHEET);
							output.accept(CAItems.ZINC_SHEET);
							output.accept(CAItems.COPPER_WIRE);
							output.accept(CAItems.IRON_WIRE);
							output.accept(CAItems.GOLD_WIRE);
							output.accept(CAItems.ELECTRUM_WIRE);
							output.accept(CAItems.SPOOL);
							output.accept(CAItems.COPPER_SPOOL);
							output.accept(CAItems.GOLD_SPOOL);
							output.accept(CAItems.ELECTRUM_SPOOL);
							output.accept(CAItems.FESTIVE_SPOOL);
							output.accept(CAItems.COPPER_ROD);
							output.accept(CAItems.IRON_ROD);
							output.accept(CAItems.GOLD_ROD);
							output.accept(CAItems.ELECTRUM_ROD);
							output.accept(CAItems.BRASS_ROD);
							output.accept(CAItems.CAKE_BASE, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
							output.accept(CAItems.CAKE_BASE_BAKED, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
							output.accept(CAItems.STRAW);
							output.accept(CABlocks.ELECTRIC_MOTOR);
							output.accept(CABlocks.SERVO_MOTOR);
							output.accept(CABlocks.ALTERNATOR);
							output.accept(CABlocks.ROLLING_MILL);
							output.accept(CABlocks.CREATIVE_ENERGY);
							output.accept(CABlocks.SMALL_CONNECTOR);
							output.accept(CABlocks.SMALL_LIGHT_CONNECTOR);
							output.accept(CABlocks.LARGE_CONNECTOR);
							output.accept(CABlocks.REDSTONE_RELAY);
							output.accept(CABlocks.CHOCOLATE_CAKE);
							output.accept(CABlocks.HONEY_CAKE);
							output.accept(CABlocks.BARBED_WIRE);
							output.accept(CABlocks.TESLA_COIL);
							output.accept(CABlocks.MODULAR_ACCUMULATOR);
							output.accept(CABlocks.PORTABLE_ENERGY_INTERFACE);
							output.accept(CABlocks.BIOMASS_PALLET);
							output.accept(CABlocks.ELECTRUM_BLOCK);
							output.accept(CABlocks.DIGITAL_ADAPTER);
							output.accept(CAFluids.SEED_OIL_ENTRY.bucket);
							output.accept(CAFluids.BIOETHANOL_ENTRY.bucket);
						}).build());
	}
}
