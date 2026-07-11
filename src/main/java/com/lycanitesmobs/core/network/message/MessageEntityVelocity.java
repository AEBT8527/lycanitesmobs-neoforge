package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.math.SchismMath;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;


public class MessageEntityVelocity {
    public int entityID;
    public int motionX;
    public int motionY;
    public int motionZ;

    public MessageEntityVelocity() {
    }

    public MessageEntityVelocity(Entity entity, double motionX, double motionY, double motionZ) {
        this.entityID = entity.getId();

        motionX = SchismMath.clamp(motionX, -3.9D, 3.9D);
        motionY = SchismMath.clamp(motionY, -3.9D, 3.9D);
        motionZ = SchismMath.clamp(motionZ, -3.9D, 3.9D);

        this.motionX = (int) (motionX * 8000.0D);
        this.motionY = (int) (motionY * 8000.0D);
        this.motionZ = (int) (motionZ * 8000.0D);
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageEntityVelocity message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() ->
                LycanitesMobs.PROXY.applyEntityVelocity(
                        message.entityID,
                        message.motionX,
                        message.motionY,
                        message.motionZ));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageEntityVelocity decode(FriendlyByteBuf packet) {
        MessageEntityVelocity message = new MessageEntityVelocity();
        message.entityID = packet.readVarInt();
        message.motionX = packet.readShort();
        message.motionY = packet.readShort();
        message.motionZ = packet.readShort();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageEntityVelocity message, FriendlyByteBuf packet) {
        packet.writeVarInt(message.entityID);
        packet.writeShort(message.motionX);
        packet.writeShort(message.motionY);
        packet.writeShort(message.motionZ);
    }
}