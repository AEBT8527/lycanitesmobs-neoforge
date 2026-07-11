package com.lycanitesmobs.core.data.info.creature;

import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.manager.ElementManager;

import java.util.Collection;
import java.util.List;

/**
 * Owns non-fatal validation of parsed creature definition references before linking.
 */
public final class CreatureDefinitionValidationHelper {
    private CreatureDefinitionValidationHelper() {
    }

    public static void validateParsedReferences(CreatureInfo creatureInfo, CreatureManager creatureManager, ElementManager elementManager) {
        validateCreatureType(creatureInfo, creatureManager);
        validateCreatureGroups(creatureInfo, creatureManager);
        validateSubspecies(creatureInfo);
        validateElements(creatureInfo, elementManager);
    }

    public static void validateCreatureGroups(Collection<CreatureGroup> creatureGroups, CreatureManager creatureManager) {
        for (CreatureGroup creatureGroup : creatureGroups) {
            validateCreatureGroupInteractions(creatureGroup, creatureManager);
        }
    }

    private static void validateCreatureType(CreatureInfo creatureInfo, CreatureManager creatureManager) {
        String creatureTypeName = creatureInfo.getCreatureTypeName();
        if (creatureManager.getCreatureType(creatureTypeName) == null) {
            LMHelperClass.logWarningMessage("Unable to find the creature type: " + creatureTypeName + " for Creature: " + creatureInfo.getName());
        }
    }

    private static void validateCreatureGroups(CreatureInfo creatureInfo, CreatureManager creatureManager) {
        for (String groupName : creatureInfo.getCreatureGroupNames()) {
            if (creatureManager.getCreatureGroup(groupName) == null) {
                LMHelperClass.logWarningMessage("[Creature] Unable to find the Creature Group: " + groupName + " for Creature: " + creatureInfo.getName());
            }
        }
    }

    private static void validateSubspecies(CreatureInfo creatureInfo) {
        if (!creatureInfo.subspecies.containsKey(0)) {
            LMHelperClass.logWarningMessage("[Creature] Creature '" + creatureInfo.getName() + "' has no default subspecies at index 0. Clear your creature configs at: config/lycanitesmobs/creatures/" + creatureInfo.getName() + ".json");
        }
    }

    private static void validateElements(CreatureInfo creatureInfo, ElementManager elementManager) {
        for (String elementName : creatureInfo.getElementNames()) {
            if (elementManager.getElement(elementName) == null) {
                LMHelperClass.logWarningMessage("[Creature] Skipping unknown element '" + elementName + "' for creature '" + creatureInfo.getName() + "'. Clear your creature configs at: config/lycanitesmobs/creatures/" + creatureInfo.getName() + ".json");
            }
        }

        for (Subspecies subspecies : creatureInfo.subspecies.values()) {
            for (String elementName : subspecies.getElementNames()) {
                if (elementManager.getElement(elementName) == null) {
                    LMHelperClass.logWarningMessage("[Creature] Skipping unknown element '" + elementName + "' for subspecies '" + subspecies.getName() + "'. If you have updated the mod, clear your creature configs.");
                }
            }
        }
    }

    private static void validateCreatureGroupInteractions(CreatureGroup creatureGroup, CreatureManager creatureManager) {
        validateCreatureGroupInteractionList(creatureGroup, "hunt", creatureGroup.getHuntGroupNames(), creatureManager);
        validateCreatureGroupInteractionList(creatureGroup, "pack", creatureGroup.getPackGroupNames(), creatureManager);
        validateCreatureGroupInteractionList(creatureGroup, "wary", creatureGroup.getWaryGroupNames(), creatureManager);
        validateCreatureGroupInteractionList(creatureGroup, "flee", creatureGroup.getFleeGroupNames(), creatureManager);
        validateCreatureGroupInteractionList(creatureGroup, "ignore", creatureGroup.getIgnoreGroupNames(), creatureManager);
    }

    private static void validateCreatureGroupInteractionList(CreatureGroup owner, String interaction, List<String> groupNames, CreatureManager creatureManager) {
        for (String groupName : groupNames) {
            if (groupName.equals(owner.getName())) {
                continue;
            }
            if (creatureManager.getCreatureGroup(groupName) == null) {
                LMHelperClass.logWarningMessage("[Creature Group] Unable to find " + interaction + " group: " + groupName + " for Creature Group: " + owner.getName());
            }
        }
    }
}
