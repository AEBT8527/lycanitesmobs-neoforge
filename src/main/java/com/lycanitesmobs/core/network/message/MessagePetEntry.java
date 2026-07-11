package com.lycanitesmobs.core.network.message;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import com.lycanitesmobs.core.manager.PetManager;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;

import java.util.UUID;
import java.util.function.Supplier;

public class MessagePetEntry {
    public UUID petEntryID;
    public String petEntryType;
    public boolean spawningActive;
    public boolean teleportEntity;
    public String summonType;
    public int subspecies;
    public int variant;
    public byte behaviour;
    public int petEntryEntityID = -1;
    public String petEntryEntityName;
    public int respawnTime;
    public int respawnTimeMax;
    public int entityLevel;
    public int entityExperience;
    public boolean isRespawning;

    public MessagePetEntry() {
    }

    public MessagePetEntry(ExtendedPlayer playerExt, PetEntry petEntry) {
        this.petEntryID = petEntry.getPetEntryID();
        this.petEntryType = petEntry.getType();
        this.spawningActive = petEntry.isSpawningActive();
        this.teleportEntity = petEntry.isTeleportRequested();
        SummonSet summonSet = petEntry.getSummonSet();
        this.summonType = summonSet.getSummonType();
        this.subspecies = petEntry.getSubspeciesIndex();
        this.variant = petEntry.getVariantIndex();
        this.behaviour = summonSet.getBehaviourByte();
        this.petEntryEntityID = petEntry.getEntity() != null ? petEntry.getEntity().getId() : -1;
        this.petEntryEntityName = petEntry.getEntityName();
        this.respawnTime = petEntry.getRespawnTime();
        this.respawnTimeMax = petEntry.getRespawnTimeMax();
        this.entityLevel = petEntry.getLevel();
        this.entityExperience = petEntry.getExperience();
        this.isRespawning = petEntry.isRespawning();
    }

    /**
     * Called when this message is received.
     */
    public static void handle(MessagePetEntry message, Supplier<PacketContext> ctx) {
        ctx.get().setPacketHandled(true);
        // Server Side:
        if (ctx.get().getDirection() == NetworkDir.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                Player player = ctx.get().getSender();
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
                PetManager petManager = playerExt.getPetManager();
                PetEntry petEntry = petManager.getEntry(message.petEntryID);
                if (petEntry == null)
                    return;
                petEntry.setSpawningActive(message.spawningActive);
                if (message.teleportEntity) {
                    petEntry.requestTeleport();
                }
                SummonSet summonSet = petEntry.getSummonSet();
                summonSet.readFromPacket(message.summonType, message.subspecies, message.variant, message.behaviour);
                petEntry.onBehaviourUpdate();
            });
            return;
        }

        // Client Side:
        ctx.get().enqueueWork(() -> LycanitesMobs.PROXY.applyPetEntry(message));
    }

    /**
     * Reads the message from bytes.
     */
    public static MessagePetEntry decode(FriendlyByteBuf packet) {
        MessagePetEntry message = new MessagePetEntry();
        message.petEntryID = packet.readUUID();
        message.petEntryType = packet.readUtf(512);
        message.spawningActive = packet.readBoolean();
        message.teleportEntity = packet.readBoolean();
        message.summonType = packet.readUtf(512);
        message.subspecies = packet.readInt();
        message.variant = packet.readInt();
        message.behaviour = packet.readByte();
        message.petEntryEntityID = packet.readInt();
        message.petEntryEntityName = packet.readUtf(1024);
        message.respawnTime = packet.readInt();
        message.respawnTimeMax = packet.readInt();
        message.entityLevel = packet.readInt();
        message.entityExperience = packet.readInt();
        message.isRespawning = packet.readBoolean();
        return message;
    }

    /**
     * Writes the message into bytes.
     */
    public static void encode(MessagePetEntry message, FriendlyByteBuf packet) {
        packet.writeUUID(message.petEntryID);
        packet.writeUtf(message.petEntryType);
        packet.writeBoolean(message.spawningActive);
        packet.writeBoolean(message.teleportEntity);
        packet.writeUtf(message.summonType);
        packet.writeInt(message.subspecies);
        packet.writeInt(message.variant);
        packet.writeByte(message.behaviour);
        packet.writeInt(message.petEntryEntityID);
        packet.writeUtf(message.petEntryEntityName);
        packet.writeInt(message.respawnTime);
        packet.writeInt(message.respawnTimeMax);
        packet.writeInt(message.entityLevel);
        packet.writeInt(message.entityExperience);
        packet.writeBoolean(message.isRespawning);
    }

}
