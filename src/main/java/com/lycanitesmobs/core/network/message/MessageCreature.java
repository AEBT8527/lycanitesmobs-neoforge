package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessageCreature {
    int entityID;
    int playerReputation = 0;

    public MessageCreature() {
    }

    public MessageCreature(BaseCreatureEntity creatureEntity, int playerReputation) {
        this.entityID = creatureEntity.getId();
        this.playerReputation = playerReputation;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageCreature message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() ->
                LycanitesMobs.PROXY.applyCreatureReputation(message.entityID, message.playerReputation));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageCreature decode(FriendlyByteBuf packet) {
        MessageCreature message = new MessageCreature();
        try {
            message.entityID = packet.readInt();
            message.playerReputation = packet.readInt();
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("There was a problem decoding the packet: " + packet + ".");
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageCreature message, FriendlyByteBuf packet) {
        packet.writeInt(message.entityID);
        packet.writeInt(message.playerReputation);
    }

}
