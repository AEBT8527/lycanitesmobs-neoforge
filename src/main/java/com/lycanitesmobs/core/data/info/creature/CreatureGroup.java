package com.lycanitesmobs.core.data.info.creature;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.entity.EntityTypeGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CreatureGroup {
    /**
     * A list of all creatures in this group.
     **/
    protected List<CreatureInfo> creatures = new ArrayList<>();

    /**
     * The name of this creature group.
     **/
    protected String name;

    /**
     * A list of groups that this group will actively hunt.
     **/
    protected List<CreatureGroup> huntGroups = new ArrayList<>();
    protected List<String> huntGroupNames = new ArrayList<>();

    /**
     * A list of groups that this group will actively hunt if in a pack.
     **/
    protected List<CreatureGroup> packGroups = new ArrayList<>();
    protected List<String> packGroupNames = new ArrayList<>();

    /**
     * A list of groups that this group will ignore but run from if hit by.
     **/
    public List<CreatureGroup> waryGroups = new ArrayList<>();
    protected List<String> waryGroupNames = new ArrayList<>();

    /**
     * A list of groups that this group will actively flee from.
     **/
    public List<CreatureGroup> fleeGroups = new ArrayList<>();
    protected List<String> fleeGroupNames = new ArrayList<>();

    /**
     * A list of groups that this group will ignore completely.
     **/
    public List<CreatureGroup> ignoreGroups = new ArrayList<>();
    protected List<String> ignoreGroupNames = new ArrayList<>();

    enum Interaction {
        HUNT("hunt"), PACKHUNT("pack"), WARY("wary"), FLEE("flee"), IGNORE("ignore"), RETALIATE("retaliate");
        private final String name;

        Interaction(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    /**
     * The default interaction towards a group not in any interaction lists.
     **/
    protected Interaction defaultInteraction = Interaction.HUNT;

    /**
     * If true, this group includes animals like Sheep, Cows and Pigs.
     **/
    protected boolean animals = false;

    /**
     * If true, this group includes humanoids like Players, Villagers or Pillagers.
     **/
    protected boolean humanoids = false;

    /**
     * If true, this group includes Players.
     **/
    protected boolean players = false;

    /**
     * If true, this group includes humanoids Villagers but not Pillagers.
     **/
    protected boolean villagers = false;

    /**
     * If true, this group includes raiders like Pillagers and Ravagers.
     **/
    protected boolean raiders = false;

    /**
     * If true, this group includes Snow Golems.
     **/
    protected boolean frozen = false;

    /**
     * If true, this group includes Blazes and Magma Cubes.
     **/
    protected boolean inferno = false;

    /**
     * A list of additional entity ids in this group.
     **/
    protected List<String> entityIds = new ArrayList<>();

    /**
     * Loads this creature group from json.
     */
    public void loadFromJson(JsonObject json) {
        this.name = json.get("name").getAsString();

        // Interaction Lists:
        if (json.has("hunt")) {
            this.huntGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("hunt"));
        }
        if (json.has("pack")) {
            this.packGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("pack"));
        }
        if (json.has("wary")) {
            this.waryGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("wary"));
        }
        if (json.has("flee")) {
            this.fleeGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("flee"));
        }
        if (json.has("ignore")) {
            this.ignoreGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("ignore"));
        }

        // Interactions:
        if (json.has("default")) {
            this.defaultInteraction = Interaction.valueOf(json.get("default").getAsString().toUpperCase(Locale.ENGLISH));
        }

        // Special Entities:
        if (json.has("animals")) {
            this.animals = json.get("animals").getAsBoolean();
        }
        if (json.has("humanoids")) {
            this.humanoids = json.get("humanoids").getAsBoolean();
        }
        if (json.has("players")) {
            this.players = json.get("players").getAsBoolean();
        }
        if (json.has("villagers")) {
            this.villagers = json.get("villagers").getAsBoolean();
        }
        if (json.has("raiders")) {
            this.raiders = json.get("raiders").getAsBoolean();
        }
        if (json.has("frozen")) {
            this.frozen = json.get("frozen").getAsBoolean();
        }
        if (json.has("inferno")) {
            this.inferno = json.get("inferno").getAsBoolean();
        }
        if (json.has("entityIds")) {
            this.entityIds = JSONHelper.getJsonStrings(json.get("entityIds").getAsJsonArray());
        }
    }

    /**
     * Gets the name of this group.
     *
     * @return The group name.
     */
    public String getName() {
        return this.name;
    }

    public List<CreatureGroup> getHuntGroups() {
        return Collections.unmodifiableList(this.huntGroups);
    }

    public List<CreatureGroup> getPackGroups() {
        return Collections.unmodifiableList(this.packGroups);
    }

    public List<String> getHuntGroupNames() {
        return Collections.unmodifiableList(this.huntGroupNames);
    }

    public List<String> getPackGroupNames() {
        return Collections.unmodifiableList(this.packGroupNames);
    }

    public List<String> getWaryGroupNames() {
        return Collections.unmodifiableList(this.waryGroupNames);
    }

    public List<String> getFleeGroupNames() {
        return Collections.unmodifiableList(this.fleeGroupNames);
    }

    public List<String> getIgnoreGroupNames() {
        return Collections.unmodifiableList(this.ignoreGroupNames);
    }

    public void clearLinkedInteractionGroups() {
        this.huntGroups.clear();
        this.packGroups.clear();
        this.waryGroups.clear();
        this.fleeGroups.clear();
        this.ignoreGroups.clear();
    }

    public void addHuntGroup(CreatureGroup group) {
        this.huntGroups.add(group);
    }

    public void addPackGroup(CreatureGroup group) {
        this.packGroups.add(group);
    }

    public void addWaryGroup(CreatureGroup group) {
        this.waryGroups.add(group);
    }

    public void addFleeGroup(CreatureGroup group) {
        this.fleeGroups.add(group);
    }

    public void addIgnoreGroup(CreatureGroup group) {
        this.ignoreGroups.add(group);
    }

    public boolean includesHumanoids() {
        return this.humanoids;
    }

    /**
     * Adds a creature to this group.
     *
     * @param creature The creature to add.
     */
    public void addCreature(CreatureInfo creature) {
        if (this.creatures.contains(creature))
            return;
        this.creatures.add(creature);
    }

    /**
     * Removes a creature from this group, used before re-linking reloaded creature definitions.
     *
     * @param creature The creature to remove.
     */
    public void removeCreature(CreatureInfo creature) {
        this.creatures.remove(creature);
    }

    /**
     * Returns if this group has the provided entity in it.
     *
     * @param entity The entity to check for.
     * @return True if the entity is in this group, otherwise false.
     */
    public boolean hasEntity(@Nonnull Entity entity) {
        if (entity instanceof BaseCreatureEntity) {
            return ((BaseCreatureEntity) entity).getCreatureInfo().isInGroup(this);
        }
        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        if (this.animals && entity instanceof Animal) {
            return true;
        }
        if (this.humanoids && (entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.VILLAGER || entity instanceof AbstractIllager || entity instanceof AbstractPiglin)) {
            return true;
        }
        if (this.players && entity.getType() == EntityType.PLAYER) {
            return true;
        }
        if (this.villagers && (entity.getType() == EntityType.VILLAGER || entity instanceof AbstractIllager || entity instanceof AbstractPiglin)) {
            return true;
        }
        if (this.raiders && (entity.getType() == EntityType.PILLAGER || entity.getType() == EntityType.RAVAGER)) {
            return true;
        }
        if (this.frozen && entity.getType() == EntityType.SNOW_GOLEM) {
            return true;
        }
        if (this.inferno && (entity.getType() == EntityType.BLAZE || entity.getType() == EntityType.MAGMA_CUBE)) {
            return true;
        }
        Identifier entityResourceLocation = null;
        if (entity.getType() instanceof EntityTypeGetter getter) {
            entityResourceLocation = getter.getRegistryName();
        }
        if (entityResourceLocation == null) {
            return false;
        }
        String entityId = entityResourceLocation.toString();
        if (this.entityIds.contains(entityId)) {
            return true;
        }

        return false;
    }

    /**
     * Returns if this group should fight back if hit by the entity.
     *
     * @param entity The target entity.
     * @return True if this group should retaliate.
     */
    public boolean shouldRevenge(Entity entity) {
        for (CreatureGroup group : this.waryGroups) {
            if (group.hasEntity(entity))
                return false;
        }
        for (CreatureGroup group : this.fleeGroups) {
            if (group.hasEntity(entity))
                return false;
        }
        for (CreatureGroup group : this.ignoreGroups) {
            if (group.hasEntity(entity))
                return false;
        }
        return this.defaultInteraction == Interaction.HUNT || this.defaultInteraction == Interaction.RETALIATE;
    }

    /**
     * Returns if this group should hunt the entity.
     *
     * @param entity The target entity.
     * @return True if this group should hunt.
     */
    public boolean shouldHunt(Entity entity) {
        for (CreatureGroup group : this.ignoreGroups) {
            if (group.hasEntity(entity))
                return false;
        }
        for (CreatureGroup group : this.huntGroups) {
            if (group.hasEntity(entity))
                return true;
        }
        return this.defaultInteraction == Interaction.HUNT;
    }

    /**
     * Returns if this group should hunt the entity when in a pack, this overrides everything.
     *
     * @param entity The target entity.
     * @return True if this group should hunt when in a pack.
     */
    public boolean shouldPackHunt(Entity entity) {
        for (CreatureGroup group : this.ignoreGroups) {
            if (group.hasEntity(entity))
                return false;
        }
        for (CreatureGroup group : this.packGroups) {
            if (group.hasEntity(entity))
                return true;
        }
        return this.defaultInteraction == Interaction.PACKHUNT;
    }

    /**
     * Returns if this group should flee from the entity on sight.
     *
     * @param entity The target entity.
     * @return True if this group should flee on sight.
     */
    public boolean shouldFlee(Entity entity) {
        for (CreatureGroup group : this.fleeGroups) {
            if (group.hasEntity(entity))
                return true;
        }
        return this.defaultInteraction == Interaction.FLEE;
    }
}
