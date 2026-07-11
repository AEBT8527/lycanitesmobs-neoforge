package com.lycanitesmobs.core.worldgen.dungeon.instance;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

record DungeonChunkBuildContext(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {

    void buildSector(SectorInstance sector) {
        sector.build(this.worldWriter, this.world, this.chunkPos, this.random);
    }

    void applyPlannedBlock(DungeonBuildPlan.PlannedBlock block, SectorInstance sector) {
        block.apply(this, sector);
    }
}

record DungeonChunkBuildProgress(int chunksBuilt, int expectedChunks, boolean complete) {
}
