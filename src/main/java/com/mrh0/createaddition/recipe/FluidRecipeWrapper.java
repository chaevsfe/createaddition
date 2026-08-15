package com.mrh0.createaddition.recipe;

import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class FluidRecipeWrapper implements RecipeInput {

	public FluidStack fluid;

	public FluidRecipeWrapper(FluidStack fluid) {
		this.fluid = fluid;
	}

	@Override
	public ItemStack getItem(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return fluid == null || fluid.isEmpty();
	}
}
