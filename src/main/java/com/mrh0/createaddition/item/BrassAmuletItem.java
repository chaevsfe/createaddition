package com.mrh0.createaddition.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BrassAmuletItem extends Item {
    public BrassAmuletItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if(!(entity instanceof Player player)) return;
        if(player.getFoodData().getSaturationLevel() > 1f) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 3, 0, true, true, true));
        }
    }
}
