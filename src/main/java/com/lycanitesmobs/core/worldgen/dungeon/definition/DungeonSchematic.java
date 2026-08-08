package com.lycanitesmobs.core.worldgen.dungeon.definition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.condition.WorldSpawnCondition;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.data.info.item.ItemDrop;
import com.lycanitesmobs.core.entity.spawner.MobSpawn;
import com.lycanitesmobs.core.entity.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.*;

public class DungeonSchematic {
    /**
     * Dungeon Schematics define actual dungeons to spawn using a collection of
     * Sectors, Themes, etc.
     **/

    /**
     * The unique name of this dungeon. Required.
     **/
    protected String name = "";

    /**
     * Whether this Schematic is enabled or not.
     **/
    protected boolean enabled = true;

    /**
     * If true, air spaces and waterloggable blocks below sea level will be filled/waterlogged during placement.
     * Used for underwater dungeons such as the streamshrine.
     **/
    protected boolean waterlogged = false;

    /**
     * The minimum amount of sectors this dungeon should have in total.
     **/
    protected int sectorCountMin = 10;
    protected int minDistanceFromSpawn = 500;

    /**
     * The maximum amount of sectors this dungeon should have in total.
     **/
    protected int sectorCountMax = 20;

    /**
     * The chance of a corridor connecting to another corridor.
     **/
    protected double corridorToCorridorChance = 0.1D;

    /**
     * The chance of a room connecting to another room.
     **/
    protected double roomToRoomChance = 0.1D;

    /**
     * A list of SpawnConditions to use. Optional.
     **/
    protected List<SpawnCondition> conditions = new ArrayList<>();

    /**
     * The list of biome ids that this dungeon spawns in.
     **/
    protected Set<String> biomeIds = new HashSet<>();

    /**
     * A list of themes to use. Required.
     **/
    protected List<String> themes = new ArrayList<>();

    /**
     * A list of entrance sectors to use. Required.
     **/
    protected List<String> entrances = new ArrayList<>();

    /**
     * A list of room sectors to use. Required.
     **/
    protected List<String> rooms = new ArrayList<>();

    /**
     * A list of corridor sectors to use. Required.
     **/
    protected List<String> corridors = new ArrayList<>();

    /**
     * A list of stairs sectors to use. Required.
     **/
    protected List<String> stairs = new ArrayList<>();

    /**
     * A list of tower sectors to use. Required.
     **/
    protected List<String> towers = new ArrayList<>();

    /**
     * A list of stairs sectors to use. Required.
     **/
    protected List<String> bossRooms = new ArrayList<>();

    /**
     * A list of finish sectors to use. Required.
     **/
    protected List<String> finishes = new ArrayList<>();

    /**
     * A list of MobSpawns to use. Optional.
     **/
    protected List<MobSpawn> mobSpawns = new ArrayList<>();

    /**
     * The loot tables to random use for each level in addition to the specific loot
     * added to this dungeon. If blank, only the specific loot is used. Ex:
     * "minecraft:chests/simple_dungeon". Default: Empty.
     **/
    protected Map<Integer, List<String>> lootTables = new HashMap<>();

    /**
     * A list of item drops to add to loot chests.
     **/
    protected List<ItemDrop> loot = new ArrayList<>();

    protected List<String> biomeTags = new ArrayList<>();

    public String getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isWaterlogged() {
        return this.waterlogged;
    }

    public List<SpawnCondition> getConditions() {
        return Collections.unmodifiableList(this.conditions);
    }

    public Set<String> getBiomeIds() {
        return Collections.unmodifiableSet(this.biomeIds);
    }

    public List<String> getThemes() {
        return Collections.unmodifiableList(this.themes);
    }

    public List<String> getEntrances() {
        return Collections.unmodifiableList(this.entrances);
    }

    public List<String> getRooms() {
        return Collections.unmodifiableList(this.rooms);
    }

    public List<String> getCorridors() {
        return Collections.unmodifiableList(this.corridors);
    }

    public List<String> getStairs() {
        return Collections.unmodifiableList(this.stairs);
    }

    public List<MobSpawn> getMobSpawns() {
        return Collections.unmodifiableList(this.mobSpawns);
    }

    public List<ItemDrop> getLoot() {
        return Collections.unmodifiableList(this.loot);
    }

    public List<String> getBiomeTags() {
        return Collections.unmodifiableList(this.biomeTags);
    }

    /**
     * Loads this Dungeon Theme from the provided JSON data.
     **/
    public void loadFromJSON(JsonObject json) {
        this.name = json.get("name").getAsString().toLowerCase();

        if (json.has("minDistanceFromSpawn"))
            this.minDistanceFromSpawn = Math.max(0, json.get("minDistanceFromSpawn").getAsInt());
        if (json.has("enabled"))
            this.enabled = json.get("enabled").getAsBoolean();

        if (json.has("waterlogged"))
            this.waterlogged = json.get("waterlogged").getAsBoolean();

        if (json.has("sectorCountMin"))
            this.sectorCountMin = json.get("sectorCountMin").getAsInt();

        if (json.has("sectorCountMax"))
            this.sectorCountMax = json.get("sectorCountMax").getAsInt();

        if (json.has("corridorToCorridorChance"))
            this.corridorToCorridorChance = json.get("corridorToCorridorChance").getAsDouble();

        if (json.has("roomToRoomChance"))
            this.roomToRoomChance = json.get("roomToRoomChance").getAsDouble();

        if (json.has("lootTables")) {
            JsonArray jsonArray = json.get("lootTables").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject lootTableJson = jsonIterator.next().getAsJsonObject();
                int level = -1;
                if (lootTableJson.has("level")) {
                    level = lootTableJson.get("level").getAsInt();
                }
                if (!this.lootTables.containsKey(level)) {
                    this.lootTables.put(level, new ArrayList<>());
                }
                this.lootTables.get(level).add(lootTableJson.get("id").getAsString());
            }
        }

        // Conditions:
        if (json.has("conditions")) {
            JsonArray jsonArray = json.get("conditions").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
                SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
                if (spawnCondition != null)
                    this.conditions.add(spawnCondition);
            }
        }

        // Biomes:
        if (json.has("biomes")) {
            this.biomeIds.clear();
            this.biomeTags = JSONHelper.getJsonStrings(json.get("biomes").getAsJsonArray());
        }

        // Themes:
        if (json.has("themes")) {
            for (JsonElement jsonElement : json.get("themes").getAsJsonArray()) {
                String jsonString = jsonElement.getAsString().toLowerCase();
                if (!this.themes.contains(jsonString))
                    this.themes.add(jsonString);
            }
        }

        // Entrances:
        if (json.has("entrances")) {
            for (JsonElement jsonElement : json.get("entrances").getAsJsonArray()) {
                String jsonString = jsonElement.getAsString().toLowerCase();
                if (!this.entrances.contains(jsonString)) {
                    this.entrances.add(jsonString);
                }
            }
        }

        // Rooms:
        if (json.has("rooms")) {
            for (JsonElement jsonElement : json.get("rooms").getAsJsonArray()) {
                String jsonString = jsonElement.getAsString().toLowerCase();
                if (!this.rooms.contains(jsonString))
                    this.rooms.add(jsonString);
            }
        }

        // Corridors:
        if (json.has("corridors")) {
            for (JsonElement jsonElement : json.get("corridors").getAsJsonArray()) {
                String jsonString = jsonElement.getAsString().toLowerCase();
                if (!this.corridors.contains(jsonString))
                    this.corridors.add(jsonString);
            }
        }

        // Stairs:
        if (json.has("stairs")) {
            for (JsonElement jsonElement : json.get("stairs").getAsJsonArray()) {
                String jsonString = jsonElement.getAsString().toLowerCase();
                if (!this.stairs.contains(jsonString))
                    this.stairs.add(jsonString);
            }
        }

        // Towers:
        if (json.has("towers")) {
            for (JsonElement jsonElement : json.get("towers").getAsJsonArray()) {
                String jsonString = jsonElement.getAsString().toLowerCase();
                if (!this.towers.contains(jsonString))
                    this.towers.add(jsonString);
            }
        }

        // Boss Rooms:
        if (json.has("bossRooms")) {
            for (JsonElement jsonElement : json.get("bossRooms").getAsJsonArray()) {
                String jsonString = jsonElement.getAsString().toLowerCase();
                if (!this.bossRooms.contains(jsonString))
                    this.bossRooms.add(jsonString);
            }
        } else {
            this.bossRooms.addAll(this.rooms);
        }

        // Finishes:
        if (json.has("finishes")) {
            for (JsonElement jsonElement : json.get("finishes").getAsJsonArray()) {
                String jsonString = jsonElement.getAsString().toLowerCase();
                if (!this.finishes.contains(jsonString)) {
                    this.finishes.add(jsonString);
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

        // Loot:
        if (json.has("loot")) {
            JsonArray lootEntries = json.getAsJsonArray("loot");
            for (JsonElement mobDropJson : lootEntries) {
                ItemDrop itemDrop = ItemDrop.createFromJSON(mobDropJson.getAsJsonObject());
                if (itemDrop != null) {
                    this.loot.add(itemDrop);
                }
            }
        }
    }

    public void resolveBiomeIds(Level world) {
        if (!this.biomeIds.isEmpty()) {
            return;
        }
        if (this.biomeTags == null || this.biomeTags.isEmpty()) {
            return;
        }

        this.biomeIds = new HashSet<>(JSONHelper.getBiomesFromTags(world, this.biomeTags));

        LMHelperClass.logInfo(
                "Dungeon",
                "[BiomeDebug] Schematic " + this.name +
                        " resolved biomeTags=" + this.biomeTags +
                        " to biomeIds=" + this.biomeIds);
    }

    /**
     * Returns if this Dungeon Schematic is allowed to be used for the given world
     * and position.
     *
     * @param world The world to use the Schematic in.
     * @param pos   The position to build a Dungeon from with this Schematic.
     * @return
     */
    public boolean canBuild(Level world, BlockPos pos) {
        return this.canBuild(world, pos, 0);
    }

    public boolean canBuild(Level world, BlockPos pos, int horizontalFootprintRadius) {
        if (!this.isFootprintFarEnoughFromSpawn(world, pos, horizontalFootprintRadius)) {
            return false;
        }
        if (!this.enabled) {
            return false;
        }

        for (SpawnCondition condition : this.conditions) {
            if (!condition.isMet(world, null, pos)) {
                LMHelperClass.logDebug("Dungeon",
                        "Schematic " + this.name + " spawn condition failed at " + pos + " condition=" + condition);
                return false;
            }
        }

        this.resolveBiomeIds(world);

        Biome biome = world.getBiome(pos).value();
        if (!this.isValidBiome(world, biome)) {
            LMHelperClass.logDebug("Dungeon",
                    "Schematic " + this.name + " wrong biome at " + pos + " biome=" + biome.toString());
            return false;
        }

        return true;
    }

    /**
     * Returns if any of the provided biomes are valid for this dungeon to spawn in.
     *
     * @param biome The biome to check.
     * @return True if at least one biome in the provided list is a valid biome.
     */
    public boolean isValidBiome(Level world, Biome biome) {
        if (this.biomeIds.isEmpty()) {
            return true;
        }

        Identifier biomeId = world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).getKey(biome);
        if (biomeId == null) {
            LMHelperClass.logDebug("Dungeon", "Schematic " + this.name + " has biome restriction but biome id is null");
            return false;
        }

        boolean result = this.biomeIds.contains(biomeId.toString());
        if (!result) {
            LMHelperClass.logDebug("Dungeon", "Schematic " + this.name + " biome mismatch: " + biomeId);
        }

        return result;
    }

    /**
     * Returns a random number of sectors to generate.
     *
     * @param random The random instance to use.
     * @return A random number of sectors to generate.
     */
    public int getRandomSectorCount(RandomSource random) {
        if (this.sectorCountMax <= this.sectorCountMin) {
            return this.sectorCountMin;
        }
        return this.sectorCountMin + random.nextInt(this.sectorCountMax - this.sectorCountMin + 1);
    }

    /**
     * Returns a random sector of the provided type.
     *
     * @param type   The type of sector to get.
     * @param random The random instance to use.
     * @return A random Dungeon Sector.
     */
    public DungeonSector getRandomSector(String type, RandomSource random) {
        // Get Sector List:
        List<String> sectorList;
        if ("entrance".equalsIgnoreCase(type))
            sectorList = this.entrances;
        else if ("corridor".equalsIgnoreCase(type))
            sectorList = this.corridors;
        else if ("stairs".equalsIgnoreCase(type))
            sectorList = this.stairs;
        else if ("tower".equalsIgnoreCase(type))
            sectorList = this.towers;
        else if ("bossRoom".equalsIgnoreCase(type))
            sectorList = this.bossRooms;
        else if ("finish".equalsIgnoreCase(type))
            sectorList = this.finishes;
        else
            sectorList = this.rooms;

        // Get Sectors From List:
        List<DungeonSector> sectors = new ArrayList<>();
        int totalWeights = 0;
        for (String sectorName : sectorList) {
            DungeonSector sector = DungeonManager.getInstance().getSector(sectorName);
            if (sector == null) {
                continue;
            }
            if (sector.getWeight() > 0) {
                sectors.add(sector);
                totalWeights += sector.getWeight();
            }
        }
        if (sectors.isEmpty()) {
            LMHelperClass.logWarning("Dungeon",
                    "Unable to find any " + type + " sectors for the dungeon: " + this.name);
            return null;
        }
        if (sectors.size() == 1) {
            return sectors.get(0);
        }

        // Get Weighted Sector:
        int randomWeight = random.nextInt(totalWeights) + 1;
        int searchedWeight = 0;
        for (DungeonSector sector : sectors) {
            if (randomWeight <= sector.getWeight() + searchedWeight) {
                return sector;
            }
            searchedWeight += sector.getWeight();
        }
        return sectors.get(sectors.size() - 1);
    }

    /**
     * Returns the next type of sector that should connect to the parent sector
     * type.
     *
     * @param random The instance of Random to use.
     * @return The type of sector to use next.
     */
    public String getNextConnectingSector(String parentType, RandomSource random) {
        if ("room".equalsIgnoreCase(parentType)) {
            return random.nextDouble() <= this.roomToRoomChance ? "room" : "corridor";
        }
        if ("corridor".equalsIgnoreCase(parentType)) {
            return random.nextDouble() <= this.corridorToCorridorChance ? "corridor" : "room";
        }
        if ("entrance".equalsIgnoreCase(parentType)) {
            return random.nextBoolean() ? "corridor" : "room";
        }
        return "room";
    }

    /**
     * Returns a random Dungeon Theme to generate.
     *
     * @param random The random instance to use.
     * @return A random Dungeon Theme to generate.
     */
    public DungeonTheme getRandomTheme(RandomSource random) {
        List<DungeonTheme> themes = new ArrayList<>();
        for (String themeName : this.themes) {
            DungeonTheme dungeonTheme = DungeonManager.getInstance().getTheme(themeName);
            if (dungeonTheme != null) {
                themes.add(dungeonTheme);
            }
        }

        if (themes.isEmpty()) {
            LMHelperClass.logWarning("Dungeon", "No Dungeon Themes Found For " + this.name);
            return null;
        }

        if (themes.size() == 1) {
            return themes.get(0);
        }

        return themes.get(random.nextInt(themes.size()));
    }

    /**
     * Gets a weighted random mob to spawn.
     *
     * @param level  The dungeon level to spawn at.
     * @param boss   False for standard mobs, true for bosses.
     * @param random The instance of random to use.
     * @return The MobSpawn of the mob to spawn or null if no mob can be spawned.
     **/
    public MobSpawn getRandomMobSpawn(int level, boolean boss, RandomSource random) {
        int levelMin = level;
        int levelMax = level;
        if (level < 0) {
            levelMax = -level * 2;
            levelMin = levelMax - 1;
        }

        // Get Weights:
        int totalWeights = 0;
        List<MobSpawn> mobSpawns = new ArrayList<>();
        for (MobSpawn mobSpawn : this.mobSpawns) {
            if (mobSpawn.isDungeonBossEntry() != boss) {
                continue;
            }
            if (mobSpawn.isAboveDungeonLevel(levelMax)) {
                continue;
            }
            if (mobSpawn.isBelowDungeonLevel(levelMin)) {
                continue;
            }
            if (mobSpawn.getWeight() > 0) {
                mobSpawns.add(mobSpawn);
                totalWeights += mobSpawn.getWeight();
            }
        }
        if (totalWeights <= 0) {
            return null;
        }

        // Pick Random Spawn Using Weights:
        int randomWeight = 1;
        if (totalWeights > 1) {
            randomWeight = random.nextInt(totalWeights);
        }
        int searchWeight = 0;
        MobSpawn chosenMobSpawn = null;
        for (MobSpawn mobSpawn : mobSpawns) {
            chosenMobSpawn = mobSpawn;
            if (mobSpawn.getWeight() + searchWeight > randomWeight) {
                break;
            }
            searchWeight += mobSpawn.getWeight();
        }
        if (chosenMobSpawn != null) {
            chosenMobSpawn.resolveEntityType();
        }
        return chosenMobSpawn;
    }

    /**
     * Returns a random loot table to apply to a chest.
     *
     * @param level  The dungeon level of the chest to add the loot to.
     * @param random The instance of random to use.
     * @return A loot table.
     */
    public Identifier getRandomLootTable(int level, RandomSource random) {
        if (level < 0) {
            level = -level * 2;
        }

        List<String> possibleLootTables = new ArrayList<>();

        if (this.lootTables.containsKey(-1)) {
            possibleLootTables.addAll(this.lootTables.get(-1));
        }
        for (int i = 0; i <= level; i++) {
            if (this.lootTables.containsKey(i)) {
                possibleLootTables.addAll(this.lootTables.get(i));
            }
        }

        if (possibleLootTables.isEmpty()) {
            return null;
        }
        if (possibleLootTables.size() == 1) {
            return Identifier.parse(possibleLootTables.get(0));
        }

        return Identifier.parse(possibleLootTables.get(random.nextInt(possibleLootTables.size())));
    }

    /**
     * Returns a list of random item stacks to put into a loot chest.
     *
     * @param random The instance of random to use.
     * @return A list of item stacks.
     */
    public List<ItemStack> getRandomLoot(RandomSource random) {
        List<ItemStack> loot = new ArrayList<>();
        for (ItemDrop itemDrop : this.loot) {
            if (itemDrop.getChance() <= 0) {
                continue;
            }

            boolean addLoot = itemDrop.getChance() >= 1;
            if (!addLoot) {
                addLoot = random.nextDouble() <= itemDrop.getChance();
            }

            if (addLoot) {
                int quantity = itemDrop.getQuantity(random, 0, 1);
                if (quantity > 0) {
                    loot.add(new ItemStack(itemDrop.getItemStack().getItem(), quantity));
                }
            }
        }
        return loot;
    }

    public WorldSpawnCondition getWorldSpawnCondition() {
        if (this.conditions == null || this.conditions.isEmpty()) {
            return null;
        }
        for (SpawnCondition condition : this.conditions) {
            if (condition instanceof WorldSpawnCondition) {
                return (WorldSpawnCondition) condition;
            }
        }
        return null;
    }


    public int getMinDistanceFromSpawn() {
        return this.minDistanceFromSpawn;
    }

    public BlockPos getWorldSpawnPosition(Level world) {
        return world.getRespawnData().globalPos().pos();
    }

    /**
     * Distance is measured from the dungeon FOOTPRINT, not its origin: a large dungeon can have
     * an origin outside the radius while its sectors still reach back over world spawn.
     */
    public boolean isFootprintFarEnoughFromSpawn(Level world, BlockPos pos, int horizontalFootprintRadius) {
        if (this.minDistanceFromSpawn <= 0) {
            return true;
        }
        BlockPos spawnPos = this.getWorldSpawnPosition(world);
        double xDistance = this.getAxisDistanceFromSpawnToFootprint(spawnPos.getX(), pos.getX(), horizontalFootprintRadius);
        double zDistance = this.getAxisDistanceFromSpawnToFootprint(spawnPos.getZ(), pos.getZ(), horizontalFootprintRadius);
        double minDistance = this.minDistanceFromSpawn;
        return xDistance * xDistance + zDistance * zDistance >= minDistance * minDistance;
    }

    public double getHorizontalDistanceFromSpawn(Level world, BlockPos pos) {
        BlockPos spawnPos = this.getWorldSpawnPosition(world);
        double xDistance = pos.getX() - spawnPos.getX();
        double zDistance = pos.getZ() - spawnPos.getZ();
        return Math.sqrt(xDistance * xDistance + zDistance * zDistance);
    }

    private double getAxisDistanceFromSpawnToFootprint(int spawnCoordinate, int originCoordinate, int horizontalFootprintRadius) {
        int radius = Math.max(0, horizontalFootprintRadius);
        int min = originCoordinate - radius;
        int max = originCoordinate + radius;
        if (spawnCoordinate < min) {
            return min - spawnCoordinate;
        }
        if (spawnCoordinate > max) {
            return spawnCoordinate - max;
        }
        return 0.0D;
    }
}
