package com.lycanitesmobs.core.manager;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.creature.*;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.data.config.ConfigCreatures;
import com.lycanitesmobs.core.entity.util.CreatureStats;
import com.lycanitesmobs.core.entity.spawner.SpawnerMobRegistry;
import com.lycanitesmobs.core.item.consumable.entity.CreatureTreatItem;
import com.lycanitesmobs.core.item.consumable.entity.ItemCustomSpawnEgg;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulstone;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulstoneFilled;
import com.lycanitesmobs.core.item.equipment.CreatureSaddleItem;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static com.lycanitesmobs.core.tabs.LMCreaturesGroup.creatureNames;
import com.lycanitesmobs.core.data.config.ConfigStatKeyAliases;

public class CreatureManager extends JSONLoader {
    private static CreatureManager INSTANCE;
    private static final List<String> DIFFICULTY_NAMES = List.of("easy", "normal", "hard");
    private static final Map<String, Double> DIFFICULTY_DEFAULTS = Map.of(
            "easy", 0.8D,
            "normal", 1.0D,
            "hard", 1.1D
    );

    /**
     * Handles all global creature general config settings.
     **/
    protected CreatureConfig config;

    /**
     * Handles all global creature spawning config settings.
     **/
    protected CreatureSpawnConfig spawnConfig;

    /**
     * A map of all creatures types by name.
     **/
    protected Map<String, CreatureType> creatureTypes = new HashMap<>();

    /**
     * A map of all creatures groups by name.
     **/
    protected Map<String, CreatureGroup> creatureGroups = new HashMap<>();

    /**
     * A map of all creatures by name.
     **/
    protected Map<String, CreatureInfo> creatures = new HashMap<>();

    /**
     * A map of all creatures by class.
     **/
    protected Map<Class, CreatureInfo> creatureClassMap = new HashMap<>();

    /**
     * A list of mods that have loaded with this Creature Manager.
     **/
    protected List<ModInfo> loadedMods = new ArrayList<>();

    /**
     * A map containing all the global multipliers for each stat for each difficulty.
     **/
    protected Map<String, Double> difficultyMultipliers = new HashMap<>();

    /**
     * A map containing all the global multipliers for each stat for mob level scaling.
     **/
    protected Map<String, Double> levelMultipliers = new HashMap<>();

    /**
     * Element registry dependency used while validating and linking creature definitions.
     **/
    protected final ElementManager elementManager;

    /**
     * The global multiplier to use for the health of tamed creatures.
     **/
    protected double tamedHealthMultiplier = 3;

    /**
     * Set to true if Doomlike Dungeons is loaded allowing mobs to register their Dungeon themes.
     **/
    protected boolean dlDungeonsLoaded = false;

    /**
     * Constructor
     */
    public CreatureManager() {
        this.config = new CreatureConfig();
        this.spawnConfig = new CreatureSpawnConfig();
        this.elementManager = ElementManager.getInstance();
    }

    /**
     * Returns the main Creature Manager instance or creates it and returns it.
     **/
    public static CreatureManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CreatureManager();
        }
        return INSTANCE;
    }

    public static List<String> getDifficultyNames() {
        return DIFFICULTY_NAMES;
    }

    public static double getDifficultyDefault(String difficultyName, String statName) {
        double defaultValue = DIFFICULTY_DEFAULTS.getOrDefault(difficultyName, 1.0D);
        if ("easy".equalsIgnoreCase(difficultyName) && "speed".equalsIgnoreCase(statName)) {
            return 1.0D;
        }
        if ("hard".equalsIgnoreCase(difficultyName)
                && ("attackSpeed".equalsIgnoreCase(statName) || "rangedSpeed".equalsIgnoreCase(statName))) {
            return 1.5D;
        }
        if ("armor".equalsIgnoreCase(statName) || "sight".equalsIgnoreCase(statName)) {
            return 1.0D;
        }
        return defaultValue;
    }

    public CreatureConfig getConfig() {
        return this.config;
    }

    public CreatureSpawnConfig getSpawnConfig() {
        return this.spawnConfig;
    }

    /**
     * Called during startup and initially loads everything in this manager.
     *
     * @param modInfo The mod loading this manager.
     */
    public void startup(ModInfo modInfo) {
        // Load From JSON:
        this.loadCreatureTypesFromJSON(modInfo);
        this.loadCreatureGroupsFromJSON(modInfo);
        this.loadCreaturesFromJSON(modInfo);

        // Initialise:
        registerItems();
        for (CreatureGroup creatureGroup : this.creatureGroups.values()) {
            link(creatureGroup, this);
        }
        CreatureBootstrapHelper.registerEntityTypeSuppliers(this.creatures.values());
    }

    public void registerItems() {
        for (CreatureType creatureType : this.creatureTypes.values()) {

            Item.Properties standardItemProperties = new Item.Properties();
            Item.Properties smallStackItemProperties = new Item.Properties();
            smallStackItemProperties.stacksTo(16);
            Item.Properties spawnEggProperties = new Item.Properties();

            Lazy<Item> treat = Lazy.of(() -> new CreatureTreatItem(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(smallStackItemProperties, creatureType.getTreatName()), creatureType));
            creatureType.setTreatItem(treat);
            ObjectManager.addItem(creatureType.getTreatName(), treat);

            Lazy<Item> saddle = Lazy.of(() -> new CreatureSaddleItem(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(smallStackItemProperties, creatureType.getSaddleName()), creatureType));
            creatureType.setSaddleItem(saddle);
            ObjectManager.addItem(creatureType.getSaddleName(), saddle);

            Lazy<Item> spawnEgg = Lazy.of(() -> new ItemCustomSpawnEgg(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(spawnEggProperties, creatureType.getSpawnEggName()), creatureType));
            creatureType.setSpawnEggItem(spawnEgg);
            ObjectManager.addItem(creatureType.getSpawnEggName(), spawnEgg);
            creatureNames.add(creatureType.getSpawnEggName());

            Lazy<ItemSoulstone> soulstone = Lazy.of(() -> new ItemSoulstoneFilled(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(standardItemProperties, creatureType.getSoulstoneName()), creatureType));
            creatureType.setSoulstoneItem(soulstone);
            ObjectManager.addItem(creatureType.getSoulstoneName(), soulstone);

            LMHelperClass.logDebug("Creature Type", "Loaded Creature Type: " + creatureType.getName());
        }
    }

    /**
     * Loads all JSON Crieature Types. Should be done before creatures are loaded so that they can find their type on load.
     **/
    public void loadCreatureTypesFromJSON(ModInfo modInfo) {
        this.loadAllJson(modInfo, "Creature Type", "creaturetypes", "name", true, null, FileLoader.common(), StreamLoader.common());
        LMHelperClass.logDebug("Creature", "Complete! " + this.creatureTypes.size() + " JSON Creature Types Loaded In Total.");
    }

    /**
     * Loads all JSON Creature Groups. Should be done before creatures are loaded so that they can find their groups on load.
     **/
    public void loadCreatureGroupsFromJSON(ModInfo modInfo) {
        this.loadAllJson(modInfo, "Creature Group", "creaturegroups", "name", true, null, FileLoader.common(), StreamLoader.common());
        CreatureDefinitionValidationHelper.validateCreatureGroups(this.creatureGroups.values(), this);
        LMHelperClass.logDebug("Creature", "Complete! " + this.creatureGroups.size() + " JSON Creature Groups Loaded In Total.");
    }

    /**
     * Loads all JSON Creatures. Should only initially be done on pre-init and before Creature Info is loaded and can then be done in game on reload.
     **/
    public void loadCreaturesFromJSON(ModInfo modInfo) {
        try {
            if (!this.loadedMods.contains(modInfo)) {
                this.loadedMods.add(modInfo);
            }
            this.loadAllJson(modInfo, "Creature", "creatures", "name", true, null, FileLoader.common(), StreamLoader.common());
            LMHelperClass.logDebug("Creature", "Complete! " + this.creatures.size() + " JSON Creatures Loaded In Total.");
        } catch (Exception e) {
            LMHelperClass.logError("Error loading Creatures for: " + modInfo.name);
            throw (e);
        }
    }

    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        // Parse Creature Type JSON:
        if ("Creature Type".equals(loadGroup)) {
            CreatureDefinitionRegistryHelper.loadCreatureType(this, modInfo, json);
            return;
        }

        // Parse Creature Group JSON:
        if ("Creature Group".equals(loadGroup)) {
            CreatureDefinitionRegistryHelper.loadCreatureGroup(this, json);
            return;
        }

        // Parse Creature JSON:
        if ("Creature".equals(loadGroup)) {
            CreatureDefinitionRegistryHelper.loadCreature(this, this.elementManager, modInfo, json);
        }
    }

    /**
     * Called during early start up, loads all global configs into this manager.
     **/
    public void loadConfig() {
        this.config.loadConfig();
        this.spawnConfig.loadConfig();

        // Difficulty:
        this.difficultyMultipliers = new HashMap<>();
        for (String difficultyName : getDifficultyNames()) {
            for (String statName : CreatureStats.STAT_NAMES) {
                this.difficultyMultipliers.put((difficultyName + "-" + statName).toUpperCase(Locale.ENGLISH),
                        ConfigStatKeyAliases.resolveCanonicalOrAlias(
                                ConfigCreatures.INSTANCE.difficultyMultipliers.get(difficultyName).get(statName),
                                ConfigCreatures.INSTANCE.getLegacyRangedSpeedDifficultyMultiplier(difficultyName, statName)));
            }
        }

        // Level:
        for (String statName : CreatureStats.STAT_NAMES) {
            this.levelMultipliers.put(statName.toUpperCase(Locale.ENGLISH),
                    ConfigStatKeyAliases.resolveCanonicalOrAlias(
                            ConfigCreatures.INSTANCE.levelMultipliers.get(statName),
                            ConfigCreatures.INSTANCE.getLegacyRangedSpeedLevelMultiplier(statName)));
        }
    }


    /**
     * Reloads all Creature JSON.
     */
    public void reload() {
        this.loadConfig();
        SpawnerMobRegistry.clearRegistries();
        for (ModInfo group : this.loadedMods) {
            this.loadCreaturesFromJSON(group);
        }
    }

    /**
     * Binds Forge-registered entity types and runs creature startup initialization after registries are available.
     */
    public void bindRegisteredValues() {
        CreatureBootstrapHelper.bindRegisteredValues(this.creatures.values(), this.spawnConfig);
    }

    /**
     * Returns loaded creature types for read-only iteration.
     *
     * @return The loaded creature types.
     */
    public Collection<CreatureType> getCreatureTypes() {
        return Collections.unmodifiableCollection(this.creatureTypes.values());
    }

    /**
     * Returns loaded creatures for read-only iteration.
     *
     * @return The loaded creatures.
     */
    public Collection<CreatureInfo> getCreatures() {
        return Collections.unmodifiableCollection(this.creatures.values());
    }

    /**
     * Returns mods that have registered creature definitions with this manager.
     *
     * @return The loaded creature definition mods.
     */
    public List<ModInfo> getLoadedMods() {
        return Collections.unmodifiableList(this.loadedMods);
    }

    /**
     * Returns configured difficulty stat multipliers for read-only inspection.
     *
     * @return The difficulty multiplier index.
     */
    public Map<String, Double> getDifficultyMultipliers() {
        return Collections.unmodifiableMap(this.difficultyMultipliers);
    }

    /**
     * Returns configured level stat multipliers for read-only inspection.
     *
     * @return The level multiplier index.
     */
    public Map<String, Double> getLevelMultipliers() {
        return Collections.unmodifiableMap(this.levelMultipliers);
    }

    public double getTamedHealthMultiplier() {
        return this.tamedHealthMultiplier;
    }

    public boolean isDLDungeonsLoaded() {
        return this.dlDungeonsLoaded;
    }

    /**
     * Returns how many creature definitions are loaded.
     *
     * @return The loaded creature count.
     */
    public int getCreatureCount() {
        return this.creatures.size();
    }

    /**
     * Gets a creature type by name.
     *
     * @param creatureTypeName The name of the creature type to get.
     * @return The Creature Type.
     */
    public CreatureType getCreatureType(String creatureTypeName) {
        if (!this.creatureTypes.containsKey(creatureTypeName))
            return null;
        return this.creatureTypes.get(creatureTypeName);
    }

    /**
     * Gets a creature group by name.
     *
     * @param creatureGroupName The name of the creature group to get.
     * @return The Creature Group.
     */
    public CreatureGroup getCreatureGroup(String creatureGroupName) {
        if (!this.creatureGroups.containsKey(creatureGroupName))
            return null;
        return this.creatureGroups.get(creatureGroupName);
    }

    /**
     * Gets a creature by name.
     *
     * @param creatureName The name of the creature to get.
     * @return The Creature Info or null.
     */
    @Nullable
    public CreatureInfo getCreature(String creatureName) {
        if (!this.creatures.containsKey(creatureName))
            return null;
        return this.creatures.get(creatureName);
    }

    /**
     * Gets a creature by class.
     *
     * @param creatureClass The class of the creature to get.
     * @return The Creature Info.
     */
    public CreatureInfo getCreature(Class creatureClass) {
        if (!this.creatureClassMap.containsKey(creatureClass))
            return null;
        return this.creatureClassMap.get(creatureClass);
    }

    /**
     * Gets a Creature Entity Type by name.
     *
     * @param creatureName The name of the creature to get.
     * @return The Entity Type or null.
     */
    @Nullable
    public EntityType<? extends LivingEntity> getEntityType(String creatureName) {
        CreatureInfo creatureInfo = this.getCreature(creatureName);
        if (creatureInfo == null)
            return null;
        return creatureInfo.getEntityType();
    }

    /**
     * Gets a creature by entity id.
     *
     * @param entityId The the entity id of the creature to get. Periods will be replaced with semicolons.
     * @return The Creature Info.
     */
    public CreatureInfo getCreatureFromId(String entityId) {
        entityId = entityId.replace(".", ":");
        String[] mobIdParts = entityId.toLowerCase().split(":");
        return this.getCreature(mobIdParts[mobIdParts.length - 1]);
    }

    /**
     * Returns a global difficulty multiplier for a stat.
     *
     * @param difficultyName The difficulty name.
     * @param statName       The stat name.
     * @return The multiplier.
     */
    public double getDifficultyMultiplier(String difficultyName, String statName) {
        String key = difficultyName.toUpperCase(Locale.ENGLISH) + "-" + statName.toUpperCase(Locale.ENGLISH);
        if (!this.difficultyMultipliers.containsKey(key)) {
            return 1;
        }
        return this.difficultyMultipliers.get(key);
    }

    /**
     * Returns a global level multiplier for a stat.
     *
     * @param statName The stat name.
     * @return The multiplier.
     */
    public double getLevelMultiplier(String statName) {
        if (!this.levelMultipliers.containsKey(statName.toUpperCase(Locale.ENGLISH))) {
            return 1;
        }
        return this.levelMultipliers.get(statName.toUpperCase(Locale.ENGLISH));
    }

    public static void link(CreatureGroup creatureGroup, CreatureManager creatureManager) {
        creatureGroup.clearLinkedInteractionGroups();

        linkGroups(creatureGroup, creatureManager, creatureGroup.getHuntGroupNames(), creatureGroup::addHuntGroup);
        linkGroups(creatureGroup, creatureManager, creatureGroup.getPackGroupNames(), creatureGroup::addPackGroup);
        linkGroups(creatureGroup, creatureManager, creatureGroup.getWaryGroupNames(), creatureGroup::addWaryGroup);
        linkGroups(creatureGroup, creatureManager, creatureGroup.getFleeGroupNames(), creatureGroup::addFleeGroup);
        linkGroups(creatureGroup, creatureManager, creatureGroup.getIgnoreGroupNames(), creatureGroup::addIgnoreGroup);

        LMHelperClass.logDebug("Creature Group", "Loaded Creature Group: " + creatureGroup.getName());
    }

    private static void linkGroups(CreatureGroup owner, CreatureManager creatureManager, Iterable<String> groupNames, Consumer<CreatureGroup> targetGroups) {
        for (String groupName : groupNames) {
            CreatureGroup group = creatureManager.getCreatureGroup(groupName);
            if (groupName.equals(owner.getName())) {
                group = owner;
            }
            if (group != null) {
                targetGroups.accept(group);
            }
        }
    }
}
