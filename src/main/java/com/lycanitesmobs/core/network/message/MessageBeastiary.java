package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.gui.Beastiary;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessageBeastiary {
    public int entryAmount = 0;
    public String[] creatureNames;
    public int[] ranks;
    public int[] experience;

    public MessageBeastiary() {
    }

    public MessageBeastiary(Beastiary beastiary) {
        this.entryAmount = Math.min(201, beastiary.getCreatureKnowledgeCount());
        if (this.entryAmount > 0) {
            this.creatureNames = new String[this.entryAmount];
            this.ranks = new int[this.entryAmount];
            this.experience = new int[this.entryAmount];
            int i = 0;
            for (CreatureKnowledge creatureKnowledge : beastiary.getCreatureKnowledgeValues()) {
                this.creatureNames[i] = creatureKnowledge.getCreatureName();
                this.ranks[i] = creatureKnowledge.getRank();
                this.experience[i] = creatureKnowledge.getExperience();
                i++;
            }
        }
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageBeastiary message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() ->
                LycanitesMobs.PROXY.applyBeastiaryEntries(
                        message.creatureNames,
                        message.ranks,
                        message.experience,
                        message.entryAmount));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageBeastiary decode(FriendlyByteBuf packet) {
        MessageBeastiary message = new MessageBeastiary();
        message.entryAmount = Math.min(300, packet.readInt());
        if (message.entryAmount == 300) {
            LMHelperClass.logWarningMessage("Received 300 or more creature entries, something went wrong with the Beastiary packet! Addition entries will be skipped to prevent OOM!");
        }
        if (message.entryAmount > 0) {
            message.creatureNames = new String[message.entryAmount];
            message.ranks = new int[message.entryAmount];
            message.experience = new int[message.entryAmount];
            for (int i = 0; i < message.entryAmount; i++) {
                message.creatureNames[i] = packet.readUtf(32767);
                message.ranks[i] = packet.readInt();
                message.experience[i] = packet.readInt();
            }
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageBeastiary message, FriendlyByteBuf packet) {
        packet.writeInt(message.entryAmount);
        if (message.entryAmount > 0) {
            for (int i = 0; i < message.entryAmount; i++) {
                packet.writeUtf(message.creatureNames[i]);
                packet.writeInt(message.ranks[i]);
                packet.writeInt(message.experience[i]);
            }
        }
    }
}
