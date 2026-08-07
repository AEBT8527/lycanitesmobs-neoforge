package com.lycanitesmobs.core.data.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class LycanitesBlockTags {
    public static final TagKey<Block> WORM_BURROWABLE = create("worm_burrowable");
    public static final TagKey<Block> EQUIPMENT_HARVEST_PICKAXE = create("equipment_harvest/pickaxe");
    public static final TagKey<Block> EQUIPMENT_HARVEST_AXE = create("equipment_harvest/axe");
    public static final TagKey<Block> EQUIPMENT_HARVEST_SHOVEL = create("equipment_harvest/shovel");
    public static final TagKey<Block> CREATURE_PATH_GRASS_PREFERRED = create("creature_path/grass_preferred");
    public static final TagKey<Block> CREATURE_PATH_DIRT_PREFERRED = create("creature_path/dirt_preferred");
    public static final TagKey<Block> CREATURE_PATH_SNOW_PREFERRED = create("creature_path/snow_preferred");
    public static final TagKey<Block> CREATURE_PATH_ICE_PREFERRED = create("creature_path/ice_preferred");
    public static final TagKey<Block> CREATURE_PATH_SAND_PREFERRED = create("creature_path/sand_preferred");
    public static final TagKey<Block> CREATURE_PATH_CLAY_PREFERRED = create("creature_path/clay_preferred");
    public static final TagKey<Block> CREATURE_PATH_STONE_PREFERRED = create("creature_path/stone_preferred");
    public static final TagKey<Block> YALE_GRAZABLE = create("yale_grazable");
    public static final TagKey<Block> EREPEDE_SPEED_BOOST = create("erepede_speed_boost");
    public static final TagKey<Block> CHERUFE_MELTABLE = create("cherufe_meltable");
    public static final TagKey<Block> VOLCAN_MELTABLE = create("volcan_meltable");
    public static final TagKey<Block> EECHETIK_MYCELIUM_CONVERTIBLE = create("eechetik_mycelium_convertible");
    public static final TagKey<Block> CREATURE_STRUCTURE_REPLACEABLE = create("creature_structure_replaceable");
    public static final TagKey<Block> POISON_CLOUD_PERSISTENT_BASE = create("poison_cloud_persistent_base");
    public static final TagKey<Block> ASPID_POISON_CLOUD_REPLACEABLE = create("aspid_poison_cloud_replaceable");
    public static final TagKey<Block> BOBEKO_FROST_CLOUD_REPLACEABLE = create("bobeko_frost_cloud_replaceable");
    public static final TagKey<Block> CHERUFE_FIRE_TRAIL_REPLACEABLE = create("cherufe_fire_trail_replaceable");
    public static final TagKey<Block> WENDIGO_FROSTFIRE_TRAIL_REPLACEABLE = create("wendigo_frostfire_trail_replaceable");
    public static final TagKey<Block> WENDIGO_FREEZABLE = create("wendigo_freezable");
    public static final TagKey<Block> BEHEMOPHET_HELLFIRE_TRAIL_REPLACEABLE = create("behemophet_hellfire_trail_replaceable");
    public static final TagKey<Block> SPAWNER_ORE_TRIGGER_ORES = create("spawner/ore_trigger_ores");
    public static final TagKey<Block> SPAWNER_ORE_TRIGGER_GEMS = create("spawner/ore_trigger_gems");
    public static final TagKey<Block> SPAWNER_ORE_LEVEL_1 = create("spawner/ore_level_1");
    public static final TagKey<Block> SPAWNER_ORE_LEVEL_2 = create("spawner/ore_level_2");
    public static final TagKey<Block> SPAWNER_ORE_LEVEL_3 = create("spawner/ore_level_3");
    public static final TagKey<Block> FROSTFIRE_CATCHABLE = create("frostfire/catchable");
    public static final TagKey<Block> FROSTFIRE_SOURCE = create("frostfire/source");
    public static final TagKey<Block> FROSTFIRE_PACKABLE = create("frostfire/packable");
    public static final TagKey<Block> HELLFIRE_SOURCE = create("hellfire/source");
    public static final TagKey<Block> SMITEFIRE_SOURCE = create("smitefire/source");

    private LycanitesBlockTags() {
    }

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("lycanitesmobs", name));
    }
}
