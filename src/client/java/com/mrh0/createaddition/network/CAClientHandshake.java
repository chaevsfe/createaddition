package com.mrh0.createaddition.network;

import com.mrh0.createaddition.CreateAddition;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;

public final class CAClientHandshake {
    private static volatile boolean answered;

    private CAClientHandshake() {
    }

    public static void register() {
        ClientConfigurationConnectionEvents.INIT.register((handler, client) -> answered = false);
        ClientConfigurationNetworking.registerGlobalReceiver(HandshakePayload.TYPE, (payload, context) -> {
            answered = true;
            if (!CAHandshake.matches(payload))
                CreateAddition.LOGGER.warn("Server runs Crafts & Additions {} (blocks {}), this client {} (blocks {})",
                        payload.version(), CAHandshake.hex(payload.fingerprint()),
                        CAHandshake.local().version(), CAHandshake.hex(CAHandshake.local().fingerprint()));
            context.responseSender().sendPacket(CAHandshake.local());
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (answered || !ClientPlayNetworking.canSend(ObservePacketPayload.TYPE)) return;
            CreateAddition.LOGGER.warn("Leaving: the server speaks Crafts & Additions but sent no handshake, so it runs a version older than this client's {}", CAHandshake.local().version());
            sender.disconnect(Component.translatableWithFallback(CAHandshake.KEY_SERVER_TOO_OLD, CAHandshake.FALLBACK_SERVER_TOO_OLD, CAHandshake.local().version()));
        });
    }
}
