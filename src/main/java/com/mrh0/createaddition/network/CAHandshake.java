package com.mrh0.createaddition.network;

import com.mrh0.createaddition.CreateAddition;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public final class CAHandshake {
    public static final int PROTOCOL = 1;
    public static final ConfigurationTask.Type TASK_TYPE = new ConfigurationTask.Type(HandshakePayload.TYPE.id().toString());
    public static final String KEY_CLIENT_TOO_OLD = "createaddition.handshake.client_too_old";
    public static final String KEY_MISMATCH = "createaddition.handshake.mismatch";
    public static final String KEY_MISMATCH_BUILD = "createaddition.handshake.mismatch_build";
    public static final String KEY_TIMEOUT = "createaddition.handshake.timeout";
    public static final String KEY_SERVER_TOO_OLD = "createaddition.handshake.server_too_old";
    private static final String FALLBACK_CLIENT_TOO_OLD = "This server runs Crafts & Additions %s and your client runs an older version. Install the same Crafts & Additions version as the server.";
    private static final String FALLBACK_MISMATCH = "This server runs Crafts & Additions %s and your client runs %s. Install the same Crafts & Additions version as the server.";
    private static final String FALLBACK_MISMATCH_BUILD = "This server and your client both run Crafts & Additions %s, but their blocks differ. Install the same Crafts & Additions build as the server.";
    private static final String FALLBACK_TIMEOUT = "Your client did not report its Crafts & Additions version. Install the same Crafts & Additions version as the server (%s).";
    public static final String FALLBACK_SERVER_TOO_OLD = "This server runs an older Crafts & Additions than your client (%s). Use the same Crafts & Additions version on both.";
    private static final int TIMEOUT_TICKS = 600;
    private static final Set<ServerConfigurationPacketListenerImpl> PENDING = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private static HandshakePayload local;

    private CAHandshake() {
    }

    public static void register() {
        local = new HandshakePayload(PROTOCOL, modVersion(), fingerprint());
        PayloadTypeRegistry.clientboundConfiguration().register(HandshakePayload.TYPE, HandshakePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(HandshakePayload.TYPE, HandshakePayload.STREAM_CODEC);
        ServerConfigurationNetworking.registerGlobalReceiver(HandshakePayload.TYPE, CAHandshake::receive);
        ServerConfigurationConnectionEvents.CONFIGURE.register(CAHandshake::configure);
        CreateAddition.LOGGER.info("Crafts & Additions {} block fingerprint {}", local.version(), hex(local.fingerprint()));
    }

    public static HandshakePayload local() {
        return local;
    }

    public static boolean matches(HandshakePayload other) {
        return other.protocol() == local.protocol() && other.fingerprint() == local.fingerprint();
    }

    public static String hex(long fingerprint) {
        return String.format("%016x", fingerprint);
    }

    private static void configure(ServerConfigurationPacketListenerImpl handler, MinecraftServer server) {
        if (!ServerConfigurationNetworking.canSend(handler, HandshakePayload.TYPE)) {
            CreateAddition.LOGGER.warn("Refused {}: the client has no Crafts & Additions handshake, so it runs a version older than the server's {}", playerName(handler), local.version());
            handler.disconnect(Component.translatableWithFallback(KEY_CLIENT_TOO_OLD, FALLBACK_CLIENT_TOO_OLD, local.version()));
            return;
        }
        handler.addTask(new PendingTask(handler));
    }

    private static void receive(HandshakePayload payload, ServerConfigurationNetworking.Context context) {
        ServerConfigurationPacketListenerImpl handler = context.packetListener();
        context.server().execute(() -> verify(handler, payload));
    }

    private static void verify(ServerConfigurationPacketListenerImpl handler, HandshakePayload remote) {
        if (!PENDING.remove(handler)) return;
        if (matches(remote)) {
            CreateAddition.LOGGER.debug("Crafts & Additions handshake matched for {}: {}", playerName(handler), remote.version());
            handler.completeTask(TASK_TYPE);
            return;
        }
        CreateAddition.LOGGER.warn("Refused {}: client Crafts & Additions {} (protocol {}, blocks {}), server {} (protocol {}, blocks {})",
                playerName(handler), remote.version(), remote.protocol(), hex(remote.fingerprint()),
                local.version(), local.protocol(), hex(local.fingerprint()));
        Component reason = remote.version().equals(local.version())
                ? Component.translatableWithFallback(KEY_MISMATCH_BUILD, FALLBACK_MISMATCH_BUILD, local.version())
                : Component.translatableWithFallback(KEY_MISMATCH, FALLBACK_MISMATCH, local.version(), remote.version());
        handler.disconnect(reason);
    }

    private static String playerName(ServerConfigurationPacketListenerImpl handler) {
        return handler.getOwner().name();
    }

    private static String modVersion() {
        return FabricLoader.getInstance().getModContainer(CreateAddition.MODID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    private static long fingerprint() {
        Map<String, Block> blocks = new TreeMap<>();
        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(CreateAddition.MODID))
                blocks.put(entry.getKey().identifier().toString(), entry.getValue());
        }
        StringBuilder description = new StringBuilder();
        blocks.forEach((id, block) -> {
            StateDefinition<Block, ?> definition = block.getStateDefinition();
            description.append(id).append('{');
            for (Property<?> property : definition.getProperties()) {
                description.append(property.getName()).append('=');
                appendValues(description, property);
                description.append(';');
            }
            description.append(definition.getPossibleStates().size()).append('}');
        });
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(description.toString().getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest).getLong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T extends Comparable<T>> void appendValues(StringBuilder description, Property<T> property) {
        for (T value : property.getPossibleValues())
            description.append(property.getName(value)).append(',');
    }

    private static final class PendingTask implements ConfigurationTask {
        private final ServerConfigurationPacketListenerImpl handler;
        private int ticks;

        private PendingTask(ServerConfigurationPacketListenerImpl handler) {
            this.handler = handler;
        }

        @Override
        public void start(Consumer<Packet<?>> sender) {
            PENDING.add(handler);
            sender.accept(ServerConfigurationNetworking.createClientboundPacket(local));
        }

        @Override
        public boolean tick() {
            if (++ticks == TIMEOUT_TICKS && PENDING.remove(handler)) {
                CreateAddition.LOGGER.warn("Refused {}: no Crafts & Additions handshake reply within {} ticks", playerName(handler), TIMEOUT_TICKS);
                handler.disconnect(Component.translatableWithFallback(KEY_TIMEOUT, FALLBACK_TIMEOUT, local.version()));
            }
            return false;
        }

        @Override
        public Type type() {
            return TASK_TYPE;
        }
    }
}
