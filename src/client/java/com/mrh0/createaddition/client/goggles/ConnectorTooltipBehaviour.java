package com.mrh0.createaddition.client.goggles;

import java.util.List;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.connector.base.AbstractConnectorBlock;
import com.mrh0.createaddition.blocks.connector.base.AbstractConnectorBlockEntity;
import com.mrh0.createaddition.client.CALang;
import com.mrh0.createaddition.network.EnergyNetworkPacketPayload;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ConnectorTooltipBehaviour extends TooltipBehaviour<AbstractConnectorBlockEntity> implements IHaveGoggleInformation {

	public ConnectorTooltipBehaviour(AbstractConnectorBlockEntity be) {
		super(be);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		ObservePacketPayload.send(getPos(), 0);

		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.connector.info").withStyle(ChatFormatting.WHITE))
			.forGoggles(tooltip);

		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.energy.mode").withStyle(ChatFormatting.GRAY))
			.forGoggles(tooltip);
		CALang.builder()
			.add(Component.literal(" ")
				.append(blockEntity.getBlockState().getValue(AbstractConnectorBlock.MODE).getTooltip().withStyle(ChatFormatting.AQUA)))
			.forGoggles(tooltip);

		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.energy.usage").withStyle(ChatFormatting.GRAY))
			.forGoggles(tooltip);
		CALang.builder()
			.add(Component.literal(" ").append(Util.format(EnergyNetworkPacketPayload.clientBuff)).append("⚡/t")
				.withStyle(ChatFormatting.AQUA))
			.forGoggles(tooltip);

		return true;
	}
}
