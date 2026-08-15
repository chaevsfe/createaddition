package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class CASounds {
    public static final SoundEvent ELECTRIC_MOTOR_BUZZ = registerSoundEvent("electric_motor_buzz");
    public static final SoundEvent TESLA_COIL = registerSoundEvent("tesla_coil");
    public static final SoundEvent ELECTRIC_CHARGE = registerSoundEvent("electric_charge");
    public static final SoundEvent LOUD_ZAP = registerSoundEvent("loud_zap");
    public static final SoundEvent LITTLE_ZAP = registerSoundEvent("little_zap");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = CreateAddition.asResource(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {}
}
