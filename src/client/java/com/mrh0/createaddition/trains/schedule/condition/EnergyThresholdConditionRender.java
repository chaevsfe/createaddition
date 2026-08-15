package com.mrh0.createaddition.trains.schedule.condition;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mrh0.createaddition.index.CAItems;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.content.trains.schedule.condition.CargoThresholdConditionRender;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class EnergyThresholdConditionRender extends CargoThresholdConditionRender<EnergyThresholdCondition> {

	@Override
	protected Component getUnit(EnergyThresholdCondition input) {
		return Component.translatable("createaddition.schedule.condition.threshold.unit");
	}

	@Override
	protected ItemStack getIcon(EnergyThresholdCondition input) {
		return CAItems.CAPACITOR.getDefaultInstance();
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return CAItems.CAPACITOR.getDefaultInstance();
	}

	@Override
	public int slotsTargeted() {
		return 0;
	}

	@Override
	public List<Component> getTitleAs(EnergyThresholdCondition input, String type) {
		return ImmutableList.of(
				CreateLang.translateDirect("schedule.condition.threshold.train_holds",
						CreateLang.translateDirect("schedule.condition.threshold." + Lang.asId(input.getOperator().name()))),
				CreateLang.translateDirect("schedule.condition.threshold.x_units_of_item",
						input.getThreshold(),
						Component.translatable("createaddition.schedule.condition.threshold.unit"),
						Component.translatable("createaddition.schedule.condition.threshold.energy"))
						.withStyle(ChatFormatting.DARK_AQUA));
	}

	@Override
	public void initConfigurationWidgets(EnergyThresholdCondition input, ModularGuiLineBuilder builder) {
		super.initConfigurationWidgets(input, builder);
		builder.addSelectionScrollInput(71, 50, (i, l) -> {
			i.forOptions(ImmutableList.of(Component.translatable("createaddition.schedule.condition.threshold.unit")))
					.titled(null);
		}, "Measure");
	}
}
