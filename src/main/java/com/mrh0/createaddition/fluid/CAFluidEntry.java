package com.mrh0.createaddition.fluid;

import com.mrh0.createaddition.index.CATags;
import com.mrh0.createaddition.mixin.FireBlockInvoker;
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class CAFluidEntry extends FluidEntry {
    {
        flowing = new Flowing();
        still = new Still();
    }

    static boolean protectsFire(Fluid fluid, BlockState replaced, Direction direction) {
        return direction != Direction.DOWN && replaced.is(BlockTags.FIRE)
            && fluid.defaultFluidState().is(CATags.Fluids.IGNITES);
    }

    static void spreadFire(Fluid fluid, LevelAccessor world, BlockPos pos, BlockState replaced, Direction direction) {
        if (direction != Direction.DOWN) return;
        if (!replaced.is(BlockTags.FIRE)) return;
        if (!fluid.defaultFluidState().is(CATags.Fluids.IGNITES)) return;
        if (world instanceof ServerLevel serverLevel && !serverLevel.canSpreadFireAround(pos)) return;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = pos.relative(side);
            if (world.isEmptyBlock(sidePos) && replaced.canSurvive(world, sidePos)) {
                BlockState fireState;
                if (replaced.getBlock() instanceof FireBlock fire) {
                    fireState = ((FireBlockInvoker) fire).invokeSpreadPlacement(world, sidePos);
                } else {
                    fireState = replaced.getBlock().defaultBlockState();
                }
                world.setBlock(sidePos, fireState, 3);
            }
        }
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
        protected void spreadTo(LevelAccessor world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
            if (protectsFire(this, state, direction)) {
                return;
            }
            super.spreadTo(world, pos, state, direction, fluidState);
            spreadFire(this, world, pos, state, direction);
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
        protected void spreadTo(LevelAccessor world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
            if (protectsFire(this, state, direction)) {
                return;
            }
            super.spreadTo(world, pos, state, direction, fluidState);
            spreadFire(this, world, pos, state, direction);
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
