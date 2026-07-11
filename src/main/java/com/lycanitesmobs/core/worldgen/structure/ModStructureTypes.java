package com.lycanitesmobs.core.worldgen.structure;

import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class ModStructureTypes {

    public static StructureType<?> LM_DUNGEON;

    @SuppressWarnings("unchecked")
    public static void init() {
        LM_DUNGEON = Registry.register(
                BuiltInRegistries.STRUCTURE_TYPE,
                AssetHelper.modResource("lm_dungeon"),
                (StructureType<LMDungeonStructure>) () -> LMDungeonStructure.CODEC);
    }
}
