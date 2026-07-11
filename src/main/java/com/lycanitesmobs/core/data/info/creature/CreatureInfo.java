package com.lycanitesmobs.core.data.info.creature;

import net.minecraft.core.registries.BuiltInRegistries;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.data.info.item.ItemDrop;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.List;

/**
 * Contains various information about a creature from default spawn information to stats, etc.
 **/
public class CreatureInfo {
    /**
     * The creature type this creature belongs to.
     **/
    protected CreatureType creatureType;
    /**
     * A list of groups that this creature is in.
     **/
    protected List<CreatureGroup> groups = new ArrayList<>();
    /**
     * The entity class used by this creature.
     **/
    protected Class<? extends BaseCreatureEntity> entityClass;
    /**
     * The constructor used by this creature to create entity instances.
     **/
    protected Constructor<? extends BaseCreatureEntity> entityConstructor;
    /**
     * The class of the model this creature should use, loaded client side only.
     **/
    protected String modelClassName;
    /**
     * The mod info of the mod this creature belongs to.
     **/
    protected ModInfo modInfo;
    /**
     * The entity type used to store base attributes of this creature.
     **/
    protected EntityType<? extends LivingEntity> entityType;
    /**
     * If false, this mob will be removed from the world if present and wont be allowed by any spawners.
     **/
    protected boolean enabled = true;
    /**
     * If true, this is not a true mob, for example the fear entity. It will also not be register1ed to spawners and will not load assets, etc.
     **/
    protected boolean dummy = false;
    /**
     * The Spawn Information for this creature.
     **/
    protected CreatureSpawn creatureSpawn;
    // Stats:
    protected double width = 0.8D;
    protected double height = 1.8D;
    protected int experience = 5;
    protected double health = 20.0D;
    protected double defense = 0.0D;
    protected double armor = 0.0D;
    protected double speed = 24.0D; // Divided by 100 when applied.
    protected double damage = 2.0D;
    protected double attackSpeed = 1.0D; // Seconds per melee.
    protected double rangedSpeed = 0.5D; // Seconds per ranged.
    protected double effectDuration = 1.0D; // Seconds of effect.
    protected double effectAmplifier = -1.0D; // No effect when less than 0.
    protected double pierce = 1.0D;
    protected double sight = 16.0D;
    protected double knockbackResistance = 0.0D;
    protected int bagSize = 5;
    protected int packSize = 3;


    // Spawn Egg:
    /**
     * The background color of this mob's egg. Required.
     **/
    protected int eggBackColor;

    /**
     * The foreground color of this mob's egg. Required.
     **/
    protected int eggForeColor;


    // Creature Type:
    /**
     * If true, this creature is a boss creature and should use special boss features such as boss health bars and dps taken limits, etc.
     **/
    protected boolean boss = false;

    /**
     * The Subspecies that this creature can use.
     **/
    protected Map<Integer, Subspecies> subspecies = new HashMap<>();

    /**
     * A list of subspecies skins that have been loaded, used to prevent them loading assets multiple times per color variation.
     **/
    protected List<String> loadedSubspeciesSkins = new ArrayList<>();

    /**
     * The Elements of this creature, affects buffs and debuffs amongst other things.
     **/
    protected List<ElementInfo> elements = new ArrayList<>();
    /**
     * Parsed element names. Resolved into elements by CreatureInfoReferenceLinker.
     **/
    protected List<String> elementNames = new ArrayList<>();

    /**
     * The diets that this creature has, this controls what food it can eat for being healed, breeding, etc. Diets will search for a tag group lycanitesmobs:diet_dietname.json for items. Creature Types will also provide what diet they are suited for.
     **/
    protected List<String> diets = new ArrayList<>();


    // Creature Difficulty:
    /**
     * If true, this mob is allowed on Peaceful Difficulty.
     **/
    protected boolean peaceful = false;

    /**
     * If true, this mob can be farmed (bred and lured using food items). The entity must have age AI for this.
     **/
    protected boolean farmable = false;

    /**
     * If true, this mob can be summoned as a minion. The entity must have pet AI for this.
     **/
    protected boolean summonable = false;

    /**
     * If true, this mob can be tamed as a pet. The entity must have pet AI and a treat item set for this.
     **/
    protected boolean tameable = false;

    /**
     * If true, this mob can be used as a mount. The entity must have mount AI for this.
     **/
    protected boolean mountable = false;

    /**
     * If true, this mob can perch on it's owner's shoulder. The entity must have pet AI for this.
     **/
    protected boolean perchable = false;

    /**
     * How many charges this creature normally costs to summon.
     **/
    protected int summonCost = 1;

    /**
     * The Dungeon Level of this mob, for Lycanites Dungeons this affects what floor the mob appears on, but this is also used by other mods such as Doomlike Dungeons to assess difficulty. Default: -1 (All levels).
     **/
    protected int dungeonLevel = -1;

    /**
     * The range (in blocks) that players must be to be considered within range of this entity as a boss, used for block place/break boss protection.
     **/
    protected int bossNearbyRange = 60;


    // Items:
    /**
     * A list of all the item drops available to this creature.
     **/
    protected List<ItemDrop> drops = new ArrayList<>();
    /**
     * A custom scale to apply to the mob's size.
     **/
    protected double sizeScale = 1;


    // Scale:
    /**
     * A custom scale to apply to the mob's hitbox.
     **/
    protected double hitboxScale = 1;
    /**
     * The offset relative to this creatures width and height that riding entities should be offset by.
     **/
    protected Vector3d mountOffset = new Vector3d(0.0D, 1.0D, 0.0D);
    /**
     * The name of this mob. Lowercase, no space, used for language entries and for generating the entity id, etc. Required.
     **/
    protected String name;
    /**
     * The parsed creature type name. Resolved into creatureType by CreatureInfoReferenceLinker.
     **/
    protected String creatureTypeName = "beast";
    /**
     * Parsed creature group names. Resolved into groups by CreatureInfoReferenceLinker.
     **/
    protected List<String> creatureGroupNames = new ArrayList<>();


    // Flags:
    protected int tamingReputation = 500;
    /**
     * A json array containing a list of drops to be loaded during init.
     **/
    protected JsonArray dropsJson;
    /**
     * A list of boolean flags set for this creature.
     **/
    protected Map<String, Boolean> boolFlags = new HashMap<>();
    /**
     * A list of double flags set for this creature.
     **/
    protected Map<String, Double> doubleFlags = new HashMap<>();
    /**
     * A list of string flags set for this creature.
     **/
    protected Map<String, String> stringFlags = new HashMap<>();

    /**
     * Constructor
     *
     * @param modInfo The mod that this creature definition will belong to.
     */
    public CreatureInfo(ModInfo modInfo) {
        this.modInfo = modInfo;
        this.creatureSpawn = new CreatureSpawn();
    }

    /**
     * Loads this creature from a JSON object.
     *
     * @return true if loaded successfully, false if this entry should be skipped.
     **/
    public boolean loadFromJson(JsonObject json) {
        return CreatureInfoJsonParser.parse(this, json);
    }

    public Map<Integer, Subspecies> getSubspecies() {
        return subspecies;
    }

    public void setCreatureType(CreatureType creatureType) {
        this.creatureType = creatureType;
    }

    public void addLinkedGroup(CreatureGroup group) {
        if (!this.groups.contains(group)) {
            this.groups.add(group);
        }
    }

    public void setLinkedElements(Collection<ElementInfo> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    public CreatureSpawn getCreatureSpawn() {
        return this.creatureSpawn;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isDummy() {
        return this.dummy;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public int getExperience() {
        return this.experience;
    }

    public double getHealth() {
        return this.health;
    }

    public double getDefense() {
        return this.defense;
    }

    public double getArmor() {
        return this.armor;
    }

    public double getSpeed() {
        return this.speed;
    }

    public double getMovementSpeed() {
        return this.speed / 100.0D;
    }

    public double getDamage() {
        return this.damage;
    }

    public double getAttackSpeed() {
        return this.attackSpeed;
    }

    public double getRangedSpeed() {
        return this.rangedSpeed;
    }

    public double getEffectDuration() {
        return this.effectDuration;
    }

    public double getEffectAmplifier() {
        return this.effectAmplifier;
    }

    public double getPierce() {
        return this.pierce;
    }

    public double getSight() {
        return this.sight;
    }

    public double getKnockbackResistance() {
        return this.knockbackResistance;
    }

    public int getBagSize() {
        return this.bagSize;
    }

    public int getPackSize() {
        return this.packSize;
    }

    public int getEggBackColor() {
        return this.eggBackColor;
    }

    public int getEggForeColor() {
        return this.eggForeColor;
    }

    public boolean isPeaceful() {
        return this.peaceful;
    }

    public int getSummonCost() {
        return this.summonCost;
    }

    public int getDungeonLevel() {
        return this.dungeonLevel;
    }

    public int getBossNearbyRange() {
        return this.bossNearbyRange;
    }

    public double getSizeScale() {
        return this.sizeScale;
    }

    public double getHitboxScale() {
        return this.hitboxScale;
    }

    public Vector3d getMountOffset() {
        return this.mountOffset;
    }

    /**
     * Clears reloadable parsed state and unlinks previous type/group membership before a fresh JSON load.
     */
    protected void resetForJsonLoad() {
        if (this.creatureType != null) {
            this.creatureType.removeCreature(this);
        }
        for (CreatureGroup group : this.groups) {
            group.removeCreature(this);
        }

        this.creatureType = null;
        this.groups.clear();
        this.creatureTypeName = "beast";
        this.creatureGroupNames = new ArrayList<>();
        this.enabled = true;
        this.dummy = false;
        this.creatureSpawn = new CreatureSpawn();
        this.width = 0.8D;
        this.height = 1.8D;
        this.experience = 5;
        this.health = 20.0D;
        this.defense = 0.0D;
        this.armor = 0.0D;
        this.speed = 24.0D;
        this.damage = 2.0D;
        this.attackSpeed = 1.0D;
        this.rangedSpeed = 0.5D;
        this.effectDuration = 1.0D;
        this.effectAmplifier = -1.0D;
        this.pierce = 1.0D;
        this.sight = 16.0D;
        this.knockbackResistance = 0.0D;
        this.bagSize = 5;
        this.packSize = 3;
        this.boss = false;
        this.subspecies.clear();
        this.loadedSubspeciesSkins.clear();
        this.elements.clear();
        this.elementNames = new ArrayList<>();
        this.diets.clear();
        this.peaceful = false;
        this.farmable = false;
        this.summonable = false;
        this.tameable = false;
        this.mountable = false;
        this.perchable = false;
        this.summonCost = 1;
        this.dungeonLevel = -1;
        this.bossNearbyRange = 60;
        this.drops.clear();
        this.sizeScale = 1;
        this.hitboxScale = 1;
        this.mountOffset = new Vector3d(0.0D, 1.0D, 0.0D);
        this.tamingReputation = 500;
        this.dropsJson = null;
        this.boolFlags.clear();
        this.doubleFlags.clear();
        this.stringFlags.clear();
    }

    /**
     * Returns the name of this creature, this is the unformatted lowercase name. Ex: lurker
     *
     * @return Creature name.
     */
    public String getName() {
        return this.name.toLowerCase();
    }

    /**
     * Returns the registry id of this creature. Ex: lycanitesmobs:lurker
     *
     * @return Creature registry entity id.
     */
    public String getEntityId() {
        return this.modInfo.modid + ":" + this.getName();
    }

    /**
     * Returns the mod that owns this creature definition.
     *
     * @return The owning mod info.
     */
    public ModInfo getModInfo() {
        return this.modInfo;
    }

    /**
     * Returns the groups that this creature is in.
     *
     * @return Creature name.
     */
    public List<CreatureGroup> getGroups() {
        return Collections.unmodifiableList(this.groups);
    }

    /**
     * Returns the linked creature type after reference linking.
     *
     * @return The linked creature type or null if this definition has not linked yet.
     */
    @Nullable
    public CreatureType getCreatureType() {
        return this.creatureType;
    }

    /**
     * Returns if this creature belongs to the provided linked type.
     *
     * @param creatureType The creature type to compare against.
     * @return True if this creature belongs to the provided type.
     */
    public boolean isCreatureType(CreatureType creatureType) {
        return this.creatureType != null && this.creatureType == creatureType;
    }

    /**
     * Returns if this creature belongs to the provided linked group.
     *
     * @param creatureGroup The creature group to check.
     * @return True if this creature belongs to the provided group.
     */
    public boolean isInGroup(CreatureGroup creatureGroup) {
        return this.groups.contains(creatureGroup);
    }

    /**
     * Returns the parsed creature type name before or after reference linking.
     *
     * @return The creature type name.
     */
    public String getCreatureTypeName() {
        return this.creatureTypeName;
    }

    /**
     * Returns parsed creature group names before or after reference linking.
     *
     * @return The creature group names.
     */
    public List<String> getCreatureGroupNames() {
        return Collections.unmodifiableList(this.creatureGroupNames);
    }

    /**
     * Returns parsed element names before or after reference linking.
     *
     * @return The element names.
     */
    public List<String> getElementNames() {
        return Collections.unmodifiableList(this.elementNames);
    }

    public String getModelClassName() {
        return this.modelClassName;
    }

    /**
     * Returns if sounds/assets for the provided subspecies skin have already been loaded.
     *
     * @param subspeciesSkin The subspecies skin name.
     * @return True if the skin has already been loaded.
     */
    public boolean hasLoadedSubspeciesSkin(String subspeciesSkin) {
        return this.loadedSubspeciesSkins.contains(subspeciesSkin);
    }

    /**
     * Marks sounds/assets for the provided subspecies skin as loaded.
     *
     * @param subspeciesSkin The subspecies skin name.
     */
    public void markSubspeciesSkinLoaded(String subspeciesSkin) {
        this.loadedSubspeciesSkins.add(subspeciesSkin);
    }

    /**
     * Returns the parsed subspecies definitions for read-only iteration.
     *
     * @return The available subspecies definitions.
     */
    public Collection<Subspecies> getSubspeciesEntries() {
        return Collections.unmodifiableCollection(this.subspecies.values());
    }

    /**
     * Returns the linked elements for this creature.
     *
     * @return The creature's linked elements.
     */
    public List<ElementInfo> getElements() {
        return Collections.unmodifiableList(this.elements);
    }

    /**
     * Returns the linked elements for a subspecies, falling back to the creature's base elements.
     *
     * @param subspecies The subspecies to check.
     * @return The subspecies or creature elements.
     */
    public List<ElementInfo> getElements(Subspecies subspecies) {
        if (subspecies != null && !subspecies.getElements().isEmpty()) {
            return subspecies.getElements();
        }
        return this.getElements();
    }

    /**
     * Returns the parsed diet names for read-only iteration.
     *
     * @return The creature's diet names.
     */
    public List<String> getDiets() {
        return Collections.unmodifiableList(this.diets);
    }

    /**
     * Returns the parsed item drops for read-only iteration.
     *
     * @return The creature's item drops.
     */
    public List<ItemDrop> getDrops() {
        return Collections.unmodifiableList(this.drops);
    }

    /**
     * Returns the entity type of this creature. Prefers the cached entityType field (set during loadValues)
     * over a fresh registry lookup to avoid returning EntityType.PIG (the registry default) when the
     * entity type hasn't been registered yet or the registry lookup fails.
     *
     * @return Creature's entity type.
     */
    @Nullable
    public EntityType<? extends LivingEntity> getEntityType() {
        return getEntityType(this);
    }

    @Nullable
    public EntityType<? extends LivingEntity> getEntityType(CreatureInfo creatureInfo) {
        if (this.entityType != null) {
            return this.entityType;
        }
        ResourceLocation id = AssetHelper.modResource(this.getName());
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(id);
        if (entityType != null && id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType))) {
            return (EntityType<? extends LivingEntity>) entityType;
        }
        LMHelperClass.logWarningMessage("[Creature] getEntityType() registry lookup failed for: " + id + " (returned " + entityType + "). Entity type may not be registered yet.");
        return null;
    }

    /**
     * Binds the Forge-registered entity type for this creature.
     *
     * @param entityType The registered entity type.
     */
    public void bindEntityType(EntityType<? extends LivingEntity> entityType) {
        this.entityType = entityType;
    }


    /**
     * Returns the resource location for this creature.
     *
     * @return Creature resource location.
     */
    public ResourceLocation getResourceLocation() {
        return AssetHelper.resource(this.modInfo.modid, this.getName());
    }

    /**
     * Returns the entity implementation class parsed for this creature.
     *
     * @return The creature entity class.
     */
    @Nullable
    public Class<? extends BaseCreatureEntity> getEntityClass() {
        return this.entityClass;
    }

    @Nullable
    public Constructor<? extends BaseCreatureEntity> getEntityConstructor() {
        return this.entityConstructor;
    }

    /**
     * Returns if this creature definition can represent the provided entity class.
     *
     * @param entityClass The entity class to check.
     * @return True if this definition's entity class is assignable from the provided class.
     */
    public boolean matchesEntityClass(Class<?> entityClass) {
        return this.entityClass != null && this.entityClass.isAssignableFrom(entityClass);
    }

    /**
     * Returns if this creature's entity class is assignable to the provided superclass or interface.
     *
     * @param parentClass The superclass or interface to check.
     * @return True if this definition's entity class can be used as the provided parent class.
     */
    public boolean isEntityClassAssignableTo(Class<?> parentClass) {
        return this.entityClass != null && parentClass.isAssignableFrom(this.entityClass);
    }


    /**
     * Returns the language key for this creature. Ex: swampmobs.lurker
     *
     * @return Creature language key.
     */
    public String getLocalisationKey() {
        return this.modInfo.modid + "." + this.getName();
    }


    /**
     * Returns a translated title for this creature. Ex: Lurker
     *
     * @return The display name of this creature.
     */
    public Component getTitle() {
        return CreatureInfoPresentationHelper.getTitle(this);
    }


    /**
     * Returns a translated description of this creature.
     *
     * @return The creature description.
     */
    public MutableComponent getDescription() {
        return CreatureInfoPresentationHelper.getDescription(this);
    }


    /**
     * Returns a translated description of this creature.
     *
     * @return The creature description.
     */
    public MutableComponent getHabitatDescription() {
        return CreatureInfoPresentationHelper.getHabitatDescription(this);
    }


    /**
     * Returns a translated description of this creature.
     *
     * @return The creature description.
     */
    public MutableComponent getCombatDescription() {
        return CreatureInfoPresentationHelper.getCombatDescription(this);
    }


    /**
     * Returns a comma separated list of Elements used by this Creature.
     *
     * @return The Elements used by this Creature.
     */
    public MutableComponent getElementNames(Subspecies subspecies) {
        return CreatureInfoPresentationHelper.getElementNames(this, subspecies);
    }


    /**
     * Returns a comma separated list of Diets used by this Creature.
     *
     * @return The Diets used by this Creature.
     */
    public MutableComponent getDietNames() {
        return CreatureInfoPresentationHelper.getDietNames(this);
    }


    /**
     * Returns a comma separated list of Biomes native for this Creature.
     *
     * @return The Biomes native for this Creature.
     */
    public MutableComponent getBiomeNames() {
        return CreatureInfoPresentationHelper.getBiomeNames(this);
    }


    /**
     * Returns a comma separated list of items dropped by this Creature.
     *
     * @return The items dropped by this Creature.
     */
    public MutableComponent getDropNames() {
        return CreatureInfoPresentationHelper.getDropNames(this);
    }


    /**
     * Returns the resource location for the GUI icon of this creature.
     *
     * @return Creature icon resource location.
     */
    public ResourceLocation getIcon() {
        return CreatureInfoPresentationHelper.getIcon(this);
    }


    /**
     * Returns if this creature is farmable.
     *
     * @return True if creature is farmable.
     */
    public boolean isFarmable() {
        return this.farmable && AgeableCreatureEntity.class.isAssignableFrom(this.entityClass);
    }


    /**
     * Returns if this creature is summonable.
     *
     * @return True if creature is summonable.
     */
    public boolean isSummonable() {
        return this.summonable && TameableCreatureEntity.class.isAssignableFrom(this.entityClass);
    }


    /**
     * Returns if this creature is tameable.
     *
     * @return True if creature is tameable.
     */
    public boolean isTameable() {
        return this.tameable && TameableCreatureEntity.class.isAssignableFrom(this.entityClass);
    }


    /**
     * Returns if this creature is mountable.
     *
     * @return True if creature is mountable.
     */
    public boolean isMountable() {
        return this.mountable && RideableCreatureEntity.class.isAssignableFrom(this.entityClass);
    }


    /**
     * Returns if this creature is perchable.
     *
     * @return True if creature is perchable.
     */
    public boolean isPerchable() {
        return this.perchable;
    }


    /**
     * Returns if this creature is a boss.
     *
     * @return True if creature is a boss.
     */
    public boolean isBoss() {
        return this.boss;
    }


    /**
     * Returns a subspecies for the provided index or the first subspecies if invalid.
     *
     * @param index The index of the subspecies for this creature.
     * @return Creature subspecies.
     */
    public Subspecies getSubspecies(int index) {
        if (!this.subspecies.containsKey(index)) {
            return this.subspecies.get(0);
        }
        return this.subspecies.get(index);
    }


    /**
     * Gets a random subspecies, normally used by a new mob when spawned.
     *
     * @param entity The entity that has this subspecies.
     * @return The Subspecies to use.
     */
    public Subspecies getRandomSubspecies(LivingEntity entity) {
        LMHelperClass.logDebug("Subspecies", "~0===== Subspecies =====0~");
        LMHelperClass.logDebug("Subspecies", "Selecting subspecies for: " + entity);
        LMHelperClass.logDebug("Subspecies", "Subspecies Available: " + this.subspecies.size());

        List<Subspecies> possibleSubspecies = new ArrayList<>();
        int highestPriority = 0;
        for (Subspecies subspeciesEntry : this.subspecies.values()) {
            if (subspeciesEntry.canSpawn(entity)) {
                possibleSubspecies.add(subspeciesEntry);
                if (subspeciesEntry.getPriority() > highestPriority) {
                    highestPriority = subspeciesEntry.getPriority();
                }
            }
        }
        if (possibleSubspecies.isEmpty()) {
            LMHelperClass.logWarningMessage("[Subspecies] No subspecies allowed for " + this.getName() + ", there should always be a default subspecies, returning the first subspecies found for now.");
            return this.subspecies.get(0);
        }

        if (highestPriority > 0) {
            for (Subspecies subspeciesEntry : possibleSubspecies.toArray(new Subspecies[possibleSubspecies.size()])) {
                if (subspeciesEntry.getPriority() < highestPriority) {
                    possibleSubspecies.remove(subspeciesEntry);
                }
            }
        }
        LMHelperClass.logDebug("Subspecies", "Subspecies Allowed: " + possibleSubspecies.size() + " Highest Priority: " + highestPriority);

        if (possibleSubspecies.size() == 1) {
            return possibleSubspecies.get(0);
        }

        int randomIndex = entity.getRandom().nextInt(possibleSubspecies.size());
        return possibleSubspecies.get(randomIndex);
    }

    /**
     * Returns the amount of Relationship Reputation required to become friendly.
     *
     * @return The reputation required to be friendly.
     */
    public int getFriendlyReputation() {
        return Math.round((float) this.tamingReputation / 2);
    }

    /**
     * Returns the amount of Relationship Reputation required to trigger a tame.
     *
     * @return The reputation required to tame.
     */
    public int getTamingReputation() {
        return this.tamingReputation;
    }

    /**
     * Returns if this creature can eat the provided item as food for healing, breeding, etc.
     *
     * @param itemStack The item to eat.
     * @return True if the item can be eaten.
     */
    public boolean canEat(ItemStack itemStack) {
        if (this.diets.isEmpty()) {
            return false;
        }
        for (String diet : this.diets) {
            ResourceLocation dietTagId = AssetHelper.modResource("diet_" + diet);
            TagKey<Item> dietTag = TagKey.create(Registries.ITEM, dietTagId);
            if (dietTag == null) {
                LMHelperClass.logWarningMessage("[Creature] Cannot find diet: " + dietTagId);
                return false;
            }
            if (itemStack.is(dietTag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new Entity instance from this creature info. Returns null on failure.
     *
     * @param world The world to create the entity in.
     * @return The created entity.
     */
    public LivingEntity createEntity(Level world) {
        EntityType<? extends LivingEntity> type = this.getEntityType();
        if (type == null) {
            return null;
        }
        if (this.entityConstructor == null) {
            LMHelperClass.logWarningMessage("[Creature] Failed to create entity for " + this.getName() + ": missing entity constructor.");
            return null;
        }
        try {
            return this.entityConstructor.newInstance(type, world);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            com.lycanitesmobs.LycanitesMobs.LOGGER.warn("[Lycanites]: [Creature] Failed to create entity for " + this.getName(), cause);
            return null;
        }
    }

    /**
     * Returns a boolean flag that this creature has.
     *
     * @param flagName     The name of the flag to get.
     * @param defaultValue The value to return if the flag is missing.
     * @return The flag value.
     */
    public boolean getFlag(String flagName, boolean defaultValue) {
        if (!this.boolFlags.containsKey(flagName)) {
            return defaultValue;
        }
        return this.boolFlags.get(flagName);
    }

    /**
     * Returns an int flag that this creature has rounded into an integer.
     *
     * @param flagName     The name of the flag to get.
     * @param defaultValue The value to return if the flag is missing.
     * @return The flag value.
     */
    public int getFlag(String flagName, int defaultValue) {
        if (!this.doubleFlags.containsKey(flagName)) {
            return defaultValue;
        }
        return Math.round(this.doubleFlags.get(flagName).floatValue());
    }

    /**
     * Returns a double flag that this creature has.
     *
     * @param flagName     The name of the flag to get.
     * @param defaultValue The value to return if the flag is missing.
     * @return The flag value.
     */
    public double getFlag(String flagName, double defaultValue) {
        if (!this.doubleFlags.containsKey(flagName)) {
            return defaultValue;
        }
        return this.doubleFlags.get(flagName);
    }

    /**
     * Returns a string flag that this creature has.
     *
     * @param flagName     The name of the flag to get.
     * @param defaultValue The value to return if the flag is missing.
     * @return The flag value.
     */
    public String getFlag(String flagName, String defaultValue) {
        if (!this.stringFlags.containsKey(flagName)) {
            return defaultValue;
        }
        return this.stringFlags.get(flagName);
    }
}
