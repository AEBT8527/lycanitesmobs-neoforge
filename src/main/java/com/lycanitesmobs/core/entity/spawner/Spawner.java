package com.lycanitesmobs.core.entity.spawner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.spawner.trigger.ChunkSpawnTrigger;
import com.lycanitesmobs.core.event.SpawnerEventListener;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.SpawnerManager;
import com.lycanitesmobs.core.entity.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.entity.spawner.location.SpawnLocation;
import com.lycanitesmobs.core.entity.spawner.trigger.SpawnTrigger;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;
import java.util.*;

public class Spawner {
    /** Spawners are loaded from JSON and operate around their Triggers, Conditions and Locations. **/

    /**
     * A list of all Spawn Conditions which determine if the Triggers should be active or not.
     **/
    protected List<SpawnCondition> conditions = new ArrayList<>();

    /**
     * A list of all Spawn Triggers which listen to events and ticks and attempt a spawn, if conditions are met.
     **/
    protected List<SpawnTrigger> triggers = new ArrayList<>();

    /**
     * A list of all Spawn Locations which are used to determine where this Spawner can actually spawn the mob, some Triggers require a specific spawn location other provide an area.
     **/
    protected List<SpawnLocation> locations = new ArrayList<>();

    /**
     * A list of all Mobs that can be spawned by this spawner.
     **/
    protected List<MobSpawn> mobSpawns = new ArrayList<>();

    /**
     * The name of this spawner, must be unique, used by creatures/groups when entering spawners.
     **/
    protected String name;

    /**
     * The name of this spawner when, used by creatures/groups when entering spawners. By default this copies the name but can be changed and doesn't have to be unique.
     **/
    protected String sharedName;

    /**
     * If set to true and this spawner is a default spawner, it will reset when loading. This must be set to false or removed from the json if it is a customised spawner.
     **/
    protected boolean loadDefault = true;

    /**
     * Can be set to false to completely disable this Spawner.
     **/
    protected boolean enabled = true;

    /**
     * Determines how many Conditions must be met. If 0 or less all are required.
     **/
    protected int conditionsRequired = 0;

    /**
     * Sets how many times any Trigger must activate (per player) before a wave is spawned. Only supported by player based triggers
     **/
    protected int triggersRequired = 1;

    /**
     * A list of messages to send to the player whenever the associated count is reached.
     **/
    protected Map<Integer, String> triggerCountMessages = new HashMap<>();

    /**
     * Determines how a Location is chosen if there are multiple. Can be: order, random or combine.
     **/
    protected String multipleLocations = "combine";

    /**
     * The minimum amount of mobs to spawn each wave.
     **/
    protected int mobCountMin = 1;

    /**
     * The maximum amount of mobs to spawn each wave.
     **/
    protected int mobCountMax = 1;

    /**
     * If true, this Spawner will divide the amount of mobs to spawn by the amount of players near the trigger player (does not affect non-player triggers).
     **/
    protected boolean mobCountPlayerScaling = true;

    /**
     * How far to search around a player for finding nearby other player for mob spawn scaling.
     **/
    protected int mobCountPlayerRange = 64;

    /**
     * If true, this Spawner will ignore all mob dimension checks (not Spawn Condition checks), this bypasses the checks in MobSpawns and SpawnInfos.
     **/
    protected boolean ignoreDimensions = false;

    /**
     * If true, this Spawner will ignore all biome checks, this bypasses the checks in MobSpawns and SpawnInfos.
     **/
    protected boolean ignoreBiomes = false;

    /**
     * If true, this Spawner will ignore collision checks, this may cause mobs to become stuck and suffocate.
     **/
    protected boolean ignoreCollision = false;

    /**
     * If true, this Spawner will ignore light level checks, this bypasses the checks in MobSpawns and SpawnInfos.
     **/
    protected boolean ignoreLightLevel = false;

    /**
     * If true, this Spawner will ignore group limit checks, this bypasses the checks in MobSpawns and SpawnInfos.
     **/
    protected boolean ignoreGroupLimit = false;

    /**
     * The range of how far this Spawner should check for surrounding mobs of the same type.
     **/
    protected double groupLimitRange = 32;

    /**
     * If set to true, the Forge Can Spawn Event is fired but its result is ignored, use this to prevent other mods from stopping the spawn via the event.
     **/
    protected boolean ignoreForgeCanSpawnEvent = false;

    /**
     * If true, mobs spawned by this Spawner will not naturally despawn.
     **/
    protected boolean forceNoDespawn = false;

    /**
     * If true, actions caused by this spawner such as block destruction can trigger additional spawn triggers creating a spawner chain.
     **/
    protected boolean chainSpawning = true;

    /**
     * If true, this Spawner will act as enabled even if it can't find any mobs to spawn, useful if you just want to use trigger messages, etc.
     **/
    protected boolean enableWithoutMobs = false;

    /**
     * If set, this is the name of the mob event that must be active, otherwise this Spawner is always active.
     **/
    protected String eventName = "";

    /**
     * If set, when a mob is spawned, blocks in the radius around the spawned mob will be destroyed.
     **/
    protected int blockBreakRadius = -1;

    /**
     * If true, spawned mobs will not drop any items until a player has damaged them in some way.
     **/
    protected boolean dropsRequirePlayerDamage = false;

    protected Map<Player, Integer> triggerCounts = new WeakHashMap<>();

    public List<SpawnCondition> getConditions() {
        return Collections.unmodifiableList(this.conditions);
    }

    public List<SpawnTrigger> getTriggers() {
        return Collections.unmodifiableList(this.triggers);
    }

    public List<SpawnLocation> getLocations() {
        return Collections.unmodifiableList(this.locations);
    }

    public List<MobSpawn> getMobSpawns() {
        return Collections.unmodifiableList(this.mobSpawns);
    }

    public String getName() {
        return this.name;
    }

    public String getSharedName() {
        return this.sharedName;
    }

    public boolean isLoadDefault() {
        return this.loadDefault;
    }

    public boolean isDefinitionEnabled() {
        return this.enabled;
    }

    public int getConditionsRequired() {
        return this.conditionsRequired;
    }

    public int getTriggersRequired() {
        return this.triggersRequired;
    }

    public boolean hasTriggerCountMessage(int count) {
        return this.triggerCountMessages.containsKey(count);
    }

    public String getTriggerCountMessage(int count) {
        return this.triggerCountMessages.get(count);
    }

    public String getMultipleLocations() {
        return this.multipleLocations;
    }

    public int getMobCountMin() {
        return this.mobCountMin;
    }

    public int getMobCountMax() {
        return this.mobCountMax;
    }

    public boolean usesMobCountPlayerScaling() {
        return this.mobCountPlayerScaling;
    }

    public int getMobCountPlayerRange() {
        return this.mobCountPlayerRange;
    }

    public boolean ignoresDimensions() {
        return this.ignoreDimensions;
    }

    public boolean ignoresBiomes() {
        return this.ignoreBiomes;
    }

    public boolean ignoresCollision() {
        return this.ignoreCollision;
    }

    public boolean ignoresLightLevel() {
        return this.ignoreLightLevel;
    }

    public boolean ignoresGroupLimit() {
        return this.ignoreGroupLimit;
    }

    public double getGroupLimitRange() {
        return this.groupLimitRange;
    }

    public boolean ignoresForgeCanSpawnEvent() {
        return this.ignoreForgeCanSpawnEvent;
    }

    public boolean forcesNoDespawn() {
        return this.forceNoDespawn;
    }

    public boolean allowsChainSpawning() {
        return this.chainSpawning;
    }

    public boolean enablesWithoutMobs() {
        return this.enableWithoutMobs;
    }

    public String getEventName() {
        return this.eventName;
    }

    public boolean hasEventName() {
        return !"".equals(this.eventName);
    }

    public int getBlockBreakRadius() {
        return this.blockBreakRadius;
    }

    public boolean dropsRequirePlayerDamage() {
        return this.dropsRequirePlayerDamage;
    }

    public boolean hasLocationType(Class<? extends SpawnLocation> locationType) {
        for (SpawnLocation location : this.locations) {
            if (locationType.isInstance(location)) {
                return true;
            }
        }
        return false;
    }

    public void disableForStructureSpawnInjection() {
        this.enabled = false;
    }


    /**
     * Loads this Spawner from the provided JSON data.
     **/
    public void loadFromJSON(JsonObject json) {
        this.loadFromJSON(json, "config/lycanitesmobs/spawners/" + json.get("name").getAsString() + ".json");
    }

    public void loadFromJSON(JsonObject json, String definitionPath) {
        // Spawner Properties:
        this.name = json.get("name").getAsString();
        this.sharedName = this.name;

        if (json.has("sharedName"))
            this.sharedName = json.get("sharedName").getAsString();

        if (json.has("loadDefault"))
            this.loadDefault = json.get("loadDefault").getAsBoolean();

        if (json.has("enabled"))
            this.enabled = json.get("enabled").getAsBoolean();

        if (json.has("conditionsRequired"))
            this.conditionsRequired = json.get("conditionsRequired").getAsInt();

        if (json.has("triggersRequired"))
            this.triggersRequired = json.get("triggersRequired").getAsInt();

        if (json.has("triggerCountMessages")) {
            JsonArray jsonArray = json.get("triggerCountMessages").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject tcmJson = jsonIterator.next().getAsJsonObject();
                this.triggerCountMessages.put(tcmJson.get("count").getAsInt(), tcmJson.get("message").getAsString());
            }
        }

        if (json.has("multipleLocations"))
            this.multipleLocations = json.get("multipleLocations").getAsString();

        if (json.has("mobCountMin"))
            this.mobCountMin = json.get("mobCountMin").getAsInt();

        if (json.has("mobCountMax"))
            this.mobCountMax = json.get("mobCountMax").getAsInt();

        if (json.has("mobCountPlayerScaling"))
            this.mobCountPlayerScaling = json.get("mobCountPlayerScaling").getAsBoolean();

        if (json.has("mobCountPlayerRange"))
            this.mobCountPlayerRange = json.get("mobCountPlayerRange").getAsInt();

        if (json.has("ignoreDimensions"))
            this.ignoreDimensions = json.get("ignoreDimensions").getAsBoolean();

        if (json.has("ignoreBiomes"))
            this.ignoreBiomes = json.get("ignoreBiomes").getAsBoolean();

        if (json.has("ignoreCollision"))
            this.ignoreCollision = json.get("ignoreCollision").getAsBoolean();

        if (json.has("ignoreLightLevel"))
            this.ignoreLightLevel = json.get("ignoreLightLevel").getAsBoolean();

        if (json.has("ignoreGroupLimit"))
            this.ignoreGroupLimit = json.get("ignoreGroupLimit").getAsBoolean();

        if (json.has("groupLimitRange"))
            this.groupLimitRange = json.get("groupLimitRange").getAsDouble();

        if (json.has("ignoreForgeCanSpawnEvent"))
            this.ignoreForgeCanSpawnEvent = json.get("ignoreForgeCanSpawnEvent").getAsBoolean();

        if (json.has("forceNoDespawn"))
            this.forceNoDespawn = json.get("forceNoDespawn").getAsBoolean();

        if (json.has("chainSpawning"))
            this.chainSpawning = json.get("chainSpawning").getAsBoolean();

        if (json.has("enableWithoutMobs"))
            this.enableWithoutMobs = json.get("enableWithoutMobs").getAsBoolean();

        if (json.has("eventName"))
            this.eventName = json.get("eventName").getAsString();

        if (json.has("blockBreakRadius"))
            this.blockBreakRadius = json.get("blockBreakRadius").getAsInt();

        if (json.has("dropsRequirePlayerDamage"))
            this.dropsRequirePlayerDamage = json.get("dropsRequirePlayerDamage").getAsBoolean();

        // Conditions:
        if (json.has("conditions")) {
            JsonArray jsonArray = json.get("conditions").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
                SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
                if (spawnCondition != null) this.conditions.add(spawnCondition);
            }
        }

        // Locations:
        if (json.has("locations")) {
            JsonArray jsonArray = json.get("locations").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject locationJson = jsonIterator.next().getAsJsonObject();
                SpawnLocation spawnLocation = SpawnLocation.createFromJSON(locationJson);
                if (spawnLocation == null) {
                    String locationType = "<missing>";
                    if (locationJson.has("type") && !locationJson.get("type").isJsonNull()) {
                        locationType = locationJson.get("type").isJsonPrimitive()
                                ? "'" + locationJson.get("type").getAsString() + "'"
                                : locationJson.get("type").toString();
                    }
                    this.enabled = false;
                    LMHelperClass.logWarningMessage("[Spawner] Disabled spawner '" + this.name
                            + "' because it has an unsupported location type " + locationType
                            + ". Fix or delete this JSON file: " + definitionPath);
                    continue;
                }
                this.locations.add(spawnLocation);
            }
        }

        // Triggers:
        if (json.has("triggers")) {
            JsonArray jsonArray = json.get("triggers").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject triggerJson = jsonIterator.next().getAsJsonObject();
                SpawnTrigger spawnTrigger = SpawnTrigger.createFromJSON(triggerJson, this);
                this.triggers.add(spawnTrigger);
                if (this.enabled) {
                    SpawnerTriggerDispatcher.getInstance().addTrigger(spawnTrigger);
                }
            }
        }

        // Mob Spawns:
        if (json.has("mobSpawns")) {
            JsonArray jsonArray = json.get("mobSpawns").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject mobSpawnJson = jsonIterator.next().getAsJsonObject();
                MobSpawn mobSpawn = MobSpawn.createFromJSON(mobSpawnJson);
                if (mobSpawn != null) {
                    this.mobSpawns.add(mobSpawn);
                }
            }
        }
    }


    /**
     * Remove this Spawner. This removes all Triggers from the Event Listener and does other cleanup.
     **/
    public void destroy() {
        for (SpawnTrigger spawnTrigger : this.triggers) {
            SpawnerTriggerDispatcher.getInstance().removeTrigger(spawnTrigger);
        }
        SpawnerManager.getInstance().removeSpawner(this);
    }


    /**
     * Returns true if this Spawner is considered enabled, this is checked first before major logging and more in depth checks are done in canSpawn().
     **/
    public boolean isEnabled(Level world, Player player) {
        if (!this.enabled) {
            return false;
        }
        if (CreatureManager.getInstance().getSpawnConfig().disableAllSpawning()) {
            return false;
        }
        if (!LMHelperClass.getGameRuleBool(world, GameRules.SPAWN_MOBS, true)) {
            return false;
        }

        if (this.hasEventName()) {
            ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
            if (worldExt == null || worldExt.getMobEventPlayerServer(this.eventName) == null) {
                return false;
            }
        }

        return this.enableWithoutMobs || !this.mobSpawns.isEmpty() || SpawnerMobRegistry.getMobSpawns(this.sharedName) != null;
    }


    /**
     * Returns true if Triggers are allowed to operate for this Spawner.
     **/
    public boolean canSpawn(Level world, Player player, BlockPos triggerPos) {
        if (!SpawnerManager.getInstance().getGlobalSpawnConditions().isEmpty()) {
            for (SpawnCondition condition : SpawnerManager.getInstance().getGlobalSpawnConditions()) {
                if (!condition.isMet(world, player, triggerPos)) {
                    return false;
                }
            }
        }

        if (this.conditions.isEmpty()) {
            return true;
        }

        int conditionsMet = 0;
        int requiredConditions = this.conditionsRequired > 0 ? this.conditionsRequired : this.conditions.size();
        for (SpawnCondition condition : this.conditions) {
            if (condition.isMet(world, player, triggerPos) && ++conditionsMet >= requiredConditions) {
                return true;
            }
        }

        return false;
    }


    /**
     * Starts a new spawn, usually called by Triggers.
     *
     * @param world        The World to spawn in.
     * @param player       The player that is being spawned around. If null all player based checks and features are ignored.
     * @param spawnTrigger The spawn trigger that has triggered this spawn.
     * @param triggerPos   The location that the spawn was triggered, usually used as the center for spawning around or on.
     * @param level        The level of the spawn trigger, higher levels are from rarer spawn conditions and can result in tougher mobs being spawned.
     * @param countAmount  How much this trigger affects the trigger count by.
     * @param chain        How far along a spawner chain this trigger is, this increases whenever the actions of one spawner trigger another spawner and is used to prevent loops, etc.
     * @return True on a successful spawn and false on failure.
     **/
    public boolean trigger(Level world, Player player, SpawnTrigger spawnTrigger, BlockPos triggerPos, int level, int countAmount, int chain) {
        if (!this.isEnabled(world, player)) {
            LMHelperClass.logDebug("JSONSpawner", this.name + ": trigger blocked, spawner not enabled.");
            return false;
        }
        if (!this.canSpawn(world, player, triggerPos)) {
            LMHelperClass.logDebug("JSONSpawner", this.name + ": trigger blocked, conditions not met.");
            return false;
        }
        if (!this.shouldExecuteSpawn(player, countAmount)) {
            LMHelperClass.logDebug("JSONSpawner", this.name + ": trigger blocked, trigger count not reached.");
            return false;
        }
        return this.doSpawn(world, player, spawnTrigger, triggerPos, level, chain);
    }

    private boolean shouldExecuteSpawn(Player player, int countAmount) {
        if (this.triggersRequired <= 1 || player == null) {
            return true;
        }

        if (!this.triggerCounts.containsKey(player)) {
            this.triggerCounts.put(player, Math.max(countAmount, 0));
        }

        int currentCount = this.triggerCounts.get(player);
        int lastCount = currentCount;

        if (countAmount == 0) {
            currentCount = 0;
        }
        else {
            currentCount += countAmount;
        }

        if (currentCount != lastCount && this.hasTriggerCountMessage(currentCount)) {
            MutableComponent message = Component.translatable(this.getTriggerCountMessage(currentCount));
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
            if (extendedPlayer != null) {
                extendedPlayer.sendOverlayMessage(message);
            }
        }

        if (currentCount >= this.triggersRequired) {
            this.triggerCounts.put(player, 0);
            return true;
        }

        this.triggerCounts.put(player, currentCount);
        return false;
    }

    public void clearTriggerCount(Player player) {
        this.triggerCounts.remove(player);
    }

    // TODO: Replace this with something better
    /**
     * Starts a new spawn, usually called from trigger() when the count is sufficient.
     *
     * @param world        The World to spawn in.
     * @param player       The player that is being spawned around.
     * @param spawnTrigger The spawn trigger that has triggered this spawn.
     * @param triggerPos   The location that the spawn was triggered, usually used as the center for spawning around or on.
     * @param level        The level of the spawn trigger, higher levels are from rarer spawn conditions and can result in tougher mobs being spawned.
     * @param chain        How far along a spawner chain this trigger is, this increases whenever the actions of one spawner trigger another spawner and is used to prevent loops, etc.
     * @return True on a successful spawn and false on failure.
     **/
    private boolean doSpawn(Level world, Player player, SpawnTrigger spawnTrigger, BlockPos triggerPos, int level, int chain) {
        int mobCount = this.getMobCount(world, player);
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);

        List<BlockPos> spawnPositions = this.getSpawnPos(world, player, triggerPos);
        if (spawnTrigger instanceof ChunkSpawnTrigger) {
            spawnPositions.removeIf(spawnPos ->
                    spawnPos.getX() < triggerPos.getX() - 8 || spawnPos.getX() > triggerPos.getX() + 8 ||
                            spawnPos.getZ() < triggerPos.getZ() - 8 || spawnPos.getZ() > triggerPos.getZ() + 8
            );
        }
        if (spawnPositions.isEmpty()) {
            LMHelperClass.logDebug("JSONSpawner", this.name + ": no valid spawn positions found around " + triggerPos + ".");
            return false;
        }
        LMHelperClass.logDebug("JSONSpawner", this.name + ": found " + spawnPositions.size() + " spawn positions, mob count target " + mobCount + ".");
        Collections.shuffle(spawnPositions);

        List<Biome> biomes = null;
        Map<BlockPos, Biome> positionBiomeCache = new HashMap<>();
        if (!this.ignoreBiomes) {
            Set<Biome> biomeSet = new LinkedHashSet<>();
            for (BlockPos spawnPos : spawnPositions) {
                Biome biome = world.getBiomeManager().getBiome(spawnPos).value();
                positionBiomeCache.put(spawnPos, biome);
                biomeSet.add(biome);
            }
            biomes = new ArrayList<>(biomeSet);
        }

        Map<Biome, List<MobSpawn>> availableMobSpawns = this.getMobSpawns(world, player, spawnPositions.size(), biomes);
        if (availableMobSpawns.isEmpty()) {
            LMHelperClass.logDebug("JSONSpawner", this.name + ": no available mob spawns for these positions/biomes.");
            return false;
        }

        int mobsSpawned = 0;
        Map<String, Integer> spawnGroupCounts = new HashMap<>();
        for (BlockPos spawnPos : spawnPositions) {
            if (mobsSpawned >= mobCount) {
                break;
            }

            Biome spawnBiome = positionBiomeCache.getOrDefault(spawnPos, world.getBiomeManager().getBiome(spawnPos).value());
            MobSpawn mobSpawn = this.chooseSpawnForBiome(world, availableMobSpawns, spawnBiome);
            if (mobSpawn == null) {
                LMHelperClass.logDebug("JSONSpawner", this.name + ": no mob spawn chosen for biome at " + spawnPos + ".");
                continue;
            }
            if (this.exceedsBatchGroupLimit(mobSpawn, spawnGroupCounts)) {
                continue;
            }

            LivingEntity entityLiving = mobSpawn.createEntity(world);
            if (entityLiving == null) {
                LMHelperClass.logDebug("JSONSpawner", this.name + ": createEntity returned null for " + mobSpawn + ".");
                continue;
            }

            entityLiving.snapTo((double) spawnPos.getX() + 0.5D, (double) spawnPos.getY(), (double) spawnPos.getZ() + 0.5D, world.getRandom().nextFloat() * 360.0F, 0.0F);

            MobSpawnEvent.SpawnPlacementCheck.Result canSpawn = this.checkForgeSpawnPlacement(world, entityLiving, spawnPos);
            if (canSpawn == MobSpawnEvent.SpawnPlacementCheck.Result.FAIL && !this.ignoreForgeCanSpawnEvent && !mobSpawn.ignoresForgeCanSpawnEvent()) {
                LMHelperClass.logDebug("JSONSpawner", this.name + ": placement check FAIL at " + spawnPos + ".");
                continue;
            }

            if (!this.mobInstanceSpawnCheck(entityLiving, mobSpawn, world, player, spawnPos, level, canSpawn == MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED)) {
                LMHelperClass.logDebug("JSONSpawner", this.name + ": mob instance spawn check failed at " + spawnPos + " for " + mobSpawn + ".");
                continue;
            }

            this.spawnEntity(world, worldExt, entityLiving, spawnPos, level, mobSpawn, player, chain, canSpawn == MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
            mobsSpawned++;

            if (mobSpawn.hasCreatureInfo()) {
                spawnGroupCounts.merge(mobSpawn.getCreatureInfo().getName(), 1, Integer::sum);
            }
        }

        return mobsSpawned > 0;
    }

    private int getMobCount(Level world, Player player) {
        int mobCount = this.mobCountMin;
        if (this.mobCountMax > this.mobCountMin) {
            mobCount = world.getRandom().nextInt(this.mobCountMax - this.mobCountMin) + this.mobCountMin;
        }
        if (player != null && this.mobCountPlayerScaling) {
            int nearbyPlayers = 0;
            for (Player targetPlayer : world.players()) {
                if ((targetPlayer.isCreative() || targetPlayer.isSpectator()) && !SpawnerEventListener.shouldTestCreative()) {
                    continue;
                }
                if (targetPlayer.distanceTo(player) > this.mobCountPlayerRange) {
                    continue;
                }
                nearbyPlayers++;
            }
            if (nearbyPlayers > 0) {
                mobCount = (int) Math.ceil((double) mobCount / nearbyPlayers);
            }
        }
        return mobCount;
    }

    private MobSpawn chooseSpawnForBiome(Level world, Map<Biome, List<MobSpawn>> mobSpawns, Biome spawnBiome) {
        if (mobSpawns.containsKey(null)) {
            return this.chooseMobToSpawn(world, mobSpawns.get(null));
        }
        if (mobSpawns.containsKey(spawnBiome)) {
            return this.chooseMobToSpawn(world, mobSpawns.get(spawnBiome));
        }
        return null;
    }

    private boolean exceedsBatchGroupLimit(MobSpawn mobSpawn, Map<String, Integer> spawnGroupCounts) {
        if (!mobSpawn.hasCreatureInfo()) {
            return false;
        }
        String creatureName = mobSpawn.getCreatureInfo().getName();
        int groupMax = mobSpawn.getCreatureInfo().getCreatureSpawn().getSpawnGroupMax();
        return groupMax > 0 && spawnGroupCounts.getOrDefault(creatureName, 0) >= groupMax;
    }

    private MobSpawnEvent.SpawnPlacementCheck.Result checkForgeSpawnPlacement(Level world, LivingEntity entityLiving, BlockPos spawnPos) {
        if (!(entityLiving instanceof Mob)) {
            return MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED;
        }
        BlockPos blockPos = new BlockPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        return new MobSpawnEvent.SpawnPlacementCheck(entityLiving.getType(), (ServerLevelAccessor) world, EntitySpawnReason.NATURAL, blockPos, world.getRandom(), false).getResult();
    }


    /**
     * Gets a list of BlockPos to spawn at. Returns an empty list if all Spawn Locations fail to get a spawn position. If no Spawn Locations are set, the triggerPos is used.
     *
     * @param world      The World to spawn in.
     * @param player     The player that is being spawned around.
     * @param triggerPos The location that the spawn was triggered, usually used as the center for spawning around or on.
     * @return True on a successful spawn and false on failure.
     **/
    private List<BlockPos> getSpawnPos(Level world, Player player, BlockPos triggerPos) {
        List<BlockPos> spawnPositions = new ArrayList<>();

        if (this.locations.isEmpty()) {
            spawnPositions.add(triggerPos);
            return spawnPositions;
        }
        if (this.locations.size() == 1) {
            return this.locations.get(0).getSpawnPositions(world, player, triggerPos);
        }
        if ("order".equals(this.multipleLocations)) {
            for (SpawnLocation location : this.locations) {
                spawnPositions = location.getSpawnPositions(world, player, triggerPos);
                if (!spawnPositions.isEmpty()) {
                    return spawnPositions;
                }
            }
        }

        if ("random".equals(this.multipleLocations)) {
            List<List<BlockPos>> possibleSpawnPosLists = new ArrayList<>();
            for (SpawnLocation location : this.locations) {
                spawnPositions = location.getSpawnPositions(world, player, triggerPos);
                if (!spawnPositions.isEmpty()) {
                    possibleSpawnPosLists.add(spawnPositions);
                }
            }
            if (possibleSpawnPosLists.isEmpty()) {
                return spawnPositions;
            }
            if (possibleSpawnPosLists.size() == 1) {
                return possibleSpawnPosLists.get(0);
            }
            return possibleSpawnPosLists.get(world.getRandom().nextInt(possibleSpawnPosLists.size()));
        }

        if ("combine".equals(this.multipleLocations)) {
            for (SpawnLocation location : this.locations) {
                spawnPositions.addAll(location.getSpawnPositions(world, player, triggerPos));
            }
            return spawnPositions;
        }

        return spawnPositions;
    }

    /**
     * Returns all viable mobs that can be spawned within the spawn conditions. Spawn block costs, chances and biomes are checked here.
     *
     * @param world      The World to spawn in.
     * @param player     The player that is being spawned around. Can be null.
     * @param blockCount The total number of possible spawn positions found.
     * @param biomes     A list of all biomes within the spawning area. If null, biome checks will be ignored, all mob spawn will be added to a null map key.
     * @return A map of biomes and mob spawn lists with each mob spawn available to each biome.
     **/
    private Map<Biome, List<MobSpawn>> getMobSpawns(Level world, @Nullable Player player, int blockCount, @Nullable List<Biome> biomes) {
        Map<Biome, List<MobSpawn>> mobSpawns = new HashMap<>();

        if (biomes == null) {
            mobSpawns.put(null, this.getBiomeSpawns(world, player, blockCount, null));
            return mobSpawns;
        }

        for (Biome biome : biomes) {
            mobSpawns.put(biome, this.getBiomeSpawns(world, player, blockCount, biome));
        }
        return mobSpawns;
    }

    /**
     * Returns all viable mobs that can be spawned within the spawn conditions and specific biome.
     *
     * @param world      The world to spawn in.
     * @param player     The player that is being spawned around. Can be null.
     * @param blockCount The total number of possible spawn positions found.
     * @param biome      The biome to spawn in.
     * @return A list of mob spawns.
     */
    private List<MobSpawn> getBiomeSpawns(Level world, @Nullable Player player, int blockCount, @Nullable Biome biome) {
        List<MobSpawn> possibleSpawns = new ArrayList<>();

        Collection<MobSpawn> globalSpawns = SpawnerMobRegistry.getMobSpawns(this.sharedName);
        if (globalSpawns != null) {
            possibleSpawns.addAll(globalSpawns);
        }

        possibleSpawns.addAll(this.mobSpawns);

        List<MobSpawn> viableMobSpawns = new ArrayList<>();
        for (MobSpawn possibleMobSpawn : possibleSpawns) {
            if (possibleMobSpawn.canSpawn(world, blockCount, biome, this.ignoreDimensions)) {
                viableMobSpawns.add(possibleMobSpawn);
            }
        }
        return viableMobSpawns;
    }

    /**
     * Gets a weighted random mob to spawn.
     *
     * @param world     The World to spawn in.
     * @param mobSpawns A list of MobSpawns to choose from.
     * @return The MobSpawn of the mob to spawn or null if no mob can be spawned.
     **/
    private MobSpawn chooseMobToSpawn(Level world, List<MobSpawn> mobSpawns) {
        int totalWeights = 0;
        for (MobSpawn mobSpawn : mobSpawns) {
            totalWeights += mobSpawn.getWeight();
        }
        if (totalWeights <= 0) {
            return null;
        }

        int randomWeight = totalWeights > 1 ? world.getRandom().nextInt(totalWeights) : 1;
        int searchWeight = 0;
        MobSpawn chosenMobSpawn = null;
        for (MobSpawn mobSpawn : mobSpawns) {
            chosenMobSpawn = mobSpawn;
            if (mobSpawn.getWeight() + searchWeight > randomWeight) {
                break;
            }
            searchWeight += mobSpawn.getWeight();
        }
        return chosenMobSpawn;
    }

    /**
     * Performs a check from the Mob Instance, this mob has not yet been spawned into the world, but has been instantiated and is able to do per mob checks.
     *
     * @param entityLiving The to be spawned mob INSTANCE.
     * @param mobSpawn     The MobSpawn that is controlling the conditions of the spawn.
     * @param world        The World to spawn in.
     * @param player       The Player that triggered the spawn. Can be null.
     * @param spawnPos     The position to spawn at.
     * @param level        The level of the spawn.
     * @param forgeForced  If true, the Forge Can Spawn Event wants to force this mob to spawn.
     * @return True if the check has passed.
     **/
    private boolean mobInstanceSpawnCheck(LivingEntity entityLiving, MobSpawn mobSpawn, Level world, Player player, BlockPos spawnPos, int level, boolean forgeForced) {
        if (entityLiving instanceof BaseCreatureEntity entityCreature) {
            if (!this.ignoreCollision && !entityCreature.checkSpawnCollision(world, spawnPos)) {
                LMHelperClass.logDebug("JSONSpawner", this.name + ": instance check - collision failed.");
                return false;
            }

            if (mobSpawn.ignoresMobInstanceConditions()) {
                return true;
            }

            if (!this.ignoreLightLevel && !mobSpawn.ignoresLightLevel() && !entityCreature.checkSpawnLightLevel(world, spawnPos)) {
                LMHelperClass.logDebug("JSONSpawner", this.name + ": instance check - light level failed.");
                return false;
            }

            boolean checkGroupLimit = !this.ignoreGroupLimit && !mobSpawn.ignoresGroupLimit();
            double effectiveGroupRange = checkGroupLimit ? this.groupLimitRange : 0;
            if (!entityCreature.checkSpawnLimits(world, spawnPos, effectiveGroupRange)) {
                LMHelperClass.logDebug("JSONSpawner", this.name + ": instance check - spawn limits failed (group range " + effectiveGroupRange + ").");
                return false;
            }

            return true;
        }

        if (!mobSpawn.ignoresMobInstanceConditions() && entityLiving instanceof Mob mobEntity) {
            return mobEntity.checkSpawnRules(world, EntitySpawnReason.NATURAL);
        }
        return true;
    }

    /**
     * Spawns an entity into the world. The mob INSTANCE should have already been positioned.
     *
     * @param world        The world to spawn in.
     * @param entityLiving The entity to spawn.
     */
    private void spawnEntity(Level world, ExtendedWorld worldExt, LivingEntity entityLiving, BlockPos spawnPos, int level, MobSpawn mobSpawn, Player player, int chain, boolean callInitialSpawn) {
        entityLiving.setPortalCooldown();

        if (entityLiving instanceof BaseCreatureEntity entityCreature) {
            entityCreature.applySpawnerSpawnState(this.forceNoDespawn, level > 0);
            if (this.blockBreakRadius > -1 && chain == 0) {
                BlockPos creaturePos = entityLiving.blockPosition();
                entityCreature.destroyArea(creaturePos.getX(), creaturePos.getY() - 1, creaturePos.getZ(), 100, true, this.blockBreakRadius, this.chainSpawning ? player : null, chain + 1);
            }
            entityCreature.setDropsRequirePlayerDamage(this.dropsRequirePlayerDamage);

            if (this.hasEventName() && worldExt != null) {
                MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.eventName);
                if (mobEventPlayerServer != null) {
                    int spawnEventCount = -1;
                    if (mobEventPlayerServer.getMobEventName().equals(worldExt.getWorldEventName())) {
                        spawnEventCount = worldExt.getWorldEventCount();
                    }
                    entityCreature.applySpawnEvent(mobEventPlayerServer.getMobEvent().getTitleName(), spawnEventCount);
                }
            }
        }

        Runnable afterSpawn = () -> {
            if (callInitialSpawn && entityLiving instanceof Mob mobEntity
                    && EventHooks.checkSpawnPlacements(entityLiving.getType(), (ServerLevelAccessor) world, EntitySpawnReason.NATURAL, spawnPos, world.getRandom(), false)) {
                mobEntity.finalizeSpawn((ServerLevelAccessor) world, com.lycanitesmobs.core.util.helpers.LMHelperClass.getCurrentDifficultyAt(world, spawnPos), EntitySpawnReason.NATURAL, null);
            }

            mobSpawn.onSpawned(entityLiving, player);
            if (this.hasEventName() && worldExt != null) {
                MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.eventName);
                if (mobEventPlayerServer != null) {
                    MobEvent mobEvent = mobEventPlayerServer.getMobEvent();
                    mobEvent.onSpawn(entityLiving, mobEventPlayerServer.getWorld(), mobEventPlayerServer.getPlayer(), mobEventPlayerServer.getOrigin(), mobEventPlayerServer.getLevel(), mobEventPlayerServer.getTicks(), -1);
                }
            }
        };

        DeferredLevelActionManager.spawnEntity(world, spawnPos, "spawner_spawn:" + entityLiving.getUUID(), entityLiving, afterSpawn);
    }
}
