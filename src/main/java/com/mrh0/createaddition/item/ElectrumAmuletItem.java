package com.mrh0.createaddition.item;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.index.CAEffects;
import com.mrh0.createaddition.index.CASounds;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class ElectrumAmuletItem extends Item {
    private static final int CHARGE_CHANCE = 300;

    public ElectrumAmuletItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof Player player)) return;
        if (!CACommonConfig.COMMON.AMULET_EFFECT_ENABLED.get()) return;

        InteractionHand otherHand;
        if (slot == EquipmentSlot.MAINHAND) otherHand = InteractionHand.OFF_HAND;
        else if (slot == EquipmentSlot.OFFHAND) otherHand = InteractionHand.MAIN_HAND;
        else return;

        EnergyStorage es = EnergyStorage.ITEM.find(player.getItemInHand(otherHand), ContainerItemContext.ofPlayerHand(player, otherHand));
        if (es == null) return;

        boolean canCharge;
        try (Transaction t = Transaction.openOuter()) {
            canCharge = es.insert(1, t) > 0;
        }

        if (canCharge && level.getRandom().nextInt(CHARGE_CHANCE) == 0) {
            try (Transaction t = Transaction.openOuter()) {
                es.insert((long) CHARGE_CHANCE * CACommonConfig.COMMON.ELECTRUM_AMULET_CHARGE_RATE.get(), t);
                t.commit();
            }
            player.addEffect(new MobEffectInstance(CAEffects.SHOCKING, 40, 0, false, true, true));
            if (CACommonConfig.COMMON.AUDIO_ENABLED.get())
                level.playSound(null, player.blockPosition(), CASounds.LITTLE_ZAP, SoundSource.PLAYERS, 0.4f, 1f);
        }
    }
}
