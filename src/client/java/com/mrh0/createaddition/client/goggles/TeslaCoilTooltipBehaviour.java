package com.mrh0.createaddition.client.goggles;

import java.util.List;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlockEntity;
import com.mrh0.createaddition.client.CALang;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.network.TimeRemainingPacketPayload;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TeslaCoilTooltipBehaviour extends TooltipBehaviour<TeslaCoilBlockEntity> implements IHaveGoggleInformation {

	public TeslaCoilTooltipBehaviour(TeslaCoilBlockEntity be) {
		super(be);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (getLevel() == null) return false;
		ObservePacketPayload.send(getPos(), 0);

		int remaining = TimeRemainingPacketPayload.clientTimeRemaining;
		if (remaining == 0 || remaining > 0 && remaining <= 20) return false;
		String timeStr = remaining == -1 ? "∞" : Util.formatTime(remaining);

		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.charging.info").withStyle(ChatFormatting.WHITE))
			.forGoggles(tooltip);
		CALang.builder()
			.add(Component.literal(" ")
				.append(Component.translatable(CreateAddition.MODID + ".tooltip.charging.time_remaining").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(" " + timeStr).withStyle(ChatFormatting.AQUA)))
			.forGoggles(tooltip);

		return true;
	}
}
