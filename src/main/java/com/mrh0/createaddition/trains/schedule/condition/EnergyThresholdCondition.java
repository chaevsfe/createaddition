package com.mrh0.createaddition.trains.schedule.condition;

import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyManager;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.schedule.condition.CargoThresholdCondition;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;

public class EnergyThresholdCondition extends CargoThresholdCondition {

    public EnergyThresholdCondition(Identifier id) {
        super(id);
    }

    @Override
    public boolean lazyTickCompletion(Level level, Train train, CompoundTag context) {
        return test(level, train, context);
    }

    @Override
    protected boolean test(Level level, Train train, CompoundTag context) {
        Ops operator = getOperator();
        int target = getThreshold();

        long foundEnergy = 0;
        for (Carriage carriage : train.carriages) {
            if (carriage.anyAvailableEntity() == null) continue;
            EnergyStorage es = PortableEnergyManager.get(carriage.anyAvailableEntity().getContraption());
            if (es == null) continue;
            foundEnergy += es.getAmount();
        }

        int found = (int) Math.min(foundEnergy, Integer.MAX_VALUE);
        requestStatusToUpdate(found / 1000, context);
        return operator.test(found, target * 1000);
    }

    @Override
    public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
        int lastDisplaySnapshot = getLastDisplaySnapshot(tag);
        if (lastDisplaySnapshot == -1)
            return Component.empty();
        int offset = getOperator() == Ops.LESS ? -1 : getOperator() == Ops.GREATER ? 1 : 0;
        return Component.translatable("create.schedule.condition.threshold.status", lastDisplaySnapshot,
                Math.max(0, getThreshold() + offset), Component.translatable("createaddition.schedule.condition.threshold.unit"));
    }
}
