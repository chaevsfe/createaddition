package com.mrh0.createaddition.client.goggles;

import java.util.List;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.mrh0.createaddition.client.CALang;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.network.TimeRemainingPacketPayload;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class LiquidBlazeBurnerTooltipBehaviour extends TooltipBehaviour<LiquidBlazeBurnerBlockEntity> implements IHaveGoggleInformation {

	public LiquidBlazeBurnerTooltipBehaviour(LiquidBlazeBurnerBlockEntity be) {
		super(be);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (getLevel() == null) return false;
		ObservePacketPayload.send(getPos(), 0);

		containedFluidTooltip(tooltip, isPlayerSneaking, blockEntity.getTank().getCapability());

		if (TimeRemainingPacketPayload.clientTimeRemaining > 20) {
			CALang.builder()
				.add(Component.literal(" ")
					.append(Component.translatable(CreateAddition.MODID + ".tooltip.liquid_burning.time_remaining")
						.withStyle(ChatFormatting.GRAY))
					.append(Component.literal(" " + Util.formatTime(TimeRemainingPacketPayload.clientTimeRemaining))
						.withStyle(ChatFormatting.AQUA)))
				.forGoggles(tooltip);
		}

		return true;
	}
}
