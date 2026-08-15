package com.mrh0.createaddition.energy;

import com.mrh0.createaddition.index.*;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum WireType {
	COPPER(0, 256, 78, 37, 30, new ItemStack(CAItems.COPPER_WIRE, 4), new ItemStack(CAItems.COPPER_SPOOL)),
	GOLD(1, 1024, 98, 83, 29, new ItemStack(CAItems.GOLD_WIRE, 4), new ItemStack(CAItems.GOLD_SPOOL)),
	ELECTRUM(2, 8196, 88, 66, 37, new ItemStack(CAItems.ELECTRUM_WIRE, 4), new ItemStack(CAItems.ELECTRUM_SPOOL)),
	FESTIVE(3, 256, 26, 94, 12, new ItemStack(CAItems.COPPER_WIRE, 4), new ItemStack(CAItems.FESTIVE_SPOOL));
	//IRON(4, 256, 87, 87, 87, new ItemStack(CAItems.IRON_WIRE, 4), new ItemStack(CAItems.IRON_SPOOL));

	private final int ID, TRANSFER, CR, CG, CB;
	private final ItemStack DROP;
	private final ItemStack SOURCE_DROP;

	WireType(int id, int transfer, int red, int green, int blue, ItemStack drop, ItemStack source) {
		ID = id;
		TRANSFER = transfer;
		CR = red;
		CG = green;
		CB = blue;
		DROP = drop;
		SOURCE_DROP = source;
	}

	public static WireType fromIndex(int index) {
		return switch (index) {
			case 0 -> COPPER;
			case 1 -> GOLD;
			case 2 -> ELECTRUM;
			case 3 -> FESTIVE;
			//case 4 -> IRON;
			default -> null;
		};
	}

	public int getIndex() {
		return ID;
	}

	public ItemStack getDrop() {
		return DROP.copy();
	}

	public ItemStack getSourceDrop() {
		return SOURCE_DROP.copy();
	}

	public int transfer() {
		return TRANSFER;
	}

	public int getRed() {
		return CR;
	}

	public int getGreen() {
		return CG;
	}

	public int getBlue() {
		return CB;
	}

	public boolean isFestive( ) {
		return this == FESTIVE;
	}

	public static WireType of(Item item) {
		if(item == CAItems.COPPER_SPOOL)
			return WireType.COPPER;
		if(item == CAItems.GOLD_SPOOL)
			return WireType.GOLD;
		if(item == CAItems.FESTIVE_SPOOL)
			return WireType.FESTIVE;
		//if(item == CAItems.IRON_SPOOL)
		//	return WireType.IRON;
		if(item == CAItems.ELECTRUM_SPOOL)
			return WireType.ELECTRUM;
		return WireType.COPPER;
	}
}
