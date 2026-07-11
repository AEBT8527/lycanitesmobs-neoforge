package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import com.lycanitesmobs.core.entity.pets.PlayerFamiliars;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class PetManager {
    protected LivingEntity host;
    /**
     * A list of all pet entries, useful for looking up everything summoned by an entity as well as ensuring that no entries are added as multiple types.
     **/
    protected Map<UUID, PetEntry> entries = new HashMap<>();
    /**
     * Newly added PetEntries that need to be synced to the client player.
     **/
    protected List<PetEntry> newEntries = new ArrayList<>();
    /**
     * PetEntries that need to be removed.
     **/
    protected List<PetEntry> removedEntries = new ArrayList<>();
    /**
     * A map containing NBT Tag Compounds mapped to Pet Entry UUIDs.
     **/
    protected Map<UUID, CompoundTag> entryNBTs = new HashMap<>();


    public PetManager(LivingEntity host) {
        this.host = host;
    }

    /**
     * Returns true if the provided entry is in this manager.
     **/
    public boolean hasEntry(PetEntry petEntry) {
        return this.entries.containsKey(petEntry.getPetEntryID());
    }

    public boolean hasEntries() {
        return !this.entries.isEmpty();
    }

    public LivingEntity getHost() {
        return this.host;
    }

    public Collection<PetEntry> getEntries() {
        return Collections.unmodifiableCollection(this.entries.values());
    }

    Collection<PetEntry> getEntrySnapshot() {
        return new ArrayList<>(this.entries.values());
    }

    boolean hasPendingEntryNBTs() {
        return !this.entryNBTs.isEmpty();
    }

    CompoundTag takePendingEntryNBT(UUID entryId) {
        return this.entryNBTs.remove(entryId);
    }

    Collection<CompoundTag> getPendingEntryNBTs() {
        return new ArrayList<>(this.entryNBTs.values());
    }

    void clearPendingEntryNBTs() {
        this.entryNBTs.clear();
    }

    void addPendingEntryNBT(UUID entryId, CompoundTag entryNBT) {
        this.entryNBTs.put(entryId, entryNBT);
    }

    boolean hasNewEntries() {
        return !this.newEntries.isEmpty();
    }

    Collection<PetEntry> getNewEntries() {
        return new ArrayList<>(this.newEntries);
    }

    void clearNewEntries() {
        this.newEntries.clear();
    }

    void markEntryRemoved(PetEntry petEntry) {
        this.removedEntries.add(petEntry);
    }

    boolean hasRemovedEntries() {
        return !this.removedEntries.isEmpty();
    }

    Collection<PetEntry> getRemovedEntries() {
        return new ArrayList<>(this.removedEntries);
    }

    void clearRemovedEntries() {
        this.removedEntries.clear();
    }

    public void setHost(LivingEntity host) {
        this.host = host;
    }

    /**
     * Adds a new PetEntry and executes onAdd() methods. The provided entry should have set whether it's a pet, mount, minion, etc.
     **/
    public void addEntry(PetEntry petEntry) {
        if (this.entries.containsKey(petEntry.getPetEntryID())) {
            LMHelperClass.logWarningMessage("[Pet Manager] Tried to add a Pet Entry that is already added!");
            return;
        }

        // Load From NBT:
        if (this.entryNBTs.containsKey(petEntry.getPetEntryID())) {
            petEntry.readFromNBT(this.entryNBTs.get(petEntry.getPetEntryID()));
        }

        this.entries.put(petEntry.getPetEntryID(), petEntry);
        petEntry.onAdd(this);
        this.newEntries.add(petEntry);
    }

    /**
     * Removes an entry from this manager. This is called automatically if the entry itself is no longer active.
     * This will not cause the entry itself to become inactive if it is still active.
     * If an entry is finished, it is best to call onRemove() on the entry itself, this method will then be called automatically.
     **/
    public void removeEntry(PetEntry petEntry) {
        if (!this.entries.containsValue(petEntry)) {
            LMHelperClass.logWarningMessage("[Pet Manager] Tried to remove a pet entry that isn't added!");
            return;
        }

        this.entries.remove(petEntry.getPetEntryID());
    }

    /**
     * Returns the requested pet entry from its specific id.
     **/
    public PetEntry getEntry(UUID id) {
        return this.entries.get(id);
    }

    /**
     * Returns the requested entry list.
     **/
    public List<PetEntry> createEntryListByType(String type) {
        List<PetEntry> filteredEntries = new ArrayList<>();
        for (PetEntry petEntry : this.entries.values()) {
            if (type.equalsIgnoreCase(petEntry.getType())) {
                filteredEntries.add(petEntry);
            }
        }
        return filteredEntries;
    }

    /**
     * Called by the host's entity update, runs any logic to manage pet entries.
     **/
    public void onUpdate(Level world) {
        if (this.host == null)
            return;

        this.applyPendingEntryNBT();
        this.syncNewEntries(world);

        int newSpiritReserved = this.updateEntries(world);
        this.removeInactiveEntries();
        this.updateSpiritReserved(newSpiritReserved);
    }

    private void applyPendingEntryNBT() {
        if (!this.hasPendingEntryNBTs()) {
            return;
        }

        for (PetEntry petEntry : this.getEntrySnapshot()) {
            CompoundTag pendingNBT = this.takePendingEntryNBT(petEntry.getPetEntryID());
            if (pendingNBT != null) {
                petEntry.readFromNBT(pendingNBT);
            }
        }

        for (CompoundTag nbtEntry : this.getPendingEntryNBTs()) {
            if (this.host instanceof Player && nbtEntry.getString("Type").equalsIgnoreCase("familiar")) {
                if (!PlayerFamiliars.INSTANCE.hasLoadedFamiliar(this.host.getUUID(), nbtEntry.getUUID("UUID"))) {
                    continue;
                }
            }
            PetEntry petEntry = new PetEntry(nbtEntry.getUUID("UUID"), nbtEntry.getString("Type"), this.host, nbtEntry.getString("SummonType"));
            petEntry.readFromNBT(nbtEntry);
            if (petEntry.isEntryActive())
                this.addEntry(petEntry);
        }

        this.clearPendingEntryNBTs();
    }

    private void syncNewEntries(Level world) {
        if (!this.hasNewEntries()) {
            return;
        }

        if (!world.isClientSide && this.host instanceof Player player) {
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            if (playerExt != null) {
                for (PetEntry petEntry : this.getNewEntries()) {
                    playerExt.sendPetEntryToPlayer(petEntry);
                }
            }
        }
        this.clearNewEntries();
    }

    private int updateEntries(Level world) {
        int newSpiritReserved = 0;
        ExtendedPlayer playerExt = this.host instanceof Player player ? ExtendedPlayer.getForPlayer(player) : null;

        for (PetEntry petEntry : this.getEntrySnapshot()) {
            if (petEntry.getOwner() != this.host)
                petEntry.setOwner(this.host);

            if (playerExt != null && petEntry.usesSpirit()) {
                int spiritCost = petEntry.getSpiritCost();
                if (petEntry.isSpawningActive() && petEntry.isEntryActive()) {
                    newSpiritReserved += spiritCost;
                    if (!playerExt.canReserveSpirit(newSpiritReserved)) {
                        petEntry.setSpawningActive(false);
                        newSpiritReserved -= spiritCost;
                    }
                }
            }

            if (petEntry.isEntryActive())
                petEntry.onUpdate(world);
            else
                this.markEntryRemoved(petEntry);
        }

        return newSpiritReserved;
    }

    private void removeInactiveEntries() {
        if (!this.hasRemovedEntries()) {
            return;
        }

        for (PetEntry petEntry : this.getRemovedEntries()) {
            this.removeEntry(petEntry);
        }
        this.clearRemovedEntries();
    }

    private void updateSpiritReserved(int newSpiritReserved) {
        if (!(this.host instanceof Player player)) {
            return;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt != null)
            playerExt.setSpiritReserved(newSpiritReserved);
    }


    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========

    /**
     * Reads a list of Pet Entries from a player's NBTTag.
     **/
    public void readFromNBT(CompoundTag nbtTagCompound) {
        if (!nbtTagCompound.contains("PetManager"))
            return;

        ListTag entryList = nbtTagCompound.getList("PetManager", 10);
        for (int i = 0; i < entryList.size(); ++i) {
            CompoundTag nbtEntry = (CompoundTag) entryList.get(i);
            if (!nbtEntry.hasUUID("UUID") && nbtEntry.contains("EntryName")) {
                LMHelperClass.logInfoMessage("[Pets] Converting Pet Entry from older mod version: " + nbtEntry.getString("EntryName") + "...");
                nbtEntry.putUUID("UUID", UUID.randomUUID());
            }
            if (nbtEntry.hasUUID("UUID")) {
                this.addPendingEntryNBT(nbtEntry.getUUID("UUID"), nbtEntry);
            }
            else {
                LMHelperClass.logWarningMessage("[Pets] A Pet Entry was missing a UUID and EntryName, this is either a bug or NBT data has been tampered with, please report this!");
            }
        }
    }

    // ========== Write ==========

    /**
     * Writes a list of Creature Knowledge to a player's NBTTag.
     **/
    public void writeToNBT(CompoundTag nbtTagCompound) {
        ListTag entryList = new ListTag();
        for (PetEntry petEntry : this.getEntries()) {
            CompoundTag nbtEntry = new CompoundTag();
            petEntry.writeToNBT(nbtEntry);
            entryList.add(nbtEntry);
        }
        nbtTagCompound.put("PetManager", entryList);
    }
}
