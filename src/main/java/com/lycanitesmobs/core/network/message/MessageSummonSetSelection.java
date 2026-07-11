package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessageSummonSetSelection {
    public byte summonSetID;

    public MessageSummonSetSelection() {
    }

    public MessageSummonSetSelection(ExtendedPlayer playerExt) {
        this.summonSetID = (byte) playerExt.getSelectedSummonSetId();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSummonSetSelection message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        // Server Side:
        if (ctx.get().getDirection() == NetworkDir.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                Player player = ctx.get().getSender();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
                playerExt.setSelectedSummonSet(message.summonSetID);
            });
            return;
        }

        // Client Side:
        ctx.get().enqueueWork(() -> LycanitesMobs.PROXY.applySelectedSummonSet(message.summonSetID));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSummonSetSelection decode(FriendlyByteBuf packet) {
        MessageSummonSetSelection message = new MessageSummonSetSelection();
        message.summonSetID = packet.readByte();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSummonSetSelection message, FriendlyByteBuf packet) {
        packet.writeByte(message.summonSetID);
    }

}
