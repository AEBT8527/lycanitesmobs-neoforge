package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessageSummonSet {
    public byte summonSetID;
    public int subpsecies;
    public int variant;
    public String summonType;
    public byte behaviour;

    public MessageSummonSet() {
    }

    public MessageSummonSet(ExtendedPlayer playerExt, byte summonSetID) {
        this.summonSetID = summonSetID;
        this.summonType = playerExt.getSummonSet(summonSetID).getSummonType();
        this.subpsecies = playerExt.getSummonSet(summonSetID).getSubspecies();
        this.variant = playerExt.getSummonSet(summonSetID).getVariant();
        this.behaviour = playerExt.getSummonSet(summonSetID).getBehaviourByte();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSummonSet message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        // Server Side:
        if (ctx.get().getDirection() == NetworkDir.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                Player player = ctx.get().getSender();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);

                SummonSet summonSet = playerExt.getSummonSet(message.summonSetID);
                summonSet.readFromPacket(message.summonType, message.subpsecies, message.variant, message.behaviour);
            });
            return;
        }

        // Client Side:
        ctx.get().enqueueWork(() -> LycanitesMobs.PROXY.applySummonSet(message));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSummonSet decode(FriendlyByteBuf packet) {
        MessageSummonSet message = new MessageSummonSet();
        message.summonSetID = packet.readByte();
        message.summonType = packet.readUtf(256);
        message.subpsecies = packet.readInt();
        message.variant = packet.readInt();
        message.behaviour = packet.readByte();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSummonSet message, FriendlyByteBuf packet) {
        packet.writeByte(message.summonSetID);
        packet.writeUtf(message.summonType);
        packet.writeInt(message.subpsecies);
        packet.writeInt(message.variant);
        packet.writeByte(message.behaviour);
    }

}
