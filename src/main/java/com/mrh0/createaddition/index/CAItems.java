package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.item.BiomassPelletItem;
import com.mrh0.createaddition.item.CapacitorItem;
import com.mrh0.createaddition.item.DiamondGritSandpaperItem;
import com.mrh0.createaddition.item.ElectrumAmuletItem;
import com.mrh0.createaddition.item.WireSpool;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class CAItems {

	public static final CapacitorItem CAPACITOR = registerItem("capacitor", CapacitorItem::new);

	public static final Item DIAMOND_GRIT = registerItem("diamond_grit", Item::new);
	public static final DiamondGritSandpaperItem DIAMOND_GRIT_SANDPAPER = registerItem("diamond_grit_sandpaper", DiamondGritSandpaperItem::new);

	public static final Item BIOMASS = registerItem("biomass", Item::new, new Item.Properties().stacksTo(16));
	public static final BiomassPelletItem BIOMASS_PELLET = registerItem("biomass_pellet", BiomassPelletItem::new);

	public static final ElectrumAmuletItem ELECTRUM_AMULET = registerItem("electrum_amulet", ElectrumAmuletItem::new);

	public static final Item ELECTRUM_INGOT = registerItem("electrum_ingot", Item::new);
	public static final Item ELECTRUM_NUGGET = registerItem("electrum_nugget", Item::new);

	public static final Item ELECTRUM_SHEET = registerItem("electrum_sheet", Item::new);
	public static final Item ZINC_SHEET = registerItem("zinc_sheet", Item::new);

	public static final Item COPPER_WIRE = registerItem("copper_wire", Item::new);
	public static final Item IRON_WIRE = registerItem("iron_wire", Item::new);
	public static final Item GOLD_WIRE = registerItem("gold_wire", Item::new);
	public static final Item ELECTRUM_WIRE = registerItem("electrum_wire", Item::new);

	public static final WireSpool SPOOL = registerItem("spool", WireSpool::new);
	public static final WireSpool COPPER_SPOOL = registerItem("copper_spool", WireSpool::new);
	public static final WireSpool GOLD_SPOOL = registerItem("gold_spool", WireSpool::new);
	public static final WireSpool ELECTRUM_SPOOL = registerItem("electrum_spool", WireSpool::new);
	public static final WireSpool FESTIVE_SPOOL = registerItem("festive_spool", WireSpool::new);

	public static final Item COPPER_ROD = registerItem("copper_rod", Item::new);
	public static final Item IRON_ROD = registerItem("iron_rod", Item::new);
	public static final Item GOLD_ROD = registerItem("gold_rod", Item::new);
	public static final Item ELECTRUM_ROD = registerItem("electrum_rod", Item::new);
	public static final Item BRASS_ROD = registerItem("brass_rod", Item::new);

	public static final Item CAKE_BASE = registerItem("cake_base", Item::new);
	public static final Item CAKE_BASE_BAKED = registerItem("cake_base_baked", Item::new);

	public static final Item STRAW = registerItem("straw", Item::new, new Item.Properties().stacksTo(16));

	private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
		return registerItem(name, factory, new Item.Properties());
	}

	private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		Identifier id = CreateAddition.asResource(name);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		T item = factory.apply(properties.setId(key));
		Registry.register(BuiltInRegistries.ITEM, id, item);
		return item;
	}

	public static void register() {
	}
}
