package com.lycanitesmobs.client.network.proxy;

import com.lycanitesmobs.client.event.mobevent.ClientMobEventEvents;
import com.lycanitesmobs.client.extensions.LycanitesClientExtensions;
import com.lycanitesmobs.client.gui.screen.beastiary.IndexBeastiaryScreen;
import com.lycanitesmobs.client.gui.screen.beastiary.SummoningBeastiaryScreen;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.manager.PetManager;
import com.lycanitesmobs.core.network.message.MessagePetEntry;
import com.lycanitesmobs.core.network.message.MessageSummoningPedestalStats;
import com.lycanitesmobs.core.network.message.MessageSummonSet;
import com.lycanitesmobs.core.network.packet.MessageScreenRequest;
import com.lycanitesmobs.core.network.proxy.IProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import com.lycanitesmobs.LycanitesMobs;

import java.util.UUID;

public class ClientProxy implements IProxy {
    @Override
    public void registerEvents() {
        // ClientManager has no @SubscribeEvent methods; nothing to register on the mod bus.
    }

    @Override
    public Level getWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public void addEntityToWorld(int entityId, Entity entity) {
        entity.setId(entityId);
        Minecraft.getInstance().level.addEntity(entity);
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void openScreen(int screenId, Player player) {
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null) {
            return;
        }

        if (screenId == MessageScreenRequest.GuiRequest.BEASTIARY.id) {
            Minecraft.getInstance().setScreen(new IndexBeastiaryScreen(player));
        } else if (screenId == MessageScreenRequest.GuiRequest.SUMMONING.id) {
            Minecraft.getInstance().setScreen(new SummoningBeastiaryScreen(player));
        }
    }

    @Override
    public void applyCreatureReputation(int entityId, int playerReputation) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        Level world = player.level();
        if (world == null) {
            return;
        }

        Entity entity = world.getEntity(entityId);
        if (!(entity instanceof BaseCreatureEntity creatureEntity) || creatureEntity.getRelationships() == null) {
            return;
        }

        CreatureRelationshipEntry relationshipEntry = creatureEntity.getOrCreateRelationshipEntry(player);
        relationshipEntry.setReputation(playerReputation);
    }

    @Override
    public void applyPlayerStats(int spirit, int summonFocus, int creatureStudyCooldown) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null) {
            return;
        }

        playerExt.applyNetworkStats(spirit, summonFocus, creatureStudyCooldown);
    }

    @Override
    public void applyPetEntry(MessagePetEntry message) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null) {
            return;
        }

        PetManager petManager = playerExt.getPetManager();
        PetEntry petEntry = petManager.getEntry(message.petEntryID);
        if (petEntry == null) {
            petEntry = new PetEntry(message.petEntryID, message.petEntryType, player, message.summonType);
            petManager.addEntry(petEntry);
        }

        petEntry.setSpawningActive(message.spawningActive);
        if (message.teleportEntity) {
            petEntry.requestTeleport();
        }
        petEntry.setEntitySubspecies(message.subspecies);
        petEntry.setEntityVariant(message.variant);

        SummonSet summonSet = petEntry.getSummonSet();
        summonSet.readFromPacket(message.summonType, message.subspecies, message.variant, message.behaviour);

        Entity entity = null;
        if (message.petEntryEntityID != -1) {
            entity = player.level().getEntity(message.petEntryEntityID);
        }
        petEntry.applyClientSync(entity, message.petEntryEntityName, message.respawnTime, message.respawnTimeMax, message.entityLevel, message.entityExperience, message.isRespawning);
    }

    @Override
    public void removePetEntry(UUID petEntryId) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null) {
            return;
        }

        PetManager petManager = playerExt.getPetManager();
        PetEntry petEntry = petManager.getEntry(petEntryId);
        if (petEntry != null) {
            petEntry.remove();
        }
    }

    @Override
    public void applySummonSet(MessageSummonSet message) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null) {
            return;
        }

        SummonSet summonSet = playerExt.getSummonSet(message.summonSetID);
        summonSet.readFromPacket(message.summonType, message.subpsecies, message.variant, message.behaviour);
    }

    @Override
    public void applySelectedSummonSet(byte summonSetId) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt != null) {
            playerExt.setSelectedSummonSet(summonSetId);
        }
    }

    @Override
    public void applySummoningPedestalStats(MessageSummoningPedestalStats message) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        BlockEntity tileEntity = player.level().getBlockEntity(new BlockPos(message.x, message.y, message.z));
        if (!(tileEntity instanceof TileEntitySummoningPedestal summoningPedestal)) {
            return;
        }

        summoningPedestal.applyNetworkStats(message.capacity, message.progress, message.fuel, message.fuelMax);
    }

    @Override
    public void applyEntityPerched(int perchedOnEntityId, int perchedByEntityId) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        Level world = player.level();
        Entity perchedOnEntity = world.getEntity(perchedOnEntityId);
        Entity perchedByEntity = perchedByEntityId != 0 ? world.getEntity(perchedByEntityId) : null;

        if (!(perchedOnEntity instanceof LivingEntity livingEntity)) {
            return;
        }

        ExtendedEntity perchedOnEntityExt = ExtendedEntity.getForEntity(livingEntity);
        if (perchedOnEntityExt != null) {
            perchedOnEntityExt.setPerchedByEntity(perchedByEntity);
        }
    }

    @Override
    public void applyEntityPickedUp(int pickedUpEntityId, int pickedUpByEntityId) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        Level world = player.level();
        Entity pickedUpEntity = world.getEntity(pickedUpEntityId);
        Entity pickedUpByEntity = pickedUpByEntityId != 0 ? world.getEntity(pickedUpByEntityId) : null;

        if (!(pickedUpEntity instanceof LivingEntity livingEntity)) {
            return;
        }

        ExtendedEntity pickedUpEntityExt = ExtendedEntity.getForEntity(livingEntity);
        if (pickedUpEntityExt != null) {
            pickedUpEntityExt.setPickedUpByEntity(pickedUpByEntity);
        }
    }

    @Override
    public void applyEntityVelocity(int entityId, int motionX, int motionY, int motionZ) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        Entity entity = player.level().getEntity(entityId);
        if (entity == null) {
            return;
        }

        entity.setDeltaMovement(entity.getDeltaMovement().add(
                (double) motionX / 8000.0D,
                (double) motionY / 8000.0D,
                (double) motionZ / 8000.0D));
    }

    @Override
    public void applyMobEvent(String mobEventName, BlockPos pos, int level, int subspecies) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }
        ClientMobEventEvents.applyMobEvent(player.level(), mobEventName);
    }

    @Override
    public void applyWorldEvent(String mobEventName, BlockPos pos, int level, int subspecies) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }
        ClientMobEventEvents.applyWorldEvent(player.level(), mobEventName);
    }

    @Override
    public void applyBeastiaryEntries(String[] creatureNames, int[] ranks, int[] experience, int entryAmount) {
        Player player = this.getClientPlayer();
        if (player == null || entryAmount < 0) {
            return;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null) {
            return;
        }

        playerExt.getBeastiary().clearCreatureKnowledge();
        for (int i = 0; i < entryAmount; i++) {
            CreatureKnowledge creatureKnowledge = new CreatureKnowledge(
                    playerExt.getBeastiary(),
                    creatureNames[i],
                    ranks[i],
                    experience[i]);
            playerExt.getBeastiary().putCreatureKnowledge(creatureKnowledge);
        }
    }

    @Override
    public void applyCreatureKnowledge(String creatureName, int rank, int experience) {
        Player player = this.getClientPlayer();
        if (player == null) {
            return;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt == null) {
            return;
        }

        playerExt.getBeastiary().addCreatureKnowledge(
                new CreatureKnowledge(playerExt.getBeastiary(), creatureName, rank, experience),
                false);
    }

    @Override
    public void showOverlayMessage(Component message) {
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    @Override
    public boolean isShiftKeyDown() {
        return Screen.hasShiftDown();
    }

    @Override
    public IClientItemExtensions createEquipmentItemExtensions() {
        return LycanitesClientExtensions.createEquipmentItemExtensions();
    }

    @Override
    public IClientItemExtensions createEquipmentPartItemExtensions() {
        return LycanitesClientExtensions.createEquipmentPartItemExtensions();
    }
}
