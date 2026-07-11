package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessageSummoningPedestalStats {
    public int capacity;
    public int progress;
    public int fuel;
    public int fuelMax;
    public int x;
    public int y;
    public int z;

    public MessageSummoningPedestalStats() {
    }

    public MessageSummoningPedestalStats(int capacity, int progress, int fuel, int fuelMax, int x, int y, int z) {
        this.capacity = capacity;
        this.progress = progress;
        this.fuel = fuel;
        this.fuelMax = fuelMax;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MessageSummoningPedestalStats(TileEntitySummoningPedestal.SummoningPedestalStats stats, BlockPos blockPos) {
        this(stats.capacity(), stats.summonProgress(), stats.summoningFuel(), stats.summoningFuelMax(),
                blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessageSummoningPedestalStats message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_CLIENT)
            return;

        ctx.get().enqueueWork(() -> LycanitesMobs.PROXY.applySummoningPedestalStats(message));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessageSummoningPedestalStats decode(FriendlyByteBuf packet) {
        MessageSummoningPedestalStats message = new MessageSummoningPedestalStats();
        message.x = packet.readInt();
        message.y = packet.readInt();
        message.z = packet.readInt();
        message.capacity = packet.readInt();
        message.progress = packet.readInt();
        message.fuel = packet.readInt();
        message.fuelMax = packet.readInt();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessageSummoningPedestalStats message, FriendlyByteBuf packet) {
        packet.writeInt(message.x);
        packet.writeInt(message.y);
        packet.writeInt(message.z);
        packet.writeInt(message.capacity);
        packet.writeInt(message.progress);
        packet.writeInt(message.fuel);
        packet.writeInt(message.fuelMax);
    }

}
