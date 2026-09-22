package com.mrh0.createaddition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrh0.createaddition.index.CATags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlowingFluid.class)
public class FlowingFluidMixin {

    @WrapOperation(
            method = "getSpread(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Map;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FlowingFluid;canHoldSpecificFluid(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z"
            )
    )
    private boolean protectFire(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid, Operation<Boolean> original) {
        if (!original.call(level, pos, state, fluid)) {
            return false;
        }
        return !(state.is(BlockTags.FIRE) && fluid.defaultFluidState().is(CATags.Fluids.IGNITES));
    }

    @WrapOperation(
            method = "spread(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FlowingFluid;spreadTo(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)V"
            )
    )
    private void spreadFire(FlowingFluid self, LevelAccessor level, BlockPos pos, BlockState replaced, Direction direction, FluidState fluidState,
            Operation<Void> original) {
        original.call(self, level, pos, replaced, direction, fluidState);
        if (direction != Direction.DOWN || !replaced.is(BlockTags.FIRE) || !fluidState.is(CATags.Fluids.IGNITES)) {
            return;
        }
        if (level instanceof ServerLevel serverLevel && !serverLevel.canSpreadFireAround(pos)) {
            return;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = pos.relative(side);
            if (level.isEmptyBlock(sidePos) && replaced.canSurvive(level, sidePos)) {
                BlockState fireState;
                if (replaced.getBlock() instanceof FireBlock fire) {
                    fireState = ((FireBlockInvoker) fire).invokeSpreadPlacement(level, sidePos);
                } else {
                    fireState = replaced.getBlock().defaultBlockState();
                }
                level.setBlock(sidePos, fireState, 3);
            }
        }
    }
}
