package com.mrh0.createaddition.item;

import com.mrh0.createaddition.CreateAddition;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public class BiomassPelletItem extends Item {
	public static final ResourceKey<ContextIntProvider> BURN_TIME = ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, CreateAddition.asResource("cooking/time_biomass_pellet"));
	public static final ResourceKey<ContextIntProvider> BLOCK_BURN_TIME = ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, CreateAddition.asResource("cooking/time_biomass_pellet_block"));

	public BiomassPelletItem(Properties props) {
		super(props);
	}
}
