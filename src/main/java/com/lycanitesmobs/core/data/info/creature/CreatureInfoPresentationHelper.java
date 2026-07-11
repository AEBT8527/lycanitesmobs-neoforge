package com.lycanitesmobs.core.data.info.creature;

import com.lycanitesmobs.core.data.info.Variant;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.data.info.item.ItemDrop;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds creature presentation data for beastiary and UI compatibility callers.
 */
public final class CreatureInfoPresentationHelper {
    private CreatureInfoPresentationHelper() {
    }

    public static Component getTitle(CreatureInfo creatureInfo) {
        return Component.translatable("entity." + creatureInfo.getLocalisationKey());
    }

    public static MutableComponent getDescription(CreatureInfo creatureInfo) {
        return Component.translatable("entity." + creatureInfo.getLocalisationKey() + ".description");
    }

    public static MutableComponent getHabitatDescription(CreatureInfo creatureInfo) {
        return Component.translatable("entity." + creatureInfo.getLocalisationKey() + ".habitat");
    }

    public static MutableComponent getCombatDescription(CreatureInfo creatureInfo) {
        return Component.translatable("entity." + creatureInfo.getLocalisationKey() + ".combat");
    }

    public static MutableComponent getElementNames(CreatureInfo creatureInfo, Subspecies subspecies) {
        List<ElementInfo> elements = creatureInfo.elements;
        if (subspecies != null && !subspecies.getElements().isEmpty()) {
            elements = subspecies.getElements();
        }
        if (elements.isEmpty()) {
            return Component.translatable("common.none");
        }
        MutableComponent elementNames = Component.literal("");
        boolean firstElement = true;
        for (ElementInfo element : elements) {
            if (!firstElement) {
                elementNames.append(", ");
            }
            firstElement = false;
            elementNames.append(element.getTitle());
        }
        return elementNames;
    }

    public static MutableComponent getDietNames(CreatureInfo creatureInfo) {
        if (creatureInfo.diets.isEmpty()) {
            return Component.translatable("common.none");
        }
        MutableComponent dietNames = Component.literal("");
        boolean firstDiet = true;
        for (String diet : creatureInfo.diets) {
            if (!firstDiet) {
                dietNames.append(", ");
            }
            firstDiet = false;
            dietNames.append(Component.translatable("diet." + diet));
        }
        return dietNames;
    }

    public static MutableComponent getBiomeNames(CreatureInfo creatureInfo) {
        List<String> biomeIds = new ArrayList<>();
        biomeIds.addAll(creatureInfo.creatureSpawn.getResolvedBiomeTags());
        biomeIds.addAll(creatureInfo.creatureSpawn.getBiomeIds());
        if (biomeIds.isEmpty()) {
            return Component.translatable("gui.beastiary.biomes.none");
        }
        MutableComponent biomeNames = Component.literal("");
        boolean firstBiome = true;
        for (String biomeId : biomeIds) {
            if (!firstBiome) {
                biomeNames.append(", ");
            }
            firstBiome = false;
            biomeNames.append(Component.literal(biomeId));
        }
        return biomeNames;
    }

    public static MutableComponent getDropNames(CreatureInfo creatureInfo) {
        if (creatureInfo.drops.isEmpty()) {
            return Component.literal("");
        }
        MutableComponent dropNames = Component.literal("");
        boolean firstDrop = true;
        for (ItemDrop drop : creatureInfo.drops) {
            if (!firstDrop) {
                dropNames.append("\n");
            }
            firstDrop = false;
            appendDropName(creatureInfo, dropNames, drop);
        }
        return dropNames;
    }

    public static ResourceLocation getIcon(CreatureInfo creatureInfo) {
        return AssetHelper.creatureIcon(creatureInfo.getName());
    }

    private static void appendDropName(CreatureInfo creatureInfo, MutableComponent dropNames, ItemDrop drop) {
        dropNames.append(drop.getItemStack().getHoverName());
        dropNames.append(" (");

        if (drop.getMaxAmount() > drop.getMinAmount()) {
            dropNames.append(drop.getMinAmount() + "-" + drop.getMaxAmount() + "X");
        } else {
            dropNames.append(drop.getMinAmount() + "X");
        }

        dropNames.append(" " + (drop.getChance() * 100) + "%");

        Subspecies subspecies = creatureInfo.getSubspecies(0);
        if (drop.getSubspeciesIndex() > 0) {
            subspecies = creatureInfo.getSubspecies(drop.getSubspeciesIndex());
            if (subspecies.getName() != null) {
                dropNames.append(" ");
                dropNames.append(subspecies.getTitle());
            }
        }

        if (drop.getVariantIndex() >= 0) {
            dropNames.append(" ");
            Variant variant = subspecies.getVariant(drop.getVariantIndex());
            if (variant == null) {
                dropNames.append(Component.translatable("subspecies.normal"));
            } else {
                dropNames.append(variant.getTitle());
            }
        }

        dropNames.append(")");
    }
}
