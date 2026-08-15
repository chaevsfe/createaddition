package com.mrh0.createaddition.blocks.modular_accumulator;

import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public class ModularAccumulatorDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected MutableComponent formatNumeric(DisplayLinkContext context, Float currentLevel) {
		int mode = getMode(context);
		if (mode == 1)
			return super.formatNumeric(context, currentLevel);
		return Util.getTextComponent(Math.round(currentLevel), "fe");
	}

	private int getMode(DisplayLinkContext context) {
		return context.sourceConfig()
			.getIntOr("Mode", 0);
	}

	@Nullable
	@Override
	protected Float getProgress(DisplayLinkContext context) {
		if (!(context.getSourceBlockEntity() instanceof ModularAccumulatorBlockEntity be)) return null;
		be = be.getControllerBE();
		if (be == null) return null;

		float capacity = be.energyCapability.getCapacity();
		float stored = be.energyCapability.getAmount();

		if (capacity == 0) return 0f;

		return switch (getMode(context)) {
			case 0, 1 -> stored / capacity;
			case 2 -> stored;
			case 3 -> capacity;
			case 4 -> capacity - stored;
			default -> 0f;
		};
	}

	@Override
	public boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

	@Override
	protected boolean progressBarActive(DisplayLinkContext context) {
		return getMode(context) == 0;
	}

	@Override
	protected String getTranslationKey() {
		return "modular_accumulator";
	}
}
