package com.lycanitesmobs.core.capabilities.level;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import com.lycanitesmobs.core.capabilities.util.BossEntry;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.manager.MobEventManager;
import com.lycanitesmobs.core.network.message.MessageMobEvent;
import com.lycanitesmobs.core.network.message.MessageWorldEvent;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ExtendedWorld extends SavedData {
    protected static final String EXT_PROP_NAME = "LycanitesMobs";
    /** 26.x saved data are codec-driven; wrap the legacy NBT save/load in a CompoundTag codec. */
    public static final net.minecraft.world.level.saveddata.SavedDataType<ExtendedWorld> LYC_SAVED_DATA_TYPE =
            new net.minecraft.world.level.saveddata.SavedDataType<>(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("lycanitesmobs", "world"),
                    ExtendedWorld::new,
                    net.minecraft.nbt.CompoundTag.CODEC.xmap(tag -> {
                        ExtendedWorld loaded = new ExtendedWorld();
                        loaded.load(tag);
                        return loaded;
                    }, worldData -> {
                        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                        worldData.saveLegacy(tag);
                        return tag;
                    }));
    protected static final Map<Level, ExtendedWorld> loadedExtWorlds = new HashMap<>();
    private final MobEventManager mobEventManager = MobEventManager.getInstance();

    /**
     * The world INSTANCE to work with.
     **/
    protected Level world;
    protected boolean initialized = false;
    protected long lastSpawnerTime = 0;
    protected long lastEventScheduleTime = 0;
    protected long lastEventUpdateTime = 0;

    // Mob Events World Config:
    protected boolean useTotalWorldTime = true;

    // Mob Events:
    protected Map<String, MobEventPlayerServer> serverMobEventPlayers = new HashMap<>();
    protected MobEventPlayerServer serverWorldEventPlayer = null;
    long worldEventStartTargetTime = 0;
    long worldEventLastStartedTime = 0;
    String worldEventName = "";
    int worldEventCount = -1;

    // Entities:
    protected Map<UUID, BossEntry> bosses = new HashMap<>();

    // Dungeons:
    protected Map<UUID, DungeonInstance> dungeons = new HashMap<>();


    // ==================================================
    //                   Get for World
    // ==================================================
    public static ExtendedWorld getForWorld(Level world) {
        if (world == null) {
            // LMHelperClass.logWarningMessage("Tried to access an ExtendedWorld from a null World.");
            return null;
        }

        ExtendedWorld worldExt;

        // Already Loaded:
        if (loadedExtWorlds.containsKey(world)) {
            worldExt = loadedExtWorlds.get(world);
            return worldExt;
        }

        // Server Side:
        if (world instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) world;
            world.increaseMaxEntityRadius(25);
            ExtendedWorld worldSavedData = serverWorld.getDataStorage().get(LYC_SAVED_DATA_TYPE);
            if (worldSavedData != null) {
                worldExt = worldSavedData;
                worldExt.world = world;
                worldExt.init();
            } else {
                worldExt = new ExtendedWorld(world);
                serverWorld.getDataStorage().set(LYC_SAVED_DATA_TYPE, worldExt);
            }
        }

        // Client Side: (Only used as a per world object instance for running events, etc.)
        else {
            worldExt = new ExtendedWorld(world);
            worldExt.init();
        }

        loadedExtWorlds.put(world, worldExt);
        return worldExt;
    }

    public static void unloadWorld(LevelAccessor world) {
        if (world instanceof Level level) {
            loadedExtWorlds.remove(level);
        }
    }


    // ==================================================
    //                     Constructor
    // ==================================================
    public ExtendedWorld() {
        super();
    }

    public ExtendedWorld(Level world) {
        super();
        this.world = world;
    }

    public Level getWorld() {
        return this.world;
    }


    // ==================================================
    //                        Init
    // ==================================================
    public void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;

        // Initial Tick Times:
        this.lastSpawnerTime = this.world.getGameTime() - 1;
        this.lastEventScheduleTime = this.world.getGameTime() - 1;
        this.lastEventUpdateTime = this.world.getGameTime() - 1;

        this.restoreSavedWorldEvent();
    }

    private void restoreSavedWorldEvent() {
        if (this.world.isClientSide() || "".equals(this.worldEventName) || this.hasServerWorldEventPlayer()) {
            return;
        }

        long savedLastStartedTime = this.worldEventLastStartedTime;
        this.startMobEvent(this.worldEventName, null, new BlockPos(0, 0, 0), 1, -1); // TODO Swap to read/write from NBT on Server Players.
        MobEventPlayerServer worldEventPlayer = this.getServerWorldEventPlayer();
        if (worldEventPlayer != null) {
            worldEventPlayer.changeStartedWorldTime(savedLastStartedTime);
        }
    }


    // ==================================================
    //                    Get Properties
    // ==================================================
    public long getWorldEventStartTargetTime() {
        return this.worldEventStartTargetTime;
    }

    public long getWorldEventLastStartedTime() {
        return this.worldEventLastStartedTime;
    }

    public String getWorldEventName() {
        return this.worldEventName;
    }

    public MobEvent getWorldEvent() {
        if (this.getWorldEventName() == null || "".equals(this.getWorldEventName())) {
            return null;
        }
        return this.mobEventManager.getMobEvent(this.getWorldEventName());
    }

    public int getWorldEventCount() {
        return this.worldEventCount;
    }

    public long getConfiguredDayBaseTime(Level world) {
        return this.useTotalWorldTime ? world.getGameTime() : LMHelperClass.getDayTime(world);
    }

    public boolean hasServerWorldEventPlayer() {
        return this.serverWorldEventPlayer != null;
    }

    public MobEventPlayerServer getServerWorldEventPlayer() {
        return this.serverWorldEventPlayer;
    }

    void setServerWorldEventPlayer(MobEventPlayerServer mobEventPlayerServer) {
        this.serverWorldEventPlayer = mobEventPlayerServer;
    }

    MobEventPlayerServer putServerMobEventPlayer(String mobEventName, MobEventPlayerServer mobEventPlayerServer) {
        return this.serverMobEventPlayers.put(mobEventName, mobEventPlayerServer);
    }

    MobEventPlayerServer removeServerMobEventPlayer(String mobEventName) {
        return this.serverMobEventPlayers.remove(mobEventName);
    }

    MobEventPlayerServer getActiveServerMobEventPlayer(String mobEventName) {
        return this.serverMobEventPlayers.get(mobEventName);
    }

    Collection<MobEventPlayerServer> getServerMobEventPlayers() {
        return this.serverMobEventPlayers.values();
    }

    public MobEventPlayerServer[] getServerMobEventPlayerSnapshot() {
        return this.serverMobEventPlayers.values().toArray(new MobEventPlayerServer[this.serverMobEventPlayers.size()]);
    }

    public boolean markSpawnerTickIfFresh(long gameTime) {
        if (this.lastSpawnerTime == gameTime) {
            return false;
        }
        this.lastSpawnerTime = gameTime;
        return true;
    }

    public long getLastSpawnerTime() {
        return this.lastSpawnerTime;
    }

    public boolean markEventScheduleTickIfFresh(long gameTime) {
        if (this.lastEventScheduleTime == gameTime) {
            return false;
        }
        this.lastEventScheduleTime = gameTime;
        return true;
    }

    public long getLastEventScheduleTime() {
        return this.lastEventScheduleTime;
    }

    public boolean markEventUpdateTickIfFresh(long gameTime) {
        if (this.lastEventUpdateTime == gameTime) {
            return false;
        }
        this.lastEventUpdateTime = gameTime;
        return true;
    }

    BossEntry getOrCreateBossEntry(UUID uuid) {
        return this.bosses.computeIfAbsent(uuid, ignored -> new BossEntry());
    }

    BossEntry getBossEntry(UUID uuid) {
        return this.bosses.get(uuid);
    }

    void removeBossEntry(UUID uuid) {
        this.bosses.remove(uuid);
    }

    Collection<BossEntry> getBossEntries() {
        return this.bosses.values();
    }

    void putDungeonInstance(DungeonInstance dungeonInstance) {
        this.dungeons.put(dungeonInstance.getUuid(), dungeonInstance);
    }

    boolean hasDungeonInstance(UUID uuid) {
        return this.dungeons.containsKey(uuid);
    }

    Collection<DungeonInstance> getDungeonInstances() {
        return this.dungeons.values();
    }


    // ==================================================
    //                    Set Properties
    // ==================================================
    public void setWorldEventStartTargetTime(long setLong) {
        if (this.worldEventStartTargetTime != setLong)
            this.setDirty();
        this.worldEventStartTargetTime = setLong;
        if (setLong > 0)
            LMHelperClass.logDebug("MobEvents", "Next random mob will start after " + ((this.worldEventStartTargetTime - this.world.getGameTime()) / 20) + "secs.");
    }

    public void setWorldEventLastStartedTime(long setLong) {
        if (this.worldEventLastStartedTime != setLong)
            this.setDirty();
        this.worldEventLastStartedTime = setLong;
    }

    public void setWorldEventName(String setString) {
        if (!this.worldEventName.equals(setString))
            this.setDirty();
        this.worldEventName = setString;
    }

    public void increaseMobEventCount() {
        this.worldEventCount++;
    }


    // ==================================================
    //                Random Event Delay
    // ==================================================

    /**
     * Gets a random time until the next random event will start.
     **/
    public int getRandomEventDelay(RandomSource random) {
        int min = Math.max(200, this.mobEventManager.getMinTicksUntilEvent());
        int max = Math.max(200, this.mobEventManager.getMaxTicksUntilEvent());
        if (max <= min) {
            return min;
        }

        return min + random.nextInt(max - min);
    }


    // ==================================================
    //                     World Event
    // ==================================================

    /**
     * Starts the provided Mob Event on the provided world.
     *
     **/
    public void startWorldEvent(MobEvent mobEvent) {
        if (mobEvent == null) {
            LMHelperClass.logWarningMessage("Tried to start a null world event, stopping any event instead.");
            this.stopWorldEvent();
            return;
        }

        boolean extended = false;
        MobEventPlayerServer worldEventPlayer = this.getServerWorldEventPlayer();
        if (worldEventPlayer != null) {
            extended = worldEventPlayer.getMobEvent() == mobEvent;
        }
        if (!extended) {
            worldEventPlayer = mobEvent.getServerEventPlayer(this.world);
            this.setServerWorldEventPlayer(worldEventPlayer);
        }
        worldEventPlayer.setExtended(extended);

        this.setWorldEventName(mobEvent.getName());
        this.increaseMobEventCount();
        this.setWorldEventStartTargetTime(0);
        this.setWorldEventLastStartedTime(this.world.getGameTime());
        worldEventPlayer.onStart();
        this.updateAllClientsEvents();
    }

    /**
     * Stops the World Event.
     **/
    public void stopWorldEvent() {
        MobEventPlayerServer worldEventPlayer = this.getServerWorldEventPlayer();
        if (worldEventPlayer != null) {
            worldEventPlayer.onFinish();
            this.setWorldEventName("");
            this.setServerWorldEventPlayer(null);
            this.updateAllClientsEvents();
        }
    }


    // ==================================================
    //                     Mob Events
    // ==================================================

    /**
     * Starts a provided Mob Event (provided by INSTANCE) on the provided world.
     *
     **/
    public void startMobEvent(MobEvent mobEvent, Player player, BlockPos pos, int level, int variant) {
        if (mobEvent == null) {
            LMHelperClass.logWarningMessage("Tried to start a null mob event.");
            return;
        }

        MobEventPlayerServer mobEventPlayerServer = mobEvent.getServerEventPlayer(this.world);
        this.putServerMobEventPlayer(mobEvent.getName(), mobEventPlayerServer);
        mobEventPlayerServer.configure(player, pos, level, variant);
        mobEventPlayerServer.onStart();
        this.updateAllClientsEvents();
    }

    /**
     * Starts a provided Mob Event (provided by name) on the provided world.
     **/
    public MobEvent startMobEvent(String mobEventName, Player player, BlockPos pos, int level, int variant) {
        MobEvent mobEvent;
        if (this.mobEventManager.hasMobEvent(mobEventName)) {
            mobEvent = this.mobEventManager.getMobEvent(mobEventName);
            if (!mobEvent.isEnabled()) {
                LMHelperClass.logWarningMessage("Tried to start a mob event that was disabled with the name: '" + mobEventName + "' on " + (this.world.isClientSide() ? "Client" : "Server"));
                return null;
            }
        }
        else {
            LMHelperClass.logWarningMessage("Tried to start a mob event with the invalid name: '" + mobEventName + "' on " + (this.world.isClientSide() ? "Client" : "Server"));
            return null;
        }

        mobEvent.trigger(this.world, player, pos, level, variant);
        return mobEvent;
    }

    /**
     * Stops a Mob Event.
     *
     **/
    public void stopMobEvent(String mobEventName) {
        MobEventPlayerServer mobEventPlayerServer = this.removeServerMobEventPlayer(mobEventName);
        if (mobEventPlayerServer != null) {
            mobEventPlayerServer.onFinish();
            this.updateAllClientsEvents();
        }
    }


    // ==================================================
    //            Get Active Mob Event Players
    // ==================================================

    /**
     * Returns a Mob Event Server Player if an event by the provided event name is currently active, otherwise null.
     **/
    public MobEventPlayerServer getMobEventPlayerServer(String mobEventName) {
        if (mobEventName == null || "".equals(mobEventName)) {
            return null;
        }

        if (mobEventName.equals(this.getWorldEventName())) {
            return this.getServerWorldEventPlayer();
        }

        return this.getActiveServerMobEventPlayer(mobEventName);
    }

    // ==================================================
    //                     Entities
    // ==================================================

    /**
     * Called by bosses to let this world know that they are active, this will add them to the boss list if they are not already in it.
     **/
    public void bossUpdate(Entity entity) {
        if (entity.isAlive()) {
            this.getOrCreateBossEntry(entity.getUUID()).update(entity);
        }
    }

    /**
     * Overrides the boss nearby range for the provided entity.
     **/
    public void overrideBossRange(Entity entity, int rangeOverride) {
        BossEntry bossEntry = this.getBossEntry(entity.getUUID());
        if (bossEntry != null) {
            bossEntry.overrideNearbyRange(rangeOverride);
        }
    }

    /**
     * Called by bosses to let this world know that they are being removed.
     **/
    public void bossRemoved(Entity entity) {
        this.removeBossEntry(entity.getUUID());
    }

    /**
     * Returns true if a boss is nearby.
     *
     * @param pos The position to search around.
     * @return True if a boss is present.
     */
    public boolean isBossNearby(Vec3 pos) {
        for (BossEntry bossEntry : this.getBossEntries()) {
            if (bossEntry != null && bossEntry.isNear(pos)) {
                return true;
            }
        }
        return false;
    }


    // ==================================================
    //                  Update Clients
    // ==================================================

    /**
     * Sends a packet to all clients updating their events for the provided world.
     **/
    public void updateAllClientsEvents() {
        MobEventPlayerServer worldEventPlayer = this.getServerWorldEventPlayer();
        BlockPos pos = worldEventPlayer != null ? worldEventPlayer.getOrigin() : new BlockPos(0, 0, 0);
        int level = worldEventPlayer != null ? worldEventPlayer.getLevel() : 0;
        int subspecies = worldEventPlayer != null ? worldEventPlayer.getVariant() : -1;
        MessageWorldEvent message = new MessageWorldEvent(this.getWorldEventName(), pos, level, subspecies);
        LycanitesMobs.PACKET_MANAGER.sendToWorld(message, this.world);
        for (MobEventPlayerServer mobEventPlayerServer : this.getServerMobEventPlayers()) {
            MessageMobEvent messageMobEvent = new MessageMobEvent(
                    mobEventPlayerServer.getMobEventName(),
                    mobEventPlayerServer.getOrigin(),
                    mobEventPlayerServer.getLevel(),
                    mobEventPlayerServer.getVariant()
            );
            LycanitesMobs.PACKET_MANAGER.sendToWorld(messageMobEvent, this.world);
        }
    }


    // ==================================================
    //                     Dungeons
    // ==================================================

    /**
     * Adds a new Dungeon Instance to this world where it can be found for generation, etc. Gives a new UUID.
     *
     * @param dungeonInstance The Dungeon Instance to add.
     * @param uuid            The UUID to give to the new Dungeon Instance.
     */
    public void addDungeonInstance(DungeonInstance dungeonInstance, UUID uuid) {
        dungeonInstance.setUuid(uuid);
        this.putDungeonInstance(dungeonInstance);
        this.setDirty();
    }


    /**
     * Returns all Dungeon Instances loaded for the provided world within range of the chunk position.
     *
     * @param chunkPos The chunk position to search around.
     * @param range    The range from the chunk position.
     * @return A list of Dungeon Instances found.
     */
    public List<DungeonInstance> getNearbyDungeonInstances(ChunkPos chunkPos, int range) {
        List<DungeonInstance> nearbyDungeons = new ArrayList<>();
        for (DungeonInstance dungeonInstance : this.getDungeonInstances()) {
            if (dungeonInstance.getWorld() == null) {
                dungeonInstance.init(this.world);
            }
            if (dungeonInstance.isChunkPosWithin(chunkPos, range)) {
                nearbyDungeons.add(dungeonInstance);
            }
        }
        return nearbyDungeons;
    }


    // ==================================================
    //                    Read From NBT
    // ==================================================
    public void load(CompoundTag nbtTagCompound) {
        this.loadMobEventState(nbtTagCompound);
        this.loadDungeonState(nbtTagCompound);
    }

    private void loadMobEventState(CompoundTag nbtTagCompound) {
        if (nbtTagCompound.contains("WorldEventStartTargetTime")) {
            this.worldEventStartTargetTime = nbtTagCompound.getIntOr("WorldEventStartTargetTime", 0);
        }
        if (nbtTagCompound.contains("WorldEventLastStartedTime")) {
            this.worldEventLastStartedTime = nbtTagCompound.getIntOr("WorldEventLastStartedTime", 0);
        }
        if (nbtTagCompound.contains("WorldEventName")) {
            this.worldEventName = nbtTagCompound.getStringOr("WorldEventName", "");
        }
        if (nbtTagCompound.contains("WorldEventCount")) {
            this.worldEventCount = nbtTagCompound.getIntOr("WorldEventCount", 0);
        }
        // TODO Load all active mob events, not just the world event.
    }

    private void loadDungeonState(CompoundTag nbtTagCompound) {
        if (!nbtTagCompound.contains("Dungeons")) {
            return;
        }

        ListTag nbtDungeonList = nbtTagCompound.getListOrEmpty("Dungeons");
        for (int i = 0; i < nbtDungeonList.size(); i++) {
            try {
                CompoundTag dungeonNBT = nbtDungeonList.getCompoundOrEmpty(i);
                DungeonInstance dungeonInstance = new DungeonInstance();
                dungeonInstance.readFromNBT(dungeonNBT);
                if (dungeonInstance.getUuid() != null && !this.hasDungeonInstance(dungeonInstance.getUuid())) {
                    this.putDungeonInstance(dungeonInstance);
                }
            }
            catch (Exception e) {
                LMHelperClass.logWarning("Dungeon", "An exception occurred when loading a dungeon from NBT.");
            }
        }
    }


    // ==================================================
    //                    Write To NBT
    // ==================================================
    public CompoundTag saveLegacy(CompoundTag nbtTagCompound) {
        this.saveMobEventState(nbtTagCompound);
        this.saveDungeonState(nbtTagCompound);
        return nbtTagCompound;
    }

    private void saveMobEventState(CompoundTag nbtTagCompound) {
        nbtTagCompound.putLong("WorldEventStartTargetTime", this.worldEventStartTargetTime);
        nbtTagCompound.putLong("WorldEventLastStartedTime", this.worldEventLastStartedTime);
        nbtTagCompound.putString("WorldEventName", this.worldEventName);
        nbtTagCompound.putInt("WorldEventCount", this.worldEventCount);
        // TODO Save all active mob events, not just the world event.
    }

    private void saveDungeonState(CompoundTag nbtTagCompound) {
        ListTag nbtDungeonList = new ListTag();
        for (DungeonInstance dungeonInstance : this.getDungeonInstances()) {
            CompoundTag dungeonNBT = new CompoundTag();
            dungeonNBT = dungeonInstance.writeToNBT(dungeonNBT);
            if (dungeonNBT != null) {
                nbtDungeonList.add(dungeonNBT);
            }
        }
        nbtTagCompound.put("Dungeons", nbtDungeonList);
    }

    public void deserializeNBT(CompoundTag p_deserializeNBT_1_) {
        this.load(p_deserializeNBT_1_);
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbtTagCompound = new CompoundTag();
        this.saveMobEventState(nbtTagCompound);
        this.saveDungeonState(nbtTagCompound);
        return nbtTagCompound;
    }

    public long getWorldSeed() {
        return this.world instanceof ServerLevel lycSrv ? lycSrv.getSeed() : 0L;
    }

    public void buildDungeonInLoadedChunks(DungeonInstance instance) {
        if (!(this.world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!instance.hasChunkBounds()) {
            return;
        }

        LMHelperClass.logDebug(
                "DungeonDebug",
                "buildDungeonInLoadedChunks: instance=" + instance.getUuid()
                        + " chunkMin=" + instance.getChunkMin()
                        + " chunkMax=" + instance.getChunkMax()
        );

        int total = 0;
        for (int cx = instance.getChunkMin().x(); cx <= instance.getChunkMax().x(); cx++) {
            for (int cz = instance.getChunkMin().z(); cz <= instance.getChunkMax().z(); cz++) {
                total++;
            }
        }
        LMHelperClass.logDebug("DungeonDebug", "buildDungeonInLoadedChunks: totalChunksInRect=" + total);

        int processed = 0;
        for (int cx = instance.getChunkMin().x(); cx <= instance.getChunkMax().x(); cx++) {
            for (int cz = instance.getChunkMin().z(); cz <= instance.getChunkMax().z(); cz++) {
                if (!serverLevel.getChunkSource().hasChunk(cx, cz)) {
                    continue;
                }

                ChunkPos chunkPos = new ChunkPos(cx, cz);
                RandomSource random = RandomSource.create(chunkPos.pack() ^ this.getWorldSeed());

                LMHelperClass.logDebug("DungeonDebug", "buildDungeonInLoadedChunks: building " + chunkPos + " (" + processed + "/" + total + ")");
                instance.buildChunk(serverLevel, serverLevel, chunkPos, random);
                processed++;
            }
        }

        LMHelperClass.logDebug("DungeonDebug", "buildDungeonInLoadedChunks: done, processed=" + processed + " of " + total);
    }


    public void debugDryRunDungeonPlacement(DungeonInstance instance) {
        if (!(this.world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!instance.hasChunkBounds()) {
            LMHelperClass.logDebug("DungeonDebug", "debugDryRunDungeonPlacement: no chunk bounds for instance " + instance.getUuid() + " " + instance);
            return;
        }

        LMHelperClass.logDebug(
                "DungeonDebug",
                "debugDryRunDungeonPlacement: instance=" + instance.getUuid() +
                        " chunkMin=[" + instance.getChunkMin().x() + "," + instance.getChunkMin().z() + "]" +
                        " chunkMax=[" + instance.getChunkMax().x() + "," + instance.getChunkMax().z() + "]"
        );

        for (int cx = instance.getChunkMin().x(); cx <= instance.getChunkMax().x(); cx++) {
            for (int cz = instance.getChunkMin().z(); cz <= instance.getChunkMax().z(); cz++) {
                boolean loaded = serverLevel.getChunkSource().hasChunk(cx, cz);
                LMHelperClass.logDebug(
                        "DungeonDebug",
                        "debugDryRunDungeonPlacement: instance=" + instance.getUuid() +
                                " chunk=[" + cx + "," + cz + "] loaded=" + loaded
                );
                if (!loaded) {
                    continue;
                }

                ChunkPos chunkPos = new ChunkPos(cx, cz);
                RandomSource random = RandomSource.create(chunkPos.pack() ^ this.getWorldSeed());
                instance.debugBuildChunk(serverLevel, serverLevel, chunkPos, random);
            }
        }
    }

    public void debugDungeonInLoadedChunks(DungeonInstance instance) {
        if (!(this.world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!instance.hasChunkBounds()) {
            LMHelperClass.logDebug("DungeonDebug", "debugDungeonInLoadedChunks: no chunk bounds for instance " + instance.getUuid() + " " + instance);
            return;
        }

        for (int cx = instance.getChunkMin().x(); cx <= instance.getChunkMax().x(); cx++) {
            for (int cz = instance.getChunkMin().z(); cz <= instance.getChunkMax().z(); cz++) {
                boolean hasChunk = serverLevel.getChunkSource().hasChunk(cx, cz);
                LMHelperClass.logDebug(
                        "DungeonDebug",
                        "debugDungeonInLoadedChunks: instance=" + instance.getUuid() +
                                " chunk=[" + cx + "," + cz + "] loaded=" + hasChunk
                );
                if (!hasChunk) {
                    continue;
                }

                ChunkPos chunkPos = new ChunkPos(cx, cz);
                RandomSource random = RandomSource.create(chunkPos.pack() ^ this.getWorldSeed());
                instance.debugBuildChunk(serverLevel, serverLevel, chunkPos, random);
            }
        }
    }


}
