package com.mrh0.createaddition.network;

import com.mrh0.createaddition.CreateAddition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HandshakePayload(int protocol, String version, long fingerprint) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HandshakePayload> TYPE = new CustomPacketPayload.Type<>(CreateAddition.asResource("handshake"));

    public static final StreamCodec<ByteBuf, HandshakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            HandshakePayload::protocol,
            ByteBufCodecs.stringUtf8(256),
            HandshakePayload::version,
            ByteBufCodecs.LONG,
            HandshakePayload::fingerprint,
            HandshakePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
