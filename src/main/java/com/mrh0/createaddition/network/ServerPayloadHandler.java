package com.mrh0.createaddition.network;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ServerPayloadHandler {
    public static void handleObservePayload(ObservePacketPayload pkt, ServerPlayer player) {
        BlockEntity be = player.level().getBlockEntity(pkt.pos());
        if (be instanceof IObserveBlockEntity ote) {
            ote.onObserved(player, pkt);
            Packet<ClientGamePacketListener> updatePacket = be.getUpdatePacket();
            if (updatePacket != null) player.connection.send(updatePacket);
        }
    }
}
