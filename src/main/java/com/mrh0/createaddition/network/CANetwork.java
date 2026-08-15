package com.mrh0.createaddition.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class CANetwork {
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ObservePacketPayload.TYPE, ObservePacketPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EnergyNetworkPacketPayload.TYPE, EnergyNetworkPacketPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TimeRemainingPacketPayload.TYPE, TimeRemainingPacketPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ObservePacketPayload.TYPE, (payload, context) -> ServerPayloadHandler.handleObservePayload(payload, context.player()));
    }
}
