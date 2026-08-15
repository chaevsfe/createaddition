package com.mrh0.createaddition.blocks.electric_motor;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.KineticScrollValueBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class ElectricMotorScrollValueBehaviour extends KineticScrollValueBehaviour {

	public ElectricMotorScrollValueBehaviour(ElectricMotorBlockEntity be) {
		super(CreateLang.translateDirect("generic.speed"), be, new CenteredSideValueBoxTransform(
				(state, side) -> state.getValue(ElectricMotorBlock.FACING) == side.getOpposite()));
	}

	@Override
	public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
		return new ValueSettingsBoard(label, behaviour.getMax(), 32, ImmutableList.of(
				Component.literal("⟳").withStyle(ChatFormatting.BOLD),
				Component.literal("⟲").withStyle(ChatFormatting.BOLD)),
				new ValueSettingsFormatter(this::formatSettings));
	}
}
