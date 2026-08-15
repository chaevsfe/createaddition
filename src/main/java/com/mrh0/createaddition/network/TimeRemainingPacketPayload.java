package com.mrh0.createaddition.network;

import com.mrh0.createaddition.CreateAddition;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record TimeRemainingPacketPayload(int timeRemaining) implements CustomPacketPayload {
    public static int clientTimeRemaining = 0;

    public static final Type<TimeRemainingPacketPayload> TYPE = new Type<>(CreateAddition.asResource("time_remaining_packet"));

    public static final StreamCodec<ByteBuf, TimeRemainingPacketPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            TimeRemainingPacketPayload::timeRemaining,
            TimeRemainingPacketPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void updateClientCache(int timeRemaining) {
        clientTimeRemaining = timeRemaining;
    }

    public static boolean send(int timeRemaining, ServerPlayer player) {
        ServerPlayNetworking.send(player, new TimeRemainingPacketPayload(timeRemaining));
        return true;
    }
}
