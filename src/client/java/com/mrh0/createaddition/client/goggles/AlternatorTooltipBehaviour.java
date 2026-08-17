package com.mrh0.createaddition.client.goggles;

import java.util.List;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.alternator.AlternatorBlockEntity;
import com.mrh0.createaddition.client.CALang;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class AlternatorTooltipBehaviour extends KineticTooltipBehaviour<AlternatorBlockEntity> {

	public AlternatorTooltipBehaviour(AlternatorBlockEntity be) {
		super(be);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		int rpm = (int) (blockEntity.isSpeedRequirementFulfilled() ? blockEntity.getSpeed() : 0);
		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.energy.production").withStyle(ChatFormatting.GRAY))
			.forGoggles(tooltip);
		CALang.builder()
			.add(Component.literal(" " + Util.format(AlternatorBlockEntity.getEnergyProductionRate(rpm)) + "⚡/t ")
				.withStyle(ChatFormatting.AQUA)
				.append(CreateLang.translateDirect("gui.goggles.at_current_speed").withStyle(ChatFormatting.DARK_GRAY)))
			.forGoggles(tooltip);
		return true;
	}
}
