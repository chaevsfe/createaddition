package com.mrh0.createaddition.network;

import com.mrh0.createaddition.CreateAddition;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ObservePacketPayload(BlockPos pos, int node) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ObservePacketPayload> TYPE = new CustomPacketPayload.Type<>(CreateAddition.asResource("observer_packet"));

    public static final StreamCodec<ByteBuf, ObservePacketPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ObservePacketPayload::pos,
            ByteBufCodecs.VAR_INT,
            ObservePacketPayload::node,
            ObservePacketPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static int cooldown = 0;

    @Environment(EnvType.CLIENT)
    public static void tick() {
        cooldown--;
        if (cooldown < 0) cooldown = 0;
    }

    @Environment(EnvType.CLIENT)
    public static boolean send(BlockPos pos, int node) {
        if (cooldown > 0) return false;
        cooldown = 10;
        ClientPlayNetworking.send(new ObservePacketPayload(pos, node));
        return true;
    }
}
