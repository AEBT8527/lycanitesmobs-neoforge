package com.lycanitesmobs.core.worldgen.dungeon.instance;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class DungeonBuildPlan {
    private final Map<ChunkPos, List<PlannedBlock>> blocksByChunk = new HashMap<>();
    private boolean ready = false;

    public void add(ChunkPos chunkPos, PlannedBlock block) {
        List<PlannedBlock> list = this.blocksByChunk.computeIfAbsent(chunkPos, k -> new ArrayList<>());
        list.add(block);
    }

    public List<PlannedBlock> getForChunk(ChunkPos chunkPos) {
        return this.blocksByChunk.getOrDefault(chunkPos, Collections.emptyList());
    }

    public Set<ChunkPos> getPlannedChunks() {
        return this.blocksByChunk.keySet();
    }

    public int getPlannedChunkCount() {
        return this.blocksByChunk.size();
    }

    public boolean isReady() {
        return this.ready;
    }

    public void markReady() {
        this.ready = true;
    }

    public record PlannedBlock(BlockPos pos, BlockState state, Direction facing) {
        public void apply(DungeonChunkBuildContext context, SectorInstance sector) {
            LMHelperClass.logDebug(
                    "Dungeon",
                    "PlannedBlock.apply: " + this.state.getBlock() + " at " + this.pos + " in chunk " + context.chunkPos()
            );
            sector.placeBlock(context.worldWriter(), context.chunkPos(), this.pos, this.state, this.facing, context.random());
        }
    }

}
