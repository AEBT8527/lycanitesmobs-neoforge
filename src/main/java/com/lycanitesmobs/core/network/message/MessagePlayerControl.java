package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.function.Supplier;

public class MessagePlayerControl {
    public byte controlStates;

    public MessagePlayerControl() {
    }

    public MessagePlayerControl(byte controlStates) {
        this.controlStates = controlStates;
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePlayerControl message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        if (ctx.get().getDirection() != NetworkDir.PLAY_TO_SERVER)
            return;

        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            playerExt.updateControlStates(message.controlStates);
            Entity vehicle = player.getVehicle();
            if (vehicle instanceof RideableCreatureEntity rideableCreature
                    && rideableCreature.getControllingPassenger() == player
                    && rideableCreature.riderControl()) {
                rideableCreature.handleRiderControls(player, playerExt);
            }
        });
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePlayerControl decode(FriendlyByteBuf packet) {
        MessagePlayerControl message = new MessagePlayerControl();
        message.controlStates = packet.readByte();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePlayerControl message, FriendlyByteBuf packet) {
        packet.writeByte(message.controlStates);
    }

}
