package com.mrh0.createaddition.client.goggles;

import java.util.List;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockEntity;
import com.mrh0.createaddition.client.CALang;
import com.mrh0.createaddition.network.EnergyNetworkPacketPayload;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ModularAccumulatorTooltipBehaviour extends TooltipBehaviour<ModularAccumulatorBlockEntity> implements IHaveGoggleInformation {

	public ModularAccumulatorTooltipBehaviour(ModularAccumulatorBlockEntity be) {
		super(be);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		ModularAccumulatorBlockEntity controller = blockEntity.getControllerBE();
		if (controller == null) return false;

		ObservePacketPayload.send(blockEntity.getController(), 0);

		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.accumulator.info").withStyle(ChatFormatting.WHITE))
			.forGoggles(tooltip);

		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.energy.stored").withStyle(ChatFormatting.GRAY))
			.forGoggles(tooltip);
		CALang.builder()
			.add(Component.literal(" ").append(Util.format(EnergyNetworkPacketPayload.clientBuff)).append("⚡")
				.withStyle(ChatFormatting.AQUA))
			.forGoggles(tooltip);

		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.energy.capacity").withStyle(ChatFormatting.GRAY))
			.forGoggles(tooltip);
		CALang.builder()
			.add(Component.literal(" ").append(Util.format(controller.getEnergy().getCapacity())).append("⚡")
				.withStyle(ChatFormatting.AQUA))
			.forGoggles(tooltip);

		return true;
	}
}
