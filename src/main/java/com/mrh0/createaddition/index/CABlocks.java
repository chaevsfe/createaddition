package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.alternator.AlternatorBlock;
import com.mrh0.createaddition.blocks.barbed_wire.BarbedWireBlock;
import com.mrh0.createaddition.blocks.cake.CACakeBlock;
import com.mrh0.createaddition.blocks.connector.LargeConnectorBlock;
import com.mrh0.createaddition.blocks.connector.SmallConnectorBlock;
import com.mrh0.createaddition.blocks.connector.SmallLightConnectorBlock;
import com.mrh0.createaddition.blocks.creative_energy.CreativeEnergyBlock;
import com.mrh0.createaddition.blocks.digital_adapter.DigitalAdapterBlock;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorBlock;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlock;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlock;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockItem;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlock;
import com.mrh0.createaddition.blocks.redstone_relay.RedstoneRelayBlock;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillBlock;
import com.mrh0.createaddition.blocks.servo_motor.ServoMotorBlock;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlock;
import com.mrh0.createaddition.item.BiomassPelletBlockItem;
import com.zurrtum.create.content.processing.AssemblyOperatorBlockItem;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class CABlocks {

	public static final ElectricMotorBlock ELECTRIC_MOTOR = registerBlock("electric_motor", ElectricMotorBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK));

	public static final ServoMotorBlock SERVO_MOTOR = registerBlock("servo_motor", ServoMotorBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion());

	public static final AlternatorBlock ALTERNATOR = registerBlock("alternator", AlternatorBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK));

	public static final RollingMillBlock ROLLING_MILL = registerBlock("rolling_mill", RollingMillBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE));

	public static final CreativeEnergyBlock CREATIVE_ENERGY = registerBlock("creative_energy", CreativeEnergyBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK),
			BlockItem::new, new Item.Properties().rarity(Rarity.EPIC));

	public static final SmallConnectorBlock SMALL_CONNECTOR = registerBlock("connector", SmallConnectorBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE));

	public static final SmallLightConnectorBlock SMALL_LIGHT_CONNECTOR = registerBlock("small_light_connector", SmallLightConnectorBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE));

	public static final LargeConnectorBlock LARGE_CONNECTOR = registerBlock("large_connector", LargeConnectorBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE));

	public static final RedstoneRelayBlock REDSTONE_RELAY = registerBlock("redstone_relay", RedstoneRelayBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE));

	public static final CACakeBlock CHOCOLATE_CAKE = registerBlock("chocolate_cake", CACakeBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).sound(SoundType.WOOL).strength(0.5f));

	public static final CACakeBlock HONEY_CAKE = registerBlock("honey_cake", CACakeBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).sound(SoundType.WOOL).strength(0.5f));

	public static final BarbedWireBlock BARBED_WIRE = registerBlock("barbed_wire", BarbedWireBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.COBWEB).noCollision().requiresCorrectToolForDrops().strength(4.0F));

	public static final TeslaCoilBlock TESLA_COIL = registerBlock("tesla_coil", TeslaCoilBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel(state -> state.getValue(TeslaCoilBlock.POWERED) ? 10 : 0),
			AssemblyOperatorBlockItem::new, new Item.Properties());

	public static final ModularAccumulatorBlock MODULAR_ACCUMULATOR = registerBlock("modular_accumulator", ModularAccumulatorBlock::regular,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion(),
			ModularAccumulatorBlockItem::new, new Item.Properties());

	public static final LiquidBlazeBurnerBlock LIQUID_BLAZE_BURNER = registerBlockNoItem("liquid_blaze_burner", LiquidBlazeBurnerBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(DyeColor.GRAY).lightLevel(LiquidBlazeBurnerBlock::getLight));

	public static final PortableEnergyInterfaceBlock PORTABLE_ENERGY_INTERFACE = registerBlock("portable_energy_interface", PortableEnergyInterfaceBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK));

	public static final Block BIOMASS_PALLET = registerBlock("biomass_pellet_block", Block::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.DRIED_KELP_BLOCK).mapColor(MapColor.COLOR_GREEN),
			BiomassPelletBlockItem::new, new Item.Properties());

	public static final Block ELECTRUM_BLOCK = registerBlock("electrum_block", Block::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW));

	public static final DigitalAdapterBlock DIGITAL_ADAPTER = registerBlock("digital_adapter", DigitalAdapterBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK));

	private static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		return registerBlock(name, factory, properties, BlockItem::new, new Item.Properties());
	}

	private static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties,
			BiFunction<Block, Item.Properties, ? extends BlockItem> itemFactory, Item.Properties itemProperties) {
		T block = registerBlockNoItem(name, factory, properties);
		Identifier id = CreateAddition.asResource(name);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		BlockItem item = itemFactory.apply(block, itemProperties.setId(key).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, id, item);
		item.registerBlocks(Item.BY_BLOCK, item);
		return block;
	}

	private static <T extends Block> T registerBlockNoItem(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		Identifier id = CreateAddition.asResource(name);
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
		T block = factory.apply(properties.setId(key));
		Registry.register(BuiltInRegistries.BLOCK, id, block);
		return block;
	}

	public static void register() {
	}
}
