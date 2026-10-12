package com.mrh0.createaddition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrh0.createaddition.index.CATags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.EnumMap;
import java.util.Map;

@Mixin(FlowingFluid.class)
public class FlowingFluidMixin {

    @WrapOperation(
            method = "spreadToSides(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FlowingFluid;getSpread(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Map;"
            )
    )
    private Map<Direction, FluidState> protectFire(FlowingFluid self, ServerLevel level, BlockPos pos, BlockState state,
            Operation<Map<Direction, FluidState>> original) {
        Map<Direction, FluidState> spread = original.call(self, level, pos, state);
        Map<Direction, FluidState> kept = spread;
        for (Map.Entry<Direction, FluidState> entry : spread.entrySet()) {
            if (entry.getValue().is(CATags.Fluids.IGNITES) && level.getBlockState(pos.relative(entry.getKey())).is(BlockTags.FIRE)) {
                if (kept == spread) {
                    kept = new EnumMap<>(spread);
                }
                kept.remove(entry.getKey());
            }
        }
        return kept;
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
