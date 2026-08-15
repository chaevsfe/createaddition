package com.mrh0.createaddition.network;

import com.mrh0.createaddition.CreateAddition;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record EnergyNetworkPacketPayload(BlockPos pos, int demand, int buff) implements CustomPacketPayload {
    public static double clientSaturation = 0;
    public static int clientDemand = 0;
    public static int clientBuff = 0;

    public static final CustomPacketPayload.Type<EnergyNetworkPacketPayload> TYPE = new CustomPacketPayload.Type<>(CreateAddition.asResource("energy_network_packet"));

    public static final StreamCodec<ByteBuf, EnergyNetworkPacketPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            EnergyNetworkPacketPayload::pos,
            ByteBufCodecs.VAR_INT,
            EnergyNetworkPacketPayload::demand,
            ByteBufCodecs.VAR_INT,
            EnergyNetworkPacketPayload::buff,
            EnergyNetworkPacketPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void updateClientCache(BlockPos pos, int demand, int buff) {
        clientDemand = demand;
        clientBuff = buff;
        clientSaturation = buff - demand;
    }

    public static boolean send(BlockPos pos, int demand, int buff, ServerPlayer player) {
        ServerPlayNetworking.send(player, new EnergyNetworkPacketPayload(pos, demand, buff));
        return true;
    }
}
