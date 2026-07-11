package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessageEntityPerched {
    public int perchedOnEntityID;
    public int perchedByEntityID;

    public MessageEntityPerched() {
    }

    public MessageEntityPerched(Entity perchedOnEntityID, Entity perchedByEntity) {
        this.perchedOnEntityID = perchedOnEntityID.getId();
        this.perchedByEntityID = perchedByEntity != null ? perchedByEntity.getId() : 0;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageEntityPerched message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() ->
                LycanitesMobs.PROXY.applyEntityPerched(message.perchedOnEntityID, message.perchedByEntityID));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageEntityPerched decode(FriendlyByteBuf packet) {
        MessageEntityPerched message = new MessageEntityPerched();
        message.perchedOnEntityID = packet.readInt();
        message.perchedByEntityID = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageEntityPerched message, FriendlyByteBuf packet) {
        packet.writeInt(message.perchedOnEntityID);
        packet.writeInt(message.perchedByEntityID);
    }

}
