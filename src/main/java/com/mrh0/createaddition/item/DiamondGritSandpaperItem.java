package com.mrh0.createaddition.item;

import com.mrh0.createaddition.config.CACommonConfig;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperItem;

public class DiamondGritSandpaperItem extends SandPaperItem {
	public DiamondGritSandpaperItem(Properties properties) {
		super(properties.durability(CACommonConfig.COMMON.DIAMOND_GRIT_SANDPAPER_USES.get()));
	}
}
