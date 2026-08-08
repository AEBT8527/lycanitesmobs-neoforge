package com.lycanitesmobs.core.entity.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.entity.spawner.CoordSorterFurthest;
import com.lycanitesmobs.core.entity.spawner.CoordSorterNearest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

public class SpawnLocation {
    /** Spawn Locations define where spawns will take place, how these work can vary based on the type of Spawn Trigger. **/

    /**
     * The minimum xyz distances in blocks from the central spawn position to spawn from. Required.
     **/
    protected Vec3i rangeMin = new Vec3i(0, 0, 0);

    /**
     * The maximum xyz distances in blocks from the central spawn position to spawn from. Required.
     **/
    protected Vec3i rangeMax = new Vec3i(0, 0, 0);

    /**
     * The minimum allowed y height.
     **/
    protected int yMin = -1;

    /**
     * The maximum allowed y height.
     **/
    protected int yMax = -1;

    /**
     * Determines the order that the returned positions should be in. Can be: difficulty, random, near or far (from the trigger position).
     **/
    protected String sorting = "difficulty";


    /**
     * Loads this Spawn Condition from the provided JSON data.
     **/
    public static SpawnLocation createFromJSON(JsonObject json) {
        if (json == null || !json.has("type") || json.get("type").isJsonNull() || !json.get("type").isJsonPrimitive()) {
            return null;
        }
        String type = json.get("type").getAsString();
        SpawnLocation spawnLocation = null;

        if ("base".equalsIgnoreCase(type)) {
            spawnLocation = new SpawnLocation();
        } else if ("random".equalsIgnoreCase(type)) {
            spawnLocation = new RandomSpawnLocation();
        } else if ("block".equalsIgnoreCase(type)) {
            spawnLocation = new BlockSpawnLocation();
        } else if ("material".equalsIgnoreCase(type)) {
            spawnLocation = new MaterialSpawnLocation();
        } else if ("structure".equalsIgnoreCase(type)) {
            spawnLocation = new StructureSpawnLocation();
        }

        if (spawnLocation == null) {
            return null;
        }
        spawnLocation.loadFromJSON(json);
        return spawnLocation;
    }


    /**
     * Loads this Spawn Location from the provided JSON data.
     **/
    public void loadFromJSON(JsonObject json) {
        this.rangeMin = JSONHelper.getVector3i(json, "rangeMin");

        this.rangeMax = JSONHelper.getVector3i(json, "rangeMax");

        if (json.has("yMin"))
            this.yMin = json.get("yMin").getAsInt();

        if (json.has("yMax"))
            this.yMax = json.get("yMax").getAsInt();

        if (json.has("sorting"))
            this.sorting = json.get("sorting").getAsString();
    }


    /**
     * Returns a list of positions to spawn at.
     **/
    public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
        List<BlockPos> spawnPositions = new ArrayList<>();

        int yOffset = this.getOffset(world.getRandom(), this.rangeMin.getY(), this.rangeMax.getY());
        int finalY = triggerPos.getY() + yOffset;

        if ((this.yMax < 0 || finalY <= this.yMax) && (this.yMin < 0 || finalY >= this.yMin)) {
            Vec3i offset = new Vec3i(
                    this.getOffset(world.getRandom(), this.rangeMin.getX(), this.rangeMax.getX()),
                    yOffset,
                    this.getOffset(world.getRandom(), this.rangeMin.getZ(), this.rangeMax.getZ())
            );
            spawnPositions.add(triggerPos.offset(offset));
        }

        return this.sortSpawnPositions(spawnPositions, world, triggerPos);
    }


    /**
     * Returns a random offset from the provided min and max values.
     **/
    public int getOffset(RandomSource random, int min, int max) {
        if (max <= min) {
            return 0;
        }
        int offset = min + random.nextInt(max - min);
        if (random.nextBoolean()) {
            offset = -offset;
        }
        return offset;
    }


    /**
     * Sorts a list of spawning positions.
     **/
    public List<BlockPos> sortSpawnPositions(List<BlockPos> spawnPositions, Level world, BlockPos triggerPos) {
        String sorting = this.sorting;

        if ("difficulty".equalsIgnoreCase(this.sorting)) {
            sorting = "random";
            if (world.getDifficulty().getId() <= 1) {
                sorting = "far";
            } else if (world.getDifficulty().getId() >= 3) {
                sorting = "near";
            }
        }

        if ("random".equalsIgnoreCase(sorting)) {
            Collections.shuffle(spawnPositions);
        } else if ("near".equalsIgnoreCase(sorting)) {
            Collections.sort(spawnPositions, new CoordSorterNearest(triggerPos));
        } else if ("far".equalsIgnoreCase(sorting)) {
            Collections.sort(spawnPositions, new CoordSorterFurthest(triggerPos));
        }
        return spawnPositions;
    }

    /**
     * Returns true when the X/Z column is already available for safe spawn probing.
     * Spawn locations run from tick/worldgen-adjacent paths, so they must not force
     * synchronous chunk work while sampling candidates near a loaded boundary.
     */
    protected boolean isColumnLoaded(Level world, int x, int z) {
        return world != null && world.getChunkSource().hasChunk(x >> 4, z >> 4);
    }

    protected boolean isBlockLoaded(Level world, BlockPos blockPos) {
        return blockPos != null && this.isColumnLoaded(world, blockPos.getX(), blockPos.getZ());
    }

    /**
     * Height of the column WITHOUT forcing the chunk to load; Integer.MIN_VALUE when it isn't
     * loaded. Level.getHeight()/getHeightmapPos() will generate the chunk, which turns a cheap
     * spawn probe into a chunk-load cascade.
     */
    protected int getLoadedHeight(Level world, Heightmap.Types heightmapType, int x, int z) {
        LevelChunk chunk = this.getLoadedChunk(world, x, z);
        if (chunk == null) {
            return Integer.MIN_VALUE;
        }
        return chunk.getHeight(heightmapType, x & 15, z & 15) + 1;
    }

    protected LevelChunk getLoadedChunk(Level world, int x, int z) {
        if (world == null) {
            return null;
        }
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (world instanceof ServerLevel serverLevel) {
            return serverLevel.getChunkSource().getChunkNow(chunkX, chunkZ);
        }
        return world.getChunkSource().hasChunk(chunkX, chunkZ) ? world.getChunk(chunkX, chunkZ) : null;
    }
}
