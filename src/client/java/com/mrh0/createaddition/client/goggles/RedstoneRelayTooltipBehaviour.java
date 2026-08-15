package com.mrh0.createaddition.client.goggles;

import java.util.List;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.redstone_relay.RedstoneRelayBlockEntity;
import com.mrh0.createaddition.client.CALang;
import com.mrh0.createaddition.network.EnergyNetworkPacketPayload;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;

public class RedstoneRelayTooltipBehaviour extends TooltipBehaviour<RedstoneRelayBlockEntity> implements IHaveGoggleInformation {

	public RedstoneRelayTooltipBehaviour(RedstoneRelayBlockEntity be) {
		super(be);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		HitResult ray = Minecraft.getInstance().hitResult;
		if (ray == null) return false;
		int node = blockEntity.getAvailableNode(ray.getLocation());

		ObservePacketPayload.send(getPos(), node);

		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.relay.info").withStyle(ChatFormatting.WHITE))
			.forGoggles(tooltip);

		CALang.builder()
			.add(Component.translatable(CreateAddition.MODID + ".tooltip.energy.selected").withStyle(ChatFormatting.GRAY))
			.forGoggles(tooltip);
		CALang.builder()
			.add(Component.literal(" ").append(Component.translatable(blockEntity.isNodeInput(node) ?
				CreateAddition.MODID + ".tooltip.energy.push" : CreateAddition.MODID + ".tooltip.energy.pull")
				.withStyle(ChatFormatting.AQUA)))
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
