package com.lycanitesmobs.core.network;

/**
 * Small stand-in for Forge's old {@code NetworkDir}. NeoForge's payload system determines
 * direction from {@link net.minecraft.network.protocol.PacketFlow}; this enum keeps the message
 * handlers' {@code ctx.get().getDirection() == NetworkDir.PLAY_TO_SERVER} checks readable.
 */
public enum NetworkDir {
    PLAY_TO_SERVER,
    PLAY_TO_CLIENT
}
