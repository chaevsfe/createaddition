package com.mrh0.createaddition.event;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.item.WireSpool;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.sound.CASoundScapes;
import com.mrh0.createaddition.util.Util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public class ClientEventHandler {

	public static boolean clientRenderHeldWire = false;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(ClientEventHandler::onClientTickEnd);
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return CreateAddition.asResource("client_resource_reload");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				CASoundScapes.invalidateAll();
			}
		});
	}

	private static void onClientTickEnd(Minecraft client) {
		ObservePacketPayload.tick();
		CASoundScapes.tick();
		updateHeldWire(client.player);
	}

	private static void updateHeldWire(LocalPlayer player) {
		if (player == null) {
			clientRenderHeldWire = false;
			return;
		}
		ItemStack stack = player.getInventory().getSelectedItem();
		if (stack.isEmpty() || WireSpool.isRemover(stack.getItem())) {
			clientRenderHeldWire = false;
			return;
		}
		clientRenderHeldWire = Util.getWireNodeOfSpools(stack) != null;
	}
}
