package com.lycanitesmobs.core.data.info.creature;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulstone;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatureType {

    // Core Info:
    /**
     * The name of this creature type. Lowercase, no space, used for language entries and for generating the entity id, etc. Required.
     **/
    protected String name;

    /**
     * The mod info of the mod this creature type belongs to.
     **/
    protected ModInfo modInfo;

    /**
     * A map of all creatures of this type by name.
     **/
    protected Map<String, CreatureInfo> creatures = new HashMap<>();

    /**
     * A list of all creatures of this type that can be tamed.
     **/
    protected List<CreatureInfo> tameableCreatures = new ArrayList<>();

    /**
     * The treat item this type uses.
     **/
    protected Lazy<Item> treat;

    /**
     * The saddle item this type uses.
     **/
    protected Lazy<Item> saddle;

    /**
     * The soulstone item this type uses.
     **/
    protected Lazy<ItemSoulstone> soulstone;

    /**
     * The spawn egg item this type uses.
     **/
    protected Lazy<Item> spawnEgg;

    /**
     * The diet type this Creature Type provides as food for predators. "none" by default.
     **/
    protected String dietProvided = "none";


    /**
     * Constructor
     *
     * @param group The group that this creature definition will belong to.
     */
    public CreatureType(ModInfo group) {
        this.modInfo = group;
    }

    /**
     * Loads this creature type from a JSON object.
     **/
    public void loadFromJson(JsonObject json) {
        this.name = json.get("name").getAsString();

        if (json.has("dietProvided"))
            this.dietProvided = json.get("dietProvided").getAsString();
    }

    /**
     * Returns the name of this creature type. Ex: beast
     *
     * @return The name of this creature type.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the mod that owns this creature type definition.
     *
     * @return The owning mod info.
     */
    public ModInfo getModInfo() {
        return this.modInfo;
    }

    /**
     * Returns a translated title for this creature type. Ex: Beast
     *
     * @return The display name of this creature type.
     */
    public MutableComponent getTitle() {
        return Component.translatable("creaturetype." + this.getName());
    }

    /**
     * Returns the creatures linked to this type for read-only iteration.
     *
     * @return The creatures belonging to this type.
     */
    public Collection<CreatureInfo> getCreatures() {
        return Collections.unmodifiableCollection(this.creatures.values());
    }

    /**
     * Returns how many creatures are linked to this type.
     *
     * @return The linked creature count.
     */
    public int getCreatureCount() {
        return this.creatures.size();
    }

    /**
     * Returns the tameable creatures linked to this type for read-only iteration.
     *
     * @return The tameable creatures belonging to this type.
     */
    public List<CreatureInfo> getTameableCreatures() {
        return Collections.unmodifiableList(this.tameableCreatures);
    }

    /**
     * Returns if this type has at least one tameable creature.
     *
     * @return True if tameable creatures are linked to this type.
     */
    public boolean hasTameableCreatures() {
        return !this.tameableCreatures.isEmpty();
    }

    /**
     * Generates a treat item name from this type. Ex: treat_beast
     *
     * @return The treat item name for this creature type.
     */
    public String getTreatName() {
        return "treat_" + this.getName();
    }

    /**
     * Registers this type's treat item handle.
     *
     * @param treat The treat item handle.
     */
    public void setTreatItem(Lazy<Item> treat) {
        this.treat = treat;
    }

    /**
     * Gets this creature type's treat item.
     *
     * @return The treat item for this creature type.
     */
    public Item getTreatItem() {
        return this.treat.get();
    }

    /**
     * Generates a saddle item name from this type. Ex: saddle_avian
     *
     * @return The saddle item name for this creature type.
     */
    public String getSaddleName() {
        return "saddle_" + this.getName();
    }

    /**
     * Registers this type's saddle item handle.
     *
     * @param saddle The saddle item handle.
     */
    public void setSaddleItem(Lazy<Item> saddle) {
        this.saddle = saddle;
    }

    /**
     * Gets this creature type's saddle item.
     *
     * @return The saddle item for this creature type.
     */
    public Item getSaddleItem() {
        return this.saddle.get();
    }

    /**
     * Generates a soulstone item name from this type. Ex: soulstone_imp
     *
     * @return The soulstone item name for this creature type.
     */
    public String getSoulstoneName() {
        return "soulstone_" + this.getName();
    }

    /**
     * Registers this type's soulstone item handle.
     *
     * @param soulstone The soulstone item handle.
     */
    public void setSoulstoneItem(Lazy<ItemSoulstone> soulstone) {
        this.soulstone = soulstone;
    }

    /**
     * Gets this creature type's soulstone item.
     *
     * @return The soulstone item for this creature type.
     */
    public ItemSoulstone getSoulstoneItem() {
        return this.soulstone.get();
    }

    /**
     * Generates a spawn egg item name from this type. Ex: spawn_insect
     *
     * @return The spawn egg item name for this creature type.
     */
    public String getSpawnEggName() {
        return "spawn_" + this.getName();
    }

    /**
     * Registers this type's spawn egg item handle.
     *
     * @param spawnEgg The spawn egg item handle.
     */
    public void setSpawnEggItem(Lazy<Item> spawnEgg) {
        this.spawnEgg = spawnEgg;
    }

    /**
     * Gets this creature type's spawn egg item.
     * Note: The Spawn Egg item requires NBT data to determine which specific Creature it spawns.
     *
     * @return The spawn egg item for this creature type.
     */
    public Item getSpawnEggItem() {
        return this.spawnEgg.get();
    }

    /**
     * Returns the diet this creature type provides as food for predators.
     *
     * @return The provided diet name.
     */
    public String getDietProvided() {
        return this.dietProvided;
    }

    /**
     * Adds a creature to this Creature Type.
     *
     * @param creatureInfo The creature to add.
     * @return
     */
    public void addCreature(CreatureInfo creatureInfo) {
        if (this.creatures.containsKey(creatureInfo.getName())) {
            return;
        }
        this.creatures.put(creatureInfo.getName(), creatureInfo);
        if (creatureInfo.isTameable()) {
            this.tameableCreatures.add(creatureInfo);
        }
    }

    /**
     * Removes a creature from this Creature Type, used before re-linking reloaded creature definitions.
     *
     * @param creatureInfo The creature to remove.
     */
    public void removeCreature(CreatureInfo creatureInfo) {
        this.creatures.remove(creatureInfo.getName());
        this.tameableCreatures.remove(creatureInfo);
    }
}
