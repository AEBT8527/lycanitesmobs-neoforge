package com.lycanitesmobs.core.worldgen.dungeon.instance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

record SectorBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
    static SectorBounds between(BlockPos first, BlockPos second) {
        return new SectorBounds(
                Math.min(first.getX(), second.getX()),
                Math.max(first.getX(), second.getX()),
                Math.min(first.getY(), second.getY()),
                Math.max(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()),
                Math.max(first.getZ(), second.getZ())
        );
    }

    SectorBounds offset(int x, int y, int z) {
        return new SectorBounds(
                this.minX + x,
                this.maxX + x,
                this.minY + y,
                this.maxY + y,
                this.minZ + z,
                this.maxZ + z
        );
    }

    SectorBounds withMinY(int minY) {
        return new SectorBounds(this.minX, this.maxX, minY, this.maxY, this.minZ, this.maxZ);
    }

    SectorBounds withYRange(int minY, int maxY) {
        return new SectorBounds(this.minX, this.maxX, minY, maxY, this.minZ, this.maxZ);
    }

    SectorBounds clipToChunk(ChunkPos chunkPos) {
        return new SectorBounds(
                Math.max(this.minX, chunkPos.getMinBlockX()),
                Math.min(this.maxX, chunkPos.getMaxBlockX()),
                this.minY,
                this.maxY,
                Math.max(this.minZ, chunkPos.getMinBlockZ()),
                Math.min(this.maxZ, chunkPos.getMaxBlockZ())
        );
    }

    boolean hasHorizontalArea() {
        return this.minX <= this.maxX && this.minZ <= this.maxZ;
    }

    int widthX() {
        return this.maxX - this.minX;
    }

    int widthZ() {
        return this.maxZ - this.minZ;
    }
}
