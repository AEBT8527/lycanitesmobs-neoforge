package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessageEntityPickedUp {
    public int pickedUpEntityID;
    public int pickedUpByEntityID;

    public MessageEntityPickedUp() {
    }

    public MessageEntityPickedUp(Entity pickedUpEntity, Entity pickedUpByEntity) {
        this.pickedUpEntityID = pickedUpEntity.getId();
        this.pickedUpByEntityID = pickedUpByEntity != null ? pickedUpByEntity.getId() : 0;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageEntityPickedUp message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() ->
                LycanitesMobs.PROXY.applyEntityPickedUp(message.pickedUpEntityID, message.pickedUpByEntityID));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageEntityPickedUp decode(FriendlyByteBuf packet) {
        MessageEntityPickedUp message = new MessageEntityPickedUp();
        message.pickedUpEntityID = packet.readInt();
        message.pickedUpByEntityID = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageEntityPickedUp message, FriendlyByteBuf packet) {
        packet.writeInt(message.pickedUpEntityID);
        packet.writeInt(message.pickedUpByEntityID);
    }

}
