package com.mrh0.createaddition.mixin;

import java.util.Optional;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
    @Unique
    private static final String CREATEADDITION_LIT_TIME_REMAINING = "createaddition:lit_time_remaining";
    @Unique
    private static final String CREATEADDITION_LIT_TOTAL_TIME = "createaddition:lit_total_time";

    @Shadow
    private int litTimeRemaining;
    @Shadow
    private int litTotalTime;

    @Inject(method = "loadAdditional(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"), require = 1)
    private void createaddition$loadLongBurn(ValueInput input, CallbackInfo ci) {
        litTimeRemaining = createaddition$restore(input, CREATEADDITION_LIT_TIME_REMAINING, litTimeRemaining);
        litTotalTime = createaddition$restore(input, CREATEADDITION_LIT_TOTAL_TIME, litTotalTime);
    }

    @Unique
    private static int createaddition$restore(ValueInput input, String key, int saved) {
        Optional<Integer> stored = input.getInt(key);
        return stored.isPresent() && stored.get().shortValue() == (short) saved ? stored.get() : saved;
    }
}
