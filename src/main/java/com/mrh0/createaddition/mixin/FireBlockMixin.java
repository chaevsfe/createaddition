package com.mrh0.createaddition.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mrh0.createaddition.index.CATags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    @ModifyReturnValue(
            method = "canBurn(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("RETURN")
    )
    private boolean igniteFluid(boolean original, BlockState state) {
        if (original) {
            return true;
        }
        return state.getFluidState().is(CATags.Fluids.IGNITES);
    }

    @ModifyExpressionValue(
            method = "getIgniteOdds(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I")
    )
    private int considerFluidFireSpreadSpeed(int igniteOdds, LevelReader level, BlockPos origin, @Local Direction direction) {
        BlockPos fluidPos = origin.relative(direction);

        FluidState fluidState = level.getFluidState(fluidPos);
        if (fluidState.isEmpty()) {
            return igniteOdds;
        }
        if (!fluidState.is(CATags.Fluids.IGNITES)) {
            return igniteOdds;
        }
        int fireSpeed = 75;
        if (level instanceof Level weatherLevel) {
            if (weatherLevel.isRainingAt(fluidPos)) {
                fireSpeed = 100;
            }
        }

        return Math.max(igniteOdds, fireSpeed);
    }

    @ModifyExpressionValue(
            method = "checkBurnOut(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/util/RandomSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;getBurnOdds(Lnet/minecraft/world/level/block/state/BlockState;)I")
    )
    private int considerFluidFlammability(int original, Level level, BlockPos pos, int odds, RandomSource random, int age) {
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.isEmpty()) {
            return original;
        }
        if (!fluidState.is(CATags.Fluids.IGNITES)) {
            return original;
        }

        int flammability = 250;
        if (level.isRainingAt(pos)) {
            flammability = 300;
        }

        return Math.max(original, flammability);
    }

    @ModifyExpressionValue(
            method = "tick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/core/HolderSet;)Z")
    )
    private boolean keepFire(boolean original, BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (original) {
            return true;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos fluidPos = pos.relative(direction);
            FluidState fluid = level.getFluidState(fluidPos);
            if (fluid.isEmpty()) {
                continue;
            }
            if (fluid.is(CATags.Fluids.IGNITES)) {
                return true;
            }
        }
        return false;
    }
}
