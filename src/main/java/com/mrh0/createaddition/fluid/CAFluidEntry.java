package com.mrh0.createaddition.fluid;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class CAFluidEntry {
    public FlowableFluid flowing = new Flowing();
    public FlowableFluid still = new Still();
    public BucketItem bucket;
    public FluidBlock block;

    private class Flowing extends FlowableFluid {
        public CAFluidEntry getEntry() {
            return CAFluidEntry.this;
        }

        @Override
        public Fluid getFlowing() {
            return this;
        }

        @Override
        public Fluid getSource() {
            return still;
        }

        @Override
        public Item getBucket() {
            return bucket != null ? bucket : Items.AIR;
        }

        @Override
        public BlockState createLegacyBlock(FluidState state) {
            if (block != null) {
                return block.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
            }
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public boolean isSame(Fluid fluid) {
            return fluid == this || fluid == still;
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
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

    private class Still extends FlowableFluid {
        public CAFluidEntry getEntry() {
            return CAFluidEntry.this;
        }

        @Override
        public Fluid getFlowing() {
            return flowing;
        }

        @Override
        public Fluid getSource() {
            return this;
        }

        @Override
        public Item getBucket() {
            return bucket != null ? bucket : Items.AIR;
        }

        @Override
        public BlockState createLegacyBlock(FluidState state) {
            if (block == null) {
                return Blocks.AIR.defaultBlockState();
            }
            return block.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }

        @Override
        public boolean isSame(Fluid fluid) {
            return fluid == this || fluid == flowing;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
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
