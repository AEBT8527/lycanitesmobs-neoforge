package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;


public class MessageCreatureKnowledge {
    public String creatureName;
    public int rank;
    public int experience;

    public MessageCreatureKnowledge() {
    }

    public MessageCreatureKnowledge(CreatureKnowledge creatureKnowledge) {
        this.creatureName = creatureKnowledge.getCreatureName();
        this.rank = creatureKnowledge.getRank();
        this.experience = creatureKnowledge.getExperience();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageCreatureKnowledge message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() ->
                LycanitesMobs.PROXY.applyCreatureKnowledge(
                        message.creatureName,
                        message.rank,
                        message.experience));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageCreatureKnowledge decode(FriendlyByteBuf packet) {
        MessageCreatureKnowledge message = new MessageCreatureKnowledge();
        try {
            message.creatureName = packet.readUtf(256);
            message.rank = packet.readInt();
            message.experience = packet.readInt();
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("There was a problem decoding the packet: " + packet + ".");
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageCreatureKnowledge message, FriendlyByteBuf packet) {
        packet.writeUtf(message.creatureName);
        packet.writeInt(message.rank);
        packet.writeInt(message.experience);
    }

}
