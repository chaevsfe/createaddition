package com.mrh0.createaddition.compat.jei;

import com.mrh0.createaddition.CreateAddition;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import net.minecraft.network.chat.Component;

public abstract class CARecipeCategory<T> extends CreateCategory<T> {

	private final Component title;

	protected CARecipeCategory(String name) {
		this.title = Component.translatable(CreateAddition.MODID + ".recipe." + name);
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public int getHeight() {
		return 53;
	}
}
