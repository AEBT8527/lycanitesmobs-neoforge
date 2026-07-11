package com.lycanitesmobs.core.data.info.creature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.info.item.ItemDrop;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns the JSON-to-CreatureInfo definition parse lifecycle.
 */
final class CreatureInfoJsonParser {
    private CreatureInfoJsonParser() {
    }

    static boolean parse(CreatureInfo creatureInfo, JsonObject json) {
        creatureInfo.resetForJsonLoad();
        if (!parseIdentity(creatureInfo, json)) {
            return false;
        }
        if (creatureInfo.dummy) {
            return true;
        }

        if (!parseSpawn(creatureInfo, json)) {
            return false;
        }
        parseStats(creatureInfo, json);
        if (!parseSubspeciesAndElements(creatureInfo, json)) {
            return false;
        }
        parseFeatures(creatureInfo, json);
        parseDrops(creatureInfo, json);
        parseFlags(creatureInfo, json);
        return true;
    }

    private static boolean parseIdentity(CreatureInfo creatureInfo, JsonObject json) {
        if (!json.has("name")) {
            LMHelperClass.logWarningMessage("[Creature] Skipping creature with missing name.");
            return false;
        }
        creatureInfo.name = json.get("name").getAsString();

        String entityClassName = json.has("entityClass") ? json.get("entityClass").getAsString() : "";
        if (!resolveEntityDefinition(creatureInfo, entityClassName)) {
            return false;
        }

        if (json.has("enabled")) {
            creatureInfo.enabled = json.get("enabled").getAsBoolean();
        }
        if (json.has("dummy")) {
            creatureInfo.dummy = json.get("dummy").getAsBoolean();
        }
        if (creatureInfo.dummy) {
            return true;
        }

        if (!json.has("modelClass")) {
            LMHelperClass.logWarningMessage("[Creature] Skipping creature '" + creatureInfo.getName() + "': missing modelClass. Clear your creature configs at: config/lycanitesmobs/creatures/" + creatureInfo.getName() + ".json");
            return false;
        }
        creatureInfo.modelClassName = json.get("modelClass").getAsString();
        creatureInfo.creatureTypeName = json.has("creatureType") ? json.get("creatureType").getAsString() : "beast";
        if (json.has("groups")) {
            creatureInfo.creatureGroupNames = JSONHelper.getJsonStrings(json.getAsJsonArray("groups"));
        }
        return true;
    }

    static boolean resolveEntityDefinition(CreatureInfo creatureInfo, String entityClassName) {
        try {
            creatureInfo.entityClass = (Class<? extends BaseCreatureEntity>) Class.forName(entityClassName);
            creatureInfo.entityConstructor = creatureInfo.entityClass.getConstructor(EntityType.class, Level.class);
            return true;
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("[Creature] Skipping creature '" + creatureInfo.getName() + "': unable to find entity class '" + entityClassName + "'. If you have updated the mod, clear your creature configs at: config/lycanitesmobs/creatures/" + creatureInfo.getName() + ".json");
            return false;
        }
    }

    private static boolean parseSpawn(CreatureInfo creatureInfo, JsonObject json) {
        if (!json.has("spawning")) {
            LMHelperClass.logWarningMessage("[Creature] Skipping creature '" + creatureInfo.getName() + "': missing spawning definition. Clear your creature configs at: config/lycanitesmobs/creatures/" + creatureInfo.getName() + ".json");
            return false;
        }
        creatureInfo.creatureSpawn.loadFromJSON(creatureInfo, json.get("spawning").getAsJsonObject());
        return true;
    }

    private static void parseStats(CreatureInfo creatureInfo, JsonObject json) {
        if (json.has("width"))
            creatureInfo.width = json.get("width").getAsDouble();
        if (json.has("height"))
            creatureInfo.height = json.get("height").getAsDouble();
        if (json.has("sizeScale"))
            creatureInfo.sizeScale = json.get("sizeScale").getAsDouble();
        if (json.has("hitboxScale"))
            creatureInfo.hitboxScale = json.get("hitboxScale").getAsDouble();
        creatureInfo.mountOffset = JSONHelper.getVector3d(json, "mountOffset", creatureInfo.mountOffset);

        if (json.has("experience"))
            creatureInfo.experience = json.get("experience").getAsInt();
        if (json.has("health"))
            creatureInfo.health = json.get("health").getAsDouble();
        if (json.has("defense"))
            creatureInfo.defense = json.get("defense").getAsDouble();
        if (json.has("armor"))
            creatureInfo.armor = json.get("armor").getAsDouble();
        if (json.has("speed"))
            creatureInfo.speed = json.get("speed").getAsDouble();
        if (json.has("damage"))
            creatureInfo.damage = json.get("damage").getAsDouble();
        if (json.has("attackSpeed"))
            creatureInfo.attackSpeed = json.get("attackSpeed").getAsDouble();
        if (json.has("rangedSpeed"))
            creatureInfo.rangedSpeed = json.get("rangedSpeed").getAsDouble();
        if (json.has("effectDuration"))
            creatureInfo.effectDuration = json.get("effectDuration").getAsDouble();
        if (json.has("effectAmplifier"))
            creatureInfo.effectAmplifier = json.get("effectAmplifier").getAsDouble();
        if (json.has("pierce"))
            creatureInfo.pierce = json.get("pierce").getAsDouble();
        if (json.has("knockbackResistance"))
            creatureInfo.knockbackResistance = json.get("knockbackResistance").getAsDouble();
        if (json.has("sight"))
            creatureInfo.sight = json.get("sight").getAsDouble();
        if (json.has("packSize"))
            creatureInfo.packSize = json.get("packSize").getAsInt();
        if (json.has("tamingReputation"))
            creatureInfo.tamingReputation = json.get("tamingReputation").getAsInt();
        if (json.has("bagSize"))
            creatureInfo.bagSize = json.get("bagSize").getAsInt();

        creatureInfo.eggBackColor = Color.decode(json.get("eggBackColor").getAsString()).getRGB();
        creatureInfo.eggForeColor = Color.decode(json.get("eggForeColor").getAsString()).getRGB();
    }

    private static boolean parseSubspeciesAndElements(CreatureInfo creatureInfo, JsonObject json) {
        if (!json.has("subspecies")) {
            LMHelperClass.logWarningMessage("[Creature] Skipping creature '" + creatureInfo.getName() + "': missing subspecies definition. Clear your creature configs at: config/lycanitesmobs/creatures/" + creatureInfo.getName() + ".json");
            return false;
        }
        for (JsonElement jsonElement : json.get("subspecies").getAsJsonArray()) {
            Subspecies subspecies = Subspecies.createFromJSON(creatureInfo, jsonElement.getAsJsonObject());
            creatureInfo.subspecies.put(subspecies.getIndex(), subspecies);
        }
        if (creatureInfo.subspecies.isEmpty()) {
            LMHelperClass.logWarningMessage("[Creature] Skipping creature '" + creatureInfo.getName() + "': no subspecies found (there should always be at least 1 default subspecies). Clear your creature configs at: config/lycanitesmobs/creatures/" + creatureInfo.getName() + ".json");
            return false;
        }

        List<String> elementNames = new ArrayList<>();
        if (json.has("element")) {
            elementNames.add(json.get("element").getAsString());
        }
        if (json.has("elements")) {
            elementNames = JSONHelper.getJsonStrings(json.get("elements").getAsJsonArray());
        }
        creatureInfo.elementNames = elementNames;

        if (json.has("diets")) {
            creatureInfo.diets = JSONHelper.getJsonStrings(json.get("diets").getAsJsonArray());
        }
        return true;
    }

    private static void parseFeatures(CreatureInfo creatureInfo, JsonObject json) {
        if (json.has("boss"))
            creatureInfo.boss = json.get("boss").getAsBoolean();
        if (json.has("peaceful"))
            creatureInfo.peaceful = json.get("peaceful").getAsBoolean();
        if (json.has("farmable"))
            creatureInfo.farmable = json.get("farmable").getAsBoolean();
        if (json.has("summonable"))
            creatureInfo.summonable = json.get("summonable").getAsBoolean();
        if (json.has("tameable"))
            creatureInfo.tameable = json.get("tameable").getAsBoolean();
        if (json.has("mountable"))
            creatureInfo.mountable = json.get("mountable").getAsBoolean();
        if (json.has("perchable"))
            creatureInfo.perchable = json.get("perchable").getAsBoolean();
        if (json.has("summonCost"))
            creatureInfo.summonCost = json.get("summonCost").getAsInt();
        if (json.has("dungeonLevel"))
            creatureInfo.dungeonLevel = json.get("dungeonLevel").getAsInt();
        if (json.has("bossNearbyRange"))
            creatureInfo.bossNearbyRange = json.get("bossNearbyRange").getAsInt();
    }

    private static void parseDrops(CreatureInfo creatureInfo, JsonObject json) {
        if (!json.has("drops")) {
            return;
        }
        for (JsonElement mobDropJson : json.getAsJsonArray("drops")) {
            ItemDrop itemDrop = ItemDrop.createFromJSON(mobDropJson.getAsJsonObject());
            if (itemDrop != null) {
                creatureInfo.drops.add(itemDrop);
            } else {
                LMHelperClass.logWarningMessage("[Creature] Unable to add item drop to creature: " + creatureInfo.getName() + ".");
            }
        }
    }

    private static void parseFlags(CreatureInfo creatureInfo, JsonObject json) {
        if (!json.has("flags")) {
            return;
        }
        for (JsonElement flagJson : json.getAsJsonArray("flags")) {
            JsonObject flagJsonObject = flagJson.getAsJsonObject();
            if (!flagJsonObject.has("name") || !flagJsonObject.has("value")) {
                LMHelperClass.logWarningMessage("Invalid creature json flag, make sure that the flag has both a name and value, skipping this flag.");
                continue;
            }
            String name = flagJsonObject.get("name").getAsString();
            JsonElement value = flagJsonObject.get("value");
            try {
                addFlag(creatureInfo, name, value.getAsBoolean());
            } catch (Exception ignored) {
            }
            try {
                addFlag(creatureInfo, name, value.getAsDouble());
            } catch (Exception ignored) {
            }
            try {
                addFlag(creatureInfo, name, value.getAsString());
            } catch (Exception ignored) {
            }
        }
    }

    static void addFlag(CreatureInfo creatureInfo, String flagName, boolean flagValue) {
        creatureInfo.boolFlags.put(flagName, flagValue);
    }

    static void addFlag(CreatureInfo creatureInfo, String flagName, double flagValue) {
        creatureInfo.doubleFlags.put(flagName, flagValue);
    }

    static void addFlag(CreatureInfo creatureInfo, String flagName, String flagValue) {
        creatureInfo.stringFlags.put(flagName, flagValue);
    }
}
