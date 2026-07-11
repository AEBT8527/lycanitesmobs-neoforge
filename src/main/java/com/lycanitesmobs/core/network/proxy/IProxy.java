package com.lycanitesmobs.core.network.proxy;

import com.lycanitesmobs.core.network.message.MessagePetEntry;
import com.lycanitesmobs.core.network.message.MessageSummoningPedestalStats;
import com.lycanitesmobs.core.network.message.MessageSummonSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface IProxy {
    void registerEvents();

    Level getWorld();

    void addEntityToWorld(int entityId, Entity entity);

    public Player getClientPlayer();

    public void openScreen(int screenId, Player player);

    void applyCreatureReputation(int entityId, int playerReputation);

    void applyPlayerStats(int spirit, int summonFocus, int creatureStudyCooldown);

    void applyPetEntry(MessagePetEntry message);

    void removePetEntry(UUID petEntryId);

    void applySummonSet(MessageSummonSet message);

    void applySelectedSummonSet(byte summonSetId);

    void applySummoningPedestalStats(MessageSummoningPedestalStats message);

    void applyEntityPerched(int perchedOnEntityId, int perchedByEntityId);

    void applyEntityPickedUp(int pickedUpEntityId, int pickedUpByEntityId);

    void applyEntityVelocity(int entityId, int motionX, int motionY, int motionZ);

    void applyMobEvent(String mobEventName, BlockPos pos, int level, int subspecies);

    void applyWorldEvent(String mobEventName, BlockPos pos, int level, int subspecies);

    void applyBeastiaryEntries(String[] creatureNames, int[] ranks, int[] experience, int entryAmount);

    void applyCreatureKnowledge(String creatureName, int rank, int experience);

    void showOverlayMessage(Component message);

    boolean isShiftKeyDown();

}
