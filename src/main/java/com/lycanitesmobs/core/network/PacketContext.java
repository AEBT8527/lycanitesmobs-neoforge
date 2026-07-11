package com.lycanitesmobs.core.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Compatibility wrapper that presents NeoForge's {@link IPayloadContext} using the small surface
 * the ported Lycanites message handlers expect from Forge's old {@code PacketContext}
 * ({@code getSender()}, {@code enqueueWork()}, {@code getDirection()}, {@code setPacketHandled()}).
 */
public class PacketContext {
    private final IPayloadContext context;
    private final NetworkDir direction;

    public PacketContext(IPayloadContext context, NetworkDir direction) {
        this.context = context;
        this.direction = direction;
    }

    /** No-op: NeoForge's payload pipeline marks packets handled automatically. */
    public void setPacketHandled(boolean handled) {
    }

    public NetworkDir getDirection() {
        return this.direction;
    }

    public void enqueueWork(Runnable task) {
        this.context.enqueueWork(task);
    }

    /** The sending player when server-bound, otherwise null (mirrors Forge's getSender). */
    public ServerPlayer getSender() {
        return this.context.player() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    public IPayloadContext raw() {
        return this.context;
    }
}
