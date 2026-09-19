package com.mrh0.createaddition.mixin;

import com.mrh0.createaddition.blocks.electric_pump.ElectricPumpBlock;
import com.mrh0.createaddition.index.CABlockEntities;
import com.zurrtum.create.content.fluids.pump.PumpBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PumpBlockEntity.class)
public abstract class PumpBlockEntityMixin {
    @ModifyArg(
        method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/kinetics/base/KineticBlockEntity;<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
        ),
        index = 0
    )
    private static BlockEntityType<?> createaddition$substituteType(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof ElectricPumpBlock)
            return CABlockEntities.ELECTRIC_PUMP;
        return type;
    }
}
