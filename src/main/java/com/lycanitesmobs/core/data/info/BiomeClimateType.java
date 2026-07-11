package com.lycanitesmobs.core.data.info;

/**
 * Replacement for Forge's removed {@code BiomeManager.BiomeType} climate categories. NeoForge no
 * longer maintains a per-climate biome index, so these are kept only as labels for the mod's
 * (currently vestigial) allowed/denied climate lists and the debug commands.
 */
public enum BiomeClimateType {
    DESERT,
    WARM,
    COOL,
    ICY
}
