package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import com.lycanitesmobs.core.manager.PetManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.UUID;
import java.util.function.Supplier;

public class MessagePetEntryRemove {
    public UUID petEntryID;


    // ==================================================
    //                    Constructors
    // ==================================================
    public MessagePetEntryRemove() {
    }

    public MessagePetEntryRemove(ExtendedPlayer playerExt, PetEntry petEntry) {
        this.petEntryID = petEntry.getPetEntryID();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePetEntryRemove message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        // Server Side:
        if (ctx.get().getDirection() == NetworkDir.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                Player player = ctx.get().getSender();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);

                PetManager petManager = playerExt.getPetManager();
                PetEntry petEntry = petManager.getEntry(message.petEntryID);
                if (petEntry == null) {
                    LMHelperClass.logWarningMessage("Tried to remove a null PetEntry from server!");
                    return; // Nothing to remove!
                }
                petEntry.remove();
            });
            return;
        }

        // Client Side:
        ctx.get().enqueueWork(() -> LycanitesMobs.PROXY.removePetEntry(message.petEntryID));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePetEntryRemove decode(FriendlyByteBuf packet) {
        MessagePetEntryRemove message = new MessagePetEntryRemove();
        try {
            message.petEntryID = packet.readUUID();
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("There was a problem decoding the packet: " + packet + ".");
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePetEntryRemove message, FriendlyByteBuf packet) {
        try {
            packet.writeUUID(message.petEntryID);
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("There was a problem encoding the packet: " + packet + ".");
            e.printStackTrace();
        }
    }

}
