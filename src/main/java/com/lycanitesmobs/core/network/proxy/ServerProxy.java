package com.lycanitesmobs.core.network.proxy;


import com.lycanitesmobs.core.network.message.MessagePetEntry;
import com.lycanitesmobs.core.network.message.MessageSummoningPedestalStats;
import com.lycanitesmobs.core.network.message.MessageSummonSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.UUID;

public class ServerProxy implements IProxy {
    @Override
    public void registerEvents() {

    }

    @Override
    public Level getWorld() {
        return null;
    }

    @Override
    public void addEntityToWorld(int entityId, Entity entity) {

    }

    @Override
    public Player getClientPlayer() {
        return null;
    }

    @Override
    public void openScreen(int screenId, Player player) {
    }

    @Override
    public void applyCreatureReputation(int entityId, int playerReputation) {
    }

    @Override
    public void applyPlayerStats(int spirit, int summonFocus, int creatureStudyCooldown) {
    }

    @Override
    public void applyPetEntry(MessagePetEntry message) {
    }

    @Override
    public void removePetEntry(UUID petEntryId) {
    }

    @Override
    public void applySummonSet(MessageSummonSet message) {
    }

    @Override
    public void applySelectedSummonSet(byte summonSetId) {
    }

    @Override
    public void applySummoningPedestalStats(MessageSummoningPedestalStats message) {
    }

    @Override
    public void applyEntityPerched(int perchedOnEntityId, int perchedByEntityId) {
    }

    @Override
    public void applyEntityPickedUp(int pickedUpEntityId, int pickedUpByEntityId) {
    }

    @Override
    public void applyEntityVelocity(int entityId, int motionX, int motionY, int motionZ) {
    }

    @Override
    public void applyMobEvent(String mobEventName, BlockPos pos, int level, int subspecies) {
    }

    @Override
    public void applyWorldEvent(String mobEventName, BlockPos pos, int level, int subspecies) {
    }

    @Override
    public void applyBeastiaryEntries(String[] creatureNames, int[] ranks, int[] experience, int entryAmount) {
    }

    @Override
    public void applyCreatureKnowledge(String creatureName, int rank, int experience) {
    }

    @Override
    public void showOverlayMessage(Component message) {
    }

    @Override
    public boolean isShiftKeyDown() {
        return false;
    }

    @Override
    public IClientItemExtensions createEquipmentItemExtensions() {
        return new IClientItemExtensions() {
        };
    }

    @Override
    public IClientItemExtensions createEquipmentPartItemExtensions() {
        return new IClientItemExtensions() {
        };
    }
}
