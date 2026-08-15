package com.mrh0.createaddition.index;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.effect.ShockingEffect;


public class CAEffects {
	public static final Holder<MobEffect> SHOCKING = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, CreateAddition.asResource("shocking"), new ShockingEffect()
			.addAttributeModifier(Attributes.MOVEMENT_SPEED, CreateAddition.asResource("shocking"), (double)-100f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

	public static void register() {}
}
