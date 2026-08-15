package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;

import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;

import static com.zurrtum.create.client.foundation.block.connected.AllCTTypes.OMNIDIRECTIONAL;
import static com.zurrtum.create.client.foundation.block.connected.AllCTTypes.RECTANGLE;
import static com.zurrtum.create.client.foundation.block.connected.CTSpriteShifter.getCT;

public class CASpriteShifts {
	public static final CTSpriteShiftEntry
		ACCUMULATOR = getCT(
				RECTANGLE,
				CreateAddition.asResource("block/modular_accumulator/block"),
				CreateAddition.asResource("block/modular_accumulator/block_connected")
			),
		ACCUMULATOR_TOP = getCT(
				RECTANGLE,
				CreateAddition.asResource("block/modular_accumulator/block_top"),
				CreateAddition.asResource("block/modular_accumulator/block_top_connected")
			),

		COPPER_WIRE_CASING = getCT(
				OMNIDIRECTIONAL,
				CreateAddition.asResource("block/copper_wire_casing/block"),
				CreateAddition.asResource("block/copper_wire_casing/block_connected")
			);
}
