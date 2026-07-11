package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessageOverlayMessage {
    public Component message;

    public MessageOverlayMessage() {
    }

    public MessageOverlayMessage(MutableComponent message) {
        this.message = message;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageOverlayMessage message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() -> LycanitesMobs.PROXY.showOverlayMessage(message.message));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageOverlayMessage decode(FriendlyByteBuf packet) {
        MessageOverlayMessage message = new MessageOverlayMessage();
        message.message = net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf) packet);
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageOverlayMessage message, FriendlyByteBuf packet) {
        net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf) packet, message.message);
    }

}
