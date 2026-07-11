package com.lycanitesmobs.core.data.info.item;

import com.lycanitesmobs.core.data.config.ConfigItem;

import java.util.List;

public class ItemConfig {
    protected static double seasonalItemDropChance = 0.1D;
    protected static boolean removeOnNoFireTick = true;

    protected static boolean chargesToXPBottles = true;

    protected static boolean equipmentSpecialNames = false;

    protected static int lowEquipmentRepairAmount = 50;
    protected static int mediumEquipmentRepairAmount = 100;
    protected static int highEquipmentRepairAmount = 500;

    protected static List<? extends String> lowEquipmentSharpnessItems;
    protected static List<? extends String> mediumEquipmentSharpnessItems;
    protected static List<? extends String> highEquipmentSharpnessItems;
    protected static List<? extends String> maxEquipmentSharpnessItems;

    protected static List<? extends String> lowEquipmentManaItems;
    protected static List<? extends String> mediumEquipmentManaItems;
    protected static List<? extends String> highEquipmentManaItems;
    protected static List<? extends String> maxEquipmentManaItems;

    public static double getSeasonalItemDropChance() {
        return seasonalItemDropChance;
    }

    public static boolean removesOnNoFireTick() {
        return removeOnNoFireTick;
    }

    public static boolean convertsChargesToXpBottles() {
        return chargesToXPBottles;
    }

    public static boolean usesEquipmentSpecialNames() {
        return equipmentSpecialNames;
    }

    public static int getLowEquipmentRepairAmount() {
        return lowEquipmentRepairAmount;
    }

    public static int getMediumEquipmentRepairAmount() {
        return mediumEquipmentRepairAmount;
    }

    public static int getHighEquipmentRepairAmount() {
        return highEquipmentRepairAmount;
    }

    public static List<? extends String> getLowEquipmentSharpnessItems() {
        return lowEquipmentSharpnessItems;
    }

    public static List<? extends String> getMediumEquipmentSharpnessItems() {
        return mediumEquipmentSharpnessItems;
    }

    public static List<? extends String> getHighEquipmentSharpnessItems() {
        return highEquipmentSharpnessItems;
    }

    public static List<? extends String> getMaxEquipmentSharpnessItems() {
        return maxEquipmentSharpnessItems;
    }

    public static List<? extends String> getLowEquipmentManaItems() {
        return lowEquipmentManaItems;
    }

    public static List<? extends String> getMediumEquipmentManaItems() {
        return mediumEquipmentManaItems;
    }

    public static List<? extends String> getHighEquipmentManaItems() {
        return highEquipmentManaItems;
    }

    public static List<? extends String> getMaxEquipmentManaItems() {
        return maxEquipmentManaItems;
    }

    public static void loadGlobalSettings() {
        seasonalItemDropChance = ConfigItem.INSTANCE.seasonalDropChance.get();
        removeOnNoFireTick = ConfigItem.INSTANCE.removeOnNoFireTick.get();

        chargesToXPBottles = ConfigItem.INSTANCE.chargesToXPBottles.get();

        equipmentSpecialNames = ConfigItem.INSTANCE.equipmentSpecialNames.get();

        lowEquipmentRepairAmount = ConfigItem.INSTANCE.lowEquipmentRepairAmount.get();
        mediumEquipmentRepairAmount = ConfigItem.INSTANCE.mediumEquipmentRepairAmount.get();
        highEquipmentRepairAmount = ConfigItem.INSTANCE.highEquipmentRepairAmount.get();

        lowEquipmentSharpnessItems = ConfigItem.INSTANCE.lowEquipmentSharpnessItems.get();
        mediumEquipmentSharpnessItems = ConfigItem.INSTANCE.mediumEquipmentSharpnessItems.get();
        highEquipmentSharpnessItems = ConfigItem.INSTANCE.highEquipmentSharpnessItems.get();
        maxEquipmentSharpnessItems = ConfigItem.INSTANCE.maxEquipmentSharpnessItems.get();

        lowEquipmentManaItems = ConfigItem.INSTANCE.lowEquipmentManaItems.get();
        mediumEquipmentManaItems = ConfigItem.INSTANCE.mediumEquipmentManaItems.get();
        highEquipmentManaItems = ConfigItem.INSTANCE.highEquipmentManaItems.get();
        maxEquipmentManaItems = ConfigItem.INSTANCE.maxEquipmentManaItems.get();
    }
}