package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import net.minecraft.network.FriendlyByteBuf;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessagePlayerStats {
    public int spirit;
    public int summonFocus;
    public int creatureStudyCooldown;

    public MessagePlayerStats() {
    }

    public MessagePlayerStats(ExtendedPlayer playerExt) {
        this.spirit = playerExt.getSpirit();
        this.summonFocus = playerExt.getSummonFocus();
        this.creatureStudyCooldown = playerExt.getCreatureStudyCooldown();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePlayerStats message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() ->
                LycanitesMobs.PROXY.applyPlayerStats(
                        message.spirit,
                        message.summonFocus,
                        message.creatureStudyCooldown));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePlayerStats decode(FriendlyByteBuf packet) {
        MessagePlayerStats message = new MessagePlayerStats();
        message.spirit = packet.readInt();
        message.summonFocus = packet.readInt();
        message.creatureStudyCooldown = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePlayerStats message, FriendlyByteBuf packet) {
        packet.writeInt(message.spirit);
        packet.writeInt(message.summonFocus);
        packet.writeInt(message.creatureStudyCooldown);
    }

}
