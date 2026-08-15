package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.ponder.PonderScenes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class CAPonders {
	public static final Identifier ELECTRIC = CreateAddition.asResource("electric");

	public static void registerTags(PonderTagRegistrationHelper<Identifier> helper) {
		PonderTagRegistrationHelper<Item> HELPER = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);

		HELPER.registerTag(ELECTRIC)
				.addToIndex()
				.item(CABlocks.ELECTRIC_MOTOR, true, false)
				.title("Electric Blocks")
				.description("Components which use electricity")
				.register();

		HELPER.addToTag(AllCreatePonderTags.KINETIC_SOURCES)
				.add(CABlocks.ELECTRIC_MOTOR.asItem());

		HELPER.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
				.add(CABlocks.ROLLING_MILL.asItem())
				.add(CABlocks.ALTERNATOR.asItem());

		HELPER.addToTag(AllCreatePonderTags.FLUIDS)
				.add(CAItems.STRAW);

		HELPER.addToTag(AllCreatePonderTags.CONTRAPTION_ACTOR)
				.add(CABlocks.PORTABLE_ENERGY_INTERFACE.asItem());

		HELPER.addToTag(ELECTRIC)
				.add(CABlocks.ELECTRIC_MOTOR.asItem())
				.add(CABlocks.ALTERNATOR.asItem())
				.add(CABlocks.TESLA_COIL.asItem())
				.add(CABlocks.MODULAR_ACCUMULATOR.asItem())
				.add(CABlocks.PORTABLE_ENERGY_INTERFACE.asItem());
	}

	public static void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
		PonderSceneRegistrationHelper<Item> HELPER = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);

		HELPER.addStoryBoard(CABlocks.ELECTRIC_MOTOR.asItem(), "electric_motor", PonderScenes::electricMotor, AllCreatePonderTags.KINETIC_SOURCES, ELECTRIC);
		HELPER.addStoryBoard(CABlocks.ALTERNATOR.asItem(), "alternator", PonderScenes::alternator, AllCreatePonderTags.KINETIC_APPLIANCES, ELECTRIC);
		HELPER.addStoryBoard(CABlocks.ROLLING_MILL.asItem(), "rolling_mill", PonderScenes::rollingMill, AllCreatePonderTags.KINETIC_APPLIANCES);
		HELPER.addStoryBoard(CABlocks.ROLLING_MILL.asItem(), "automate_rolling_mill", PonderScenes::automateRollingMill, AllCreatePonderTags.KINETIC_APPLIANCES);
		HELPER.addStoryBoard(CABlocks.TESLA_COIL.asItem(), "tesla_coil", PonderScenes::teslaCoil, ELECTRIC);
		HELPER.addStoryBoard(CABlocks.TESLA_COIL.asItem(), "tesla_coil_hurt", PonderScenes::teslaCoilHurt, ELECTRIC);
		HELPER.addStoryBoard(CAItems.STRAW, "liquid_blaze_burner", PonderScenes::liquidBlazeBurner, AllCreatePonderTags.FLUIDS);
		HELPER.addStoryBoard(AllItems.BLAZE_BURNER, "liquid_blaze_burner", PonderScenes::liquidBlazeBurner, AllCreatePonderTags.LOGISTICS);
		HELPER.addStoryBoard(CABlocks.MODULAR_ACCUMULATOR.asItem(), "accumulator", PonderScenes::modularAccumulator, ELECTRIC);
		HELPER.addStoryBoard(CABlocks.PORTABLE_ENERGY_INTERFACE.asItem(), "pei_transfer", PonderScenes::peiTransfer, AllCreatePonderTags.CONTRAPTION_ACTOR, ELECTRIC);
		HELPER.addStoryBoard(CABlocks.PORTABLE_ENERGY_INTERFACE.asItem(), "pei_redstone", PonderScenes::peiRedstone, AllCreatePonderTags.CONTRAPTION_ACTOR, ELECTRIC);

		if (CreateAddition.CC_ACTIVE)
			HELPER.addStoryBoard(CABlocks.ELECTRIC_MOTOR.asItem(), "cc_electric_motor", PonderScenes::ccMotor, AllCreatePonderTags.KINETIC_SOURCES, ELECTRIC);
	}
}
