package com.lycanitesmobs.core.manager;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.creature.*;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns registration and reload-safe replacement of parsed creature definitions.
 */
public final class CreatureDefinitionRegistryHelper {
    private CreatureDefinitionRegistryHelper() {
    }

    public static void loadCreatureType(CreatureManager creatureManager, ModInfo modInfo, JsonObject json) {
        CreatureType creatureType = new CreatureType(modInfo);
        creatureType.loadFromJson(json);

        if (creatureManager.creatureTypes.containsKey(creatureType.getName())) {
            LMHelperClass.logWarningMessage("[Creature Type] Duplicate creature type definition found, reloading existing definition: " + creatureType.getName());
            creatureType = creatureManager.creatureTypes.get(creatureType.getName());
            creatureType.loadFromJson(json);
        }

        creatureManager.creatureTypes.put(creatureType.getName(), creatureType);
    }

    public static void loadCreatureGroup(CreatureManager creatureManager, JsonObject json) {
        CreatureGroup creatureGroup = new CreatureGroup();
        creatureGroup.loadFromJson(json);

        if (creatureManager.creatureGroups.containsKey(creatureGroup.getName())) {
            LMHelperClass.logWarningMessage("[Creature Group] Duplicate creature group definition found, reloading existing definition: " + creatureGroup.getName());
            creatureGroup = creatureManager.creatureGroups.get(creatureGroup.getName());
            creatureGroup.loadFromJson(json);
        }

        creatureManager.creatureGroups.put(creatureGroup.getName(), creatureGroup);
    }

    public static void loadCreature(CreatureManager creatureManager, ElementManager elementManager, ModInfo modInfo, JsonObject json) {
        CreatureInfo parsedCreatureInfo = new CreatureInfo(modInfo);
        if (!parsedCreatureInfo.loadFromJson(json)) {
            return;
        }

        CreatureInfo creatureInfo = parsedCreatureInfo;
        if (creatureManager.creatures.containsKey(parsedCreatureInfo.getName())) {
            LMHelperClass.logWarningMessage("[Creature] Duplicate creature definition found, reloading existing definition: " + parsedCreatureInfo.getName());
            creatureInfo = creatureManager.creatures.get(parsedCreatureInfo.getName());
            creatureManager.creatureClassMap.remove(creatureInfo.getEntityClass());
            if (!creatureInfo.loadFromJson(json)) {
                return;
            }
        }

        if (!creatureInfo.isDummy()) {
            CreatureDefinitionValidationHelper.validateParsedReferences(creatureInfo, creatureManager, elementManager);
            link(creatureInfo, creatureManager, elementManager);
        }
        creatureManager.creatures.put(creatureInfo.getName(), creatureInfo);
        creatureManager.creatureClassMap.put(creatureInfo.getEntityClass(), creatureInfo);
    }

    public static void link(CreatureInfo creatureInfo, CreatureManager creatureManager, ElementManager elementManager) {
        String creatureTypeName = creatureInfo.getCreatureTypeName();
        creatureInfo.setCreatureType(creatureManager.getCreatureType(creatureTypeName));
        if (creatureInfo.getCreatureType() == null) {
            creatureInfo.setCreatureType(creatureManager.getCreatureType("beast"));
        }
        if (creatureInfo.getCreatureType() != null) {
            creatureInfo.getCreatureType().addCreature(creatureInfo);
        }

        for (String groupName : creatureInfo.getCreatureGroupNames()) {
            CreatureGroup group = creatureManager.getCreatureGroup(groupName);
            if (group != null) {
                creatureInfo.addLinkedGroup(group);
                group.addCreature(creatureInfo);
            }
        }

        linkElements(creatureInfo, elementManager);
    }

    private static void linkElements(CreatureInfo creatureInfo, ElementManager elementManager) {
        List<ElementInfo> elements = new ArrayList<>();
        for (String elementName : creatureInfo.getElementNames()) {
            ElementInfo element = elementManager.getElement(elementName);
            if (element == null) {
                continue;
            }
            elements.add(element);
        }
        creatureInfo.setLinkedElements(elements);

        for (Subspecies subspecies : creatureInfo.getSubspecies().values()) {
            List<ElementInfo> subspeciesElements = new ArrayList<>();
            for (String elementName : subspecies.getElementNames()) {
                ElementInfo element = elementManager.getElement(elementName);
                if (element == null) {
                    continue;
                }
                subspeciesElements.add(element);
            }
            subspecies.setLinkedElements(subspeciesElements);
        }
    }
}
