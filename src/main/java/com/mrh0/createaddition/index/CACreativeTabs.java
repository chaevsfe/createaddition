package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;

import java.util.Set;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
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
							Set<Item> searchOnly = Set.of(CAItems.CAKE_BASE, CAItems.CAKE_BASE_BAKED, CAItems.BIOMASS);
							for (Item item : BuiltInRegistries.ITEM) {
								if (!CreateAddition.MODID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) continue;
								if (searchOnly.contains(item)) {
									output.accept(item, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
								} else {
									output.accept(item);
								}
							}
						}).build());
	}
}
