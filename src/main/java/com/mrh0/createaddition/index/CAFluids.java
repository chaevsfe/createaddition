package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.fluid.CAFluidEntry;
import com.zurrtum.create.AllFluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;

public class CAFluids {
    public static final CAFluidEntry SEED_OIL_ENTRY = new CAFluidEntry();
    public static final CAFluidEntry BIOETHANOL_ENTRY = new CAFluidEntry();

    public static final FlowableFluid FLOWING_SEED_OIL = registerFluid("flowing_seed_oil", SEED_OIL_ENTRY.flowing);
    public static final FlowableFluid SEED_OIL = registerFluid("seed_oil", SEED_OIL_ENTRY.still);
    public static final FlowableFluid FLOWING_BIOETHANOL = registerFluid("flowing_bioethanol", BIOETHANOL_ENTRY.flowing);
    public static final FlowableFluid BIOETHANOL = registerFluid("bioethanol", BIOETHANOL_ENTRY.still);

    private static FlowableFluid registerFluid(String name, FlowableFluid fluid) {
        return Registry.register(BuiltInRegistries.FLUID, CreateAddition.asResource(name), fluid);
    }

    public static void register() {
    }

    public static void registerFluidBlocks() {
        SEED_OIL_ENTRY.block = registerFluidBlock("seed_oil", SEED_OIL_ENTRY);
        BIOETHANOL_ENTRY.block = registerFluidBlock("bioethanol", BIOETHANOL_ENTRY);
    }

    private static FluidBlock registerFluidBlock(String name, CAFluidEntry entry) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, CreateAddition.asResource(name));
        FluidBlock block = new FluidBlock(entry.still, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    public static void registerItems() {
        SEED_OIL_ENTRY.bucket = registerBucket("seed_oil_bucket", SEED_OIL_ENTRY.still);
        BIOETHANOL_ENTRY.bucket = registerBucket("bioethanol_bucket", BIOETHANOL_ENTRY.still);
        AllFluidItemInventory.ALL.put(SEED_OIL_ENTRY.bucket, new AllFluidItemInventory.Entry(BucketFluidInventory::new));
        AllFluidItemInventory.ALL.put(BIOETHANOL_ENTRY.bucket, new AllFluidItemInventory.Entry(BucketFluidInventory::new));
    }

    private static BucketItem registerBucket(String name, Fluid fluid) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, CreateAddition.asResource(name));
        BucketItem bucket = new BucketItem(fluid, new Item.Properties().setId(key).craftRemainder(Items.BUCKET).stacksTo(1));
        return Registry.register(BuiltInRegistries.ITEM, key, bucket);
    }
}
