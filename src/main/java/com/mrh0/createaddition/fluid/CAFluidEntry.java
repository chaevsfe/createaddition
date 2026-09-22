package com.mrh0.createaddition.fluid;

import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;

public class CAFluidEntry extends FluidEntry {
    {
        flowing = new Flowing();
        still = new Still();
    }

    private class Flowing extends FlowableFluid.Flowing {
        Flowing() {
            super(CAFluidEntry.this);
        }

        @Override
        public CAFluidEntry getEntry() {
            return CAFluidEntry.this;
        }

        @Override
        public int getTickDelay(LevelReader world) {
            return 15;
        }

        @Override
        public int getSlopeFindDistance(LevelReader world) {
            return 6;
        }

        @Override
        protected float getExplosionResistance() {
            return 100f;
        }
    }

    private class Still extends FlowableFluid.Still {
        Still() {
            super(CAFluidEntry.this);
        }

        @Override
        public CAFluidEntry getEntry() {
            return CAFluidEntry.this;
        }

        @Override
        public Item getBucket() {
            return bucket != null ? bucket : Items.AIR;
        }

        @Override
        public int getTickDelay(LevelReader world) {
            return 15;
        }

        @Override
        public int getSlopeFindDistance(LevelReader world) {
            return 6;
        }

        @Override
        protected float getExplosionResistance() {
            return 100f;
        }
    }
}
