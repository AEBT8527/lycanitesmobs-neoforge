package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessageMobEvent {
    public String mobEventName;
    public BlockPos pos;
    public int level = 1;
    public int subspecies = 1;

    public MessageMobEvent() {
    }

    public MessageMobEvent(String mobEventName, BlockPos pos, int level, int subspecies) {
        this.mobEventName = mobEventName;
        this.pos = pos;
        this.level = level;
        this.subspecies = subspecies;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageMobEvent message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() ->
                LycanitesMobs.PROXY.applyMobEvent(
                        message.mobEventName,
                        message.pos,
                        message.level,
                        message.subspecies));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageMobEvent decode(FriendlyByteBuf packet) {
        MessageMobEvent message = new MessageMobEvent();
        message.mobEventName = packet.readUtf(256);
        message.pos = packet.readBlockPos();
        message.level = packet.readInt();
        message.subspecies = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageMobEvent message, FriendlyByteBuf packet) {
        packet.writeUtf(message.mobEventName);
        packet.writeBlockPos(message.pos);
        packet.writeInt(message.level);
        packet.writeInt(message.subspecies);
    }

}
