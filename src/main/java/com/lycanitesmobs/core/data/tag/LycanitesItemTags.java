package com.lycanitesmobs.core.data.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class LycanitesItemTags {
    public static final TagKey<Item> SUMMONING_PEDESTAL_FUEL = create("summoning_pedestal_fuel");
    public static final TagKey<Item> SUMMONING_PEDESTAL_DENSE_FUEL = create("summoning_pedestal_dense_fuel");
    public static final TagKey<Item> SUMMONING_STAFF_REPAIR = create("summoning_staff_repair");

    private LycanitesItemTags() {
    }

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("lycanitesmobs", name));
    }
}
