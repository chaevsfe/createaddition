package com.mrh0.createaddition.item;

import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CAItems;

import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.minecraft.world.item.Item;

public class BiomassPelletItem extends Item {
	public static final int BURN_TIME = 6400;

	static {
		FuelValueEvents.BUILD.register((builder, context) -> {
			builder.add(CAItems.BIOMASS_PELLET, BURN_TIME);
			builder.add(CABlocks.BIOMASS_PALLET, BURN_TIME * 9);
		});
	}

	public BiomassPelletItem(Properties props) {
		super(props);
	}
}
