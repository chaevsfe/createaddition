package com.mrh0.createaddition.event;

import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlock;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyManager;
import com.mrh0.createaddition.energy.network.EnergyNetworkManager;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CAItems;
import com.zurrtum.create.AllBlocks;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GameEvents {

	public static void initCommon() {
		ServerTickEvents.START_LEVEL_TICK.register(GameEvents::levelTickEvent);
		ServerTickEvents.START_SERVER_TICK.register(GameEvents::serverTickEvent);
		ServerLevelEvents.LOAD.register(GameEvents::loadEvent);
		ServerLevelEvents.UNLOAD.register(GameEvents::unloadEvent);
		UseBlockCallback.EVENT.register(GameEvents::interact);
	}

	public static void levelTickEvent(ServerLevel level) {
		EnergyNetworkManager.tickWorld(level);
	}

	public static void serverTickEvent(MinecraftServer server) {
		// Using ServerTick instead of WorldTick because some contraptions can switch worlds.
		PortableEnergyManager.tick();
	}

	public static void loadEvent(MinecraftServer server, ServerLevel level) {
		new EnergyNetworkManager(level);
	}

	public static void unloadEvent(MinecraftServer server, ServerLevel level) {
		EnergyNetworkManager.instances.remove(level);
	}

	public static InteractionResult interact(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		try {
			if(level.isClientSide()) return InteractionResult.PASS;
			BlockPos pos = hitResult.getBlockPos();
			ItemStack item = player.getItemInHand(hand);
			BlockState state = level.getBlockState(pos);
			if(item.getItem() == CAItems.STRAW.get() && level.getBlockEntity(pos) instanceof BlazeBurnerBlockEntity) {
				if(state.is(AllBlocks.BLAZE_BURNER)) {
					BlockState newState = CABlocks.LIQUID_BLAZE_BURNER.getDefaultState()
							.setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING)
							.setValue(LiquidBlazeBurnerBlock.FACING, state.getValue(BlazeBurnerBlock.FACING));
					level.setBlockAndUpdate(pos, newState);
					if(!player.isCreative())
						item.shrink(1);
					return InteractionResult.SUCCESS;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return InteractionResult.PASS;
	}
}
