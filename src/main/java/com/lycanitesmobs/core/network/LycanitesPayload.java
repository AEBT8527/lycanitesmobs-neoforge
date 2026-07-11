package com.lycanitesmobs.core.network;

import com.lycanitesmobs.core.manager.PacketManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Single wire payload that carries every Lycanites network message. Instead of porting all ~22
 * message classes to NeoForge's per-payload system individually, they keep their original
 * encode/decode/handle static methods and are multiplexed here by a numeric message id (matching
 * the old {@code SimpleChannel.registerMessage} ids). {@link PacketManager} owns the id table and
 * the per-id encode/decode/handle dispatch.
 */
public record LycanitesPayload(int messageId, Object message) implements CustomPacketPayload {
    public static final Type<LycanitesPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("lycanitesmobs", "main"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LycanitesPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.messageId);
                PacketManager.encodeInner(payload.messageId, payload.message, buf);
            },
            (buf) -> {
                int id = buf.readVarInt();
                return new LycanitesPayload(id, PacketManager.decodeInner(id, buf));
            }
    );

    @Override
    public Type<LycanitesPayload> type() {
        return TYPE;
    }
}
