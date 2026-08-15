package com.mrh0.createaddition.recipe.charging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrh0.createaddition.index.CARecipes;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public record ChargingRecipe(Ingredient ingredient, List<ProcessingOutput> results, int energy,
                             int maxChargeRate) implements CreateSingleStackRollableRecipe {
    public static final MapCodec<ChargingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Ingredient.CODEC.listOf(1, 1).fieldOf("ingredients").xmap(list -> list.getFirst(), single -> List.of(single)).forGetter(ChargingRecipe::ingredient),
        ProcessingOutput.CODEC.listOf(1, 1).fieldOf("results").forGetter(ChargingRecipe::results),
        Codec.INT.fieldOf("energy").forGetter(ChargingRecipe::energy),
        Codec.INT.fieldOf("max_charge_rate").forGetter(ChargingRecipe::maxChargeRate)
    ).apply(instance, ChargingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargingRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC,
        ChargingRecipe::ingredient,
        ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
        ChargingRecipe::results,
        ByteBufCodecs.INT,
        ChargingRecipe::energy,
        ByteBufCodecs.INT,
        ChargingRecipe::maxChargeRate,
        ChargingRecipe::new
    );
    public static final RecipeSerializer<ChargingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public int getEnergy() {
        return energy;
    }

    public int getMaxChargeRate() {
        return maxChargeRate;
    }

    public ItemStack getResultStack() {
        return results.getFirst().create();
    }

    @Override
    public RecipeSerializer<ChargingRecipe> getSerializer() {
        return CARecipes.CHARGING;
    }

    @Override
    public RecipeType<ChargingRecipe> getType() {
        return CARecipes.CHARGING_TYPE;
    }
}
