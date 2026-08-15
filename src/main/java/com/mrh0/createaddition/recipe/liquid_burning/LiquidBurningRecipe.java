package com.mrh0.createaddition.recipe.liquid_burning;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.FluidRecipeWrapper;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record LiquidBurningRecipe(FluidIngredient fluidIngredient, int burnTime,
                                  boolean superheated) implements CreateRecipe<FluidRecipeWrapper> {
    public static final MapCodec<LiquidBurningRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(LiquidBurningRecipe::fluidIngredient),
        Codec.INT.fieldOf("burn_time").forGetter(LiquidBurningRecipe::burnTime),
        Codec.BOOL.optionalFieldOf("superheated", false).forGetter(LiquidBurningRecipe::superheated)
    ).apply(instance, LiquidBurningRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, LiquidBurningRecipe> STREAM_CODEC = StreamCodec.composite(
        FluidIngredient.PACKET_CODEC,
        LiquidBurningRecipe::fluidIngredient,
        ByteBufCodecs.INT,
        LiquidBurningRecipe::burnTime,
        ByteBufCodecs.BOOL,
        LiquidBurningRecipe::superheated,
        LiquidBurningRecipe::new
    );
    public static final RecipeSerializer<LiquidBurningRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public int getBurnTime() {
        return burnTime;
    }

    public boolean isSuperheated() {
        return superheated;
    }

    public FluidIngredient getFluidInput() {
        return fluidIngredient;
    }

    @Override
    public boolean matches(FluidRecipeWrapper wrapper, Level level) {
        return wrapper.fluid != null && fluidIngredient.test(wrapper.fluid);
    }

    @Override
    public ItemStack assemble(FluidRecipeWrapper wrapper) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<LiquidBurningRecipe> getSerializer() {
        return CARecipes.LIQUID_BURNING;
    }

    @Override
    public RecipeType<LiquidBurningRecipe> getType() {
        return CARecipes.LIQUID_BURNING_TYPE;
    }
}
