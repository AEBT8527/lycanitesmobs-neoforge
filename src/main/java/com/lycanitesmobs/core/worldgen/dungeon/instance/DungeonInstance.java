package com.lycanitesmobs.core.worldgen.dungeon.instance;

import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DungeonInstance {
    /** A Dungeon Instance is a dungeon that is active in the world. **/

    /**
     * A unique identifier for this Dungeon Instance.
     **/
    protected UUID uuid;

    /**
     * The Schematic this instance builds from. Can be null when a Schematic has been removed or renamed in which case the dungeon is immediately set as complete.
     **/
    protected DungeonSchematic schematic;

    /**
     * If true, this dungeon has been fully built and does not need to generate its layout, etc. This is where all chunks this dungeon is in have been loaded.
     **/
    protected boolean complete = false;

    /**
     * Stores how many chunks have been built. When this matches the number of chunks that are used by this dungeon, this dungeon is marked as complete.
     **/
    protected int chunksBuilt = 0;

    /**
     * The origin block position of this dungeon where it begins building from.
     **/
    protected BlockPos originPos;

    /**
     * The minimum xz chunk that sectors that this layout is in.
     **/
    protected ChunkPos chunkMin;

    /**
     * The maximum xz chunk that sectors that this layout is in.
     **/
    protected ChunkPos chunkMax;

    /**
     * The world that the dungeon should build in.
     **/
    protected Level world;

    /**
     * The seed for generating this dungeon. All random decisions are based on this seed so that the Dungeon Layout can generate the same on world reload, etc.
     **/
    protected long seed = 0;

    /**
     * The random instance to use when randomly generating, this can be seeded for consistent results.
     **/
    protected RandomSource random;

    /**
     * The generated layout of this dungeon, this contains all randomly selected sectors and their structures, etc. This is null on complete dungeons.
     **/
    protected DungeonLayout layout;

    protected DungeonBuildPlan buildPlan;

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public DungeonSchematic getSchematic() {
        return this.schematic;
    }

    public void setSchematic(DungeonSchematic schematic) {
        this.schematic = schematic;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public int getChunksBuilt() {
        return this.chunksBuilt;
    }

    public ChunkPos getChunkMin() {
        return this.chunkMin;
    }

    public ChunkPos getChunkMax() {
        return this.chunkMax;
    }

    public boolean hasChunkBounds() {
        return this.chunkMin != null && this.chunkMax != null;
    }

    public Level getWorld() {
        return this.world;
    }

    protected void setWorld(Level world) {
        this.world = world;
    }

    public void setWorldIfMissing(Level world) {
        if (this.world == null) {
            this.world = world;
        }
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public RandomSource getRandom() {
        return this.random;
    }

    protected void resetRandom() {
        this.random = RandomSource.create(this.seed);
    }

    public DungeonLayout getLayout() {
        return this.layout;
    }

    public boolean hasLayout() {
        return this.layout != null;
    }

    public List<SectorInstance> getLayoutSectors() {
        if (this.layout == null) {
            return Collections.emptyList();
        }
        return this.layout.getSectors();
    }

    public boolean hasLayoutSectorsInChunk(ChunkPos chunkPos) {
        return this.layout != null && this.layout.hasSectorsInChunk(chunkPos);
    }

    public List<SectorInstance> getLayoutSectorsInChunk(ChunkPos chunkPos) {
        if (this.layout == null) {
            return Collections.emptyList();
        }
        return this.layout.getSectorsInChunk(chunkPos);
    }

    public int getLayoutChunkCount() {
        if (this.layout == null) {
            return 0;
        }
        return this.layout.getChunkCount();
    }

    public SectorInstance getFirstLayoutSector() {
        if (this.layout == null) {
            return null;
        }
        return this.layout.getFirstSector();
    }

    protected void setLayout(DungeonLayout layout) {
        this.layout = layout;
    }

    public boolean hasBuildPlan() {
        return this.buildPlan != null;
    }

    public boolean hasReadyBuildPlan() {
        return this.buildPlan != null && this.buildPlan.isReady();
    }

    public List<DungeonBuildPlan.PlannedBlock> getPlannedBlocks(ChunkPos chunkPos) {
        if (this.buildPlan == null) {
            return Collections.emptyList();
        }
        return this.buildPlan.getForChunk(chunkPos);
    }

    public int getPlannedChunkCount() {
        if (this.buildPlan == null) {
            return 0;
        }
        return this.buildPlan.getPlannedChunkCount();
    }

    protected DungeonBuildPlan createBuildPlan() {
        this.buildPlan = new DungeonBuildPlan();
        return this.buildPlan;
    }

    protected DungeonChunkBuildProgress markChunkBuilt(int expectedChunks) {
        this.chunksBuilt++;
        if (this.chunksBuilt >= expectedChunks) {
            this.complete = true;
        }
        return new DungeonChunkBuildProgress(this.chunksBuilt, expectedChunks, this.complete);
    }

    protected void expandChunkBounds(SectorInstance sectorInstance) {
        ChunkPos minChunkPos = new ChunkPos(sectorInstance.getOccupiedBoundsMin());
        if (this.chunkMin == null) {
            this.chunkMin = minChunkPos;
        } else {
            if (minChunkPos.x < this.chunkMin.x) {
                this.chunkMin = new ChunkPos(minChunkPos.x, this.chunkMin.z);
            }
            if (minChunkPos.z < this.chunkMin.z) {
                this.chunkMin = new ChunkPos(this.chunkMin.x, minChunkPos.z);
            }
        }

        ChunkPos maxChunkPos = new ChunkPos(sectorInstance.getOccupiedBoundsMax());
        if (this.chunkMax == null) {
            this.chunkMax = maxChunkPos;
        } else {
            if (maxChunkPos.x > this.chunkMax.x) {
                this.chunkMax = new ChunkPos(maxChunkPos.x, this.chunkMax.z);
            }
            if (maxChunkPos.z > this.chunkMax.z) {
                this.chunkMax = new ChunkPos(this.chunkMax.x, maxChunkPos.z);
            }
        }
    }

    /**
     * Sets the origin position. This must be set before init. Reading from NBT sets this from the NBT data.
     *
     * @param blockPos The exact block position that this dungeon builds from.
     */
    public void setOrigin(BlockPos blockPos) {
        this.originPos = blockPos;
        if (this.chunkMin == null) {
            this.chunkMin = new ChunkPos(blockPos);
        }
        if (this.chunkMax == null) {
            this.chunkMax = new ChunkPos(blockPos);
        }
    }


    /**
     * Initialises this Dungeon where if it's not complete it will generate its layout, etc. Should be called after readFromNBT when loading an existing dungeon and origin must be set.
     *
     * @param world The world that this Instance will build in.
     * @return True on success and false if unable to initialize.
     */
    public boolean init(Level world) {
        this.world = world;
        if (this.complete) {
            return true;
        }
        if (this.world == null || this.originPos == null) {
            LMHelperClass.logWarningMessage("Tried to initialise a dungeon with a missing world or origin. " + this);
            return false;
        }

        if (!this.selectSchematic(world)) {
            return false;
        }

        this.prepareRandom(world);

        LMHelperClass.logDebug("Dungeon", "Starting Dungeon Instance Generation For " + this);
        this.generateLayout();
        this.prepareBuildPlan();

        LMHelperClass.logInfo("Dungeon", "Generated New Dungeon Instance " + this);
        this.markWorldDirty(world);

        return true;
    }

    private boolean selectSchematic(Level world) {
        if (this.schematic != null) {
            return true;
        }

        List<DungeonSchematic> schematics = new ArrayList<>();
        for (DungeonSchematic schematic : DungeonManager.getInstance().getSchematics()) {
            if (schematic.canBuild(world, this.originPos)) {
                schematics.add(schematic);
            }
        }
        if (schematics.isEmpty()) {
            LMHelperClass.logDebug("Dungeon", "No valid dungeon schematics found for origin position: " + this.originPos);
            return false;
        }
        if (schematics.size() == 1) {
            this.schematic = schematics.get(0);
        } else {
            this.schematic = schematics.get(this.world.random.nextInt(schematics.size()));
        }
        return true;
    }

    private void prepareRandom(Level world) {
        if (this.seed == 0) {
            this.seed = world.random.nextLong();
        }
        this.resetRandom();
    }

    private void generateLayout() {
        if (this.layout != null) {
            return;
        }
        this.layout = new DungeonLayout(this);
        this.layout.generate(this.random);
    }

    private void prepareBuildPlan() {
        DungeonBuildPlan buildPlan = this.createBuildPlan();
        CompletableFuture.runAsync(() -> {
            for (SectorInstance sector : this.getLayoutSectors()) {
                sector.generateBuildPlan(buildPlan);
            }
            buildPlan.markReady();
        });
    }

    private void markWorldDirty(Level world) {
        ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
        if (extendedWorld != null) {
            extendedWorld.setDirty();
        }
    }


    /**
     * Returns true if the chunk position is within this dungeon's area.
     *
     * @param chunkPos The chunk position to check.
     * @param padding  Increases the dungeon area, useful for finding chunks within range of this layout as well.
     * @return True if the chunk is within this dungeon's area.
     */
    public boolean isChunkPosWithin(ChunkPos chunkPos, int padding) {
        if (chunkPos.x < this.chunkMin.x - padding || chunkPos.x > this.chunkMax.x + padding) {
            return false;
        }

        if (chunkPos.z < this.chunkMin.z - padding || chunkPos.z > this.chunkMax.z + padding) {
            return false;
        }

        return true;
    }


    /**
     * Builds blocks from every sector within the provided chunk position.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for placement during WorldGen.
     * @param chunkPos    The chunk position to build within.
     */
    public void buildChunk(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        DungeonChunkBuildContext context = new DungeonChunkBuildContext(worldWriter, world, chunkPos, random);
        if (this.complete || this.layout == null) {
            return;
        }

        if (this.hasReadyBuildPlan()) {
            this.buildPlannedChunk(context);
            return;
        }

        if (this.buildPlan == null) {
            LMHelperClass.logDebug("Dungeon", "buildChunk: buildPlan is null for dungeon " + this.uuid + " chunk " + context.chunkPos());
        } else {
            LMHelperClass.logDebug("Dungeon", "buildChunk: buildPlan not ready yet for dungeon " + this.uuid + " chunk " + context.chunkPos());
        }

        if (!this.hasLayoutSectorsInChunk(context.chunkPos())) {
            LMHelperClass.logDebug("Dungeon", "buildChunk: sectorChunkMap has no entry for chunk " + context.chunkPos() + " for dungeon " + this.uuid);
            return;
        }

        LMHelperClass.logDebug(
                "Dungeon",
                "buildChunk: using sector build for chunk " + context.chunkPos() +
                        " with " + this.getLayoutSectorsInChunk(context.chunkPos()).size() +
                        " sectors for dungeon " + this.uuid
        );

        for (SectorInstance sectorInstance : this.getLayoutSectorsInChunk(context.chunkPos())) {
            context.buildSector(sectorInstance);
        }

        DungeonChunkBuildProgress progress = this.markChunkBuilt(this.getLayoutChunkCount());
        if (progress.complete()) {
            LMHelperClass.logDebug(
                    "Dungeon",
                    "buildChunk: dungeon " + this.uuid + " marked complete using sectorChunkMap. chunksBuilt=" +
                            progress.chunksBuilt() + " sectorChunks=" + progress.expectedChunks()
            );
        }
    }


    public void debugBuildChunk(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        DungeonChunkBuildContext context = new DungeonChunkBuildContext(worldWriter, world, chunkPos, random);
        LMHelperClass.logDebug(
                "DungeonDebug",
                "debugBuildChunk: instance=" + this.uuid +
                        " complete=" + this.complete +
                        " layout=" + (this.layout != null ? "present" : "null") +
                        " chunk=" + context.chunkPos()
        );

        if (this.complete) {
            LMHelperClass.logDebug("DungeonDebug", "debugBuildChunk: instance complete, skipping " + context.chunkPos());
            return;
        }

        if (this.layout == null) {
            LMHelperClass.logDebug("DungeonDebug", "debugBuildChunk: layout null for instance " + this.uuid);
            return;
        }

        if (this.hasReadyBuildPlan()) {
            int blockCount = this.getPlannedBlocks(context.chunkPos()).size();
            LMHelperClass.logDebug(
                    "DungeonDebug",
                    "debugBuildChunk: buildPlan ready for " + context.chunkPos() +
                            " plannedBlockCount=" + blockCount
            );
            return;
        }

        if (!this.hasLayoutSectorsInChunk(context.chunkPos())) {
            LMHelperClass.logDebug("DungeonDebug", "debugBuildChunk: chunk " + context.chunkPos() + " not in sectorChunkMap");
            return;
        }

        int sectors = this.getLayoutSectorsInChunk(context.chunkPos()).size();
        LMHelperClass.logDebug(
                "DungeonDebug",
                "debugBuildChunk: chunk " + context.chunkPos() +
                        " in sectorChunkMap with sectorCount=" + sectors
        );
    }

    private void buildPlannedChunk(DungeonChunkBuildContext context) {
        List<DungeonBuildPlan.PlannedBlock> blocks = this.getPlannedBlocks(context.chunkPos());

        if (blocks.isEmpty()) {
            LMHelperClass.logDebug("Dungeon", "buildChunk: no planned blocks for chunk " + context.chunkPos() + " in dungeon " + this.uuid);
        } else {
            int placed = 0;
            for (DungeonBuildPlan.PlannedBlock block : blocks) {
                if (placed < 10) {
                    LMHelperClass.logDebug(
                            "Dungeon",
                            "buildChunk: placing planned block " +
                                    block.state().getBlock() + " at " + block.pos() +
                                    " in chunk " + context.chunkPos() + " for dungeon " + this.uuid
                    );
                }
                SectorInstance anySector = this.getFirstLayoutSector();
                if (anySector != null) {
                    context.applyPlannedBlock(block, anySector);
                    placed++;
                }
            }
            LMHelperClass.logDebug(
                    "Dungeon",
                    "buildChunk: placed " + placed + " planned blocks in chunk " + context.chunkPos() + " for dungeon " + this.uuid
            );
        }

        DungeonChunkBuildProgress progress = this.markChunkBuilt(this.getPlannedChunkCount());
        if (progress.complete()) {
            LMHelperClass.logDebug(
                    "Dungeon",
                    "buildChunk: dungeon " + this.uuid + " marked complete using buildPlan. chunksBuilt=" +
                            progress.chunksBuilt() + " plannedChunks=" + progress.expectedChunks()
            );
        }
    }

    /**
     * Loads this Dungeon Instance from the provided NBT data, this is mostly just if the dungeon is built and what its origin position is.
     *
     * @param nbtTagCompound The NBT Data to read from.
     */
    public void readFromNBT(CompoundTag nbtTagCompound) {
        this.uuid = nbtTagCompound.getUUID("Id");
        this.schematic = DungeonManager.getInstance().getSchematic(nbtTagCompound.getString("Schematic"));
        this.seed = nbtTagCompound.getLong("Seed");
        this.complete = nbtTagCompound.getBoolean("Complete");
        this.chunksBuilt = nbtTagCompound.getInt("ChunksBuilt");
        if (this.schematic == null) {
            this.complete = true;
        }
        int[] originPos = nbtTagCompound.getIntArray("OriginPos");
        this.originPos = new BlockPos(originPos[0], originPos[1], originPos[2]);
        int[] chunkMin = nbtTagCompound.getIntArray("ChunkMin");
        this.chunkMin = new ChunkPos(chunkMin[0], chunkMin[1]);
        int[] chunkMax = nbtTagCompound.getIntArray("ChunkMax");
        this.chunkMax = new ChunkPos(chunkMax[0], chunkMax[1]);

        LMHelperClass.logDebug("Dungeon", "Loaded Dungeon Instance from NBT: " + this);
    }

    /**
     * Writes this dungeon to NBT. Should only be called after the this instance has been initialised.
     *
     * @param nbtTagCompound The NBTData to write to.
     * @return The written to NBTData. Null if this Dungeon Instance cannot be saved.
     */
    public CompoundTag writeToNBT(CompoundTag nbtTagCompound) {
        if (this.uuid == null || this.schematic == null)
            return null;

        nbtTagCompound.putUUID("Id", this.uuid);
        nbtTagCompound.putString("Schematic", this.schematic.getName());
        nbtTagCompound.putLong("Seed", this.seed);
        nbtTagCompound.putBoolean("Complete", this.complete);
        nbtTagCompound.putInt("ChunksBuilt", this.chunksBuilt);
        nbtTagCompound.putIntArray("OriginPos", new int[]{this.originPos.getX(), this.originPos.getY(), this.originPos.getZ()});
        nbtTagCompound.putIntArray("ChunkMin", new int[]{this.chunkMin.x, this.chunkMin.z});
        nbtTagCompound.putIntArray("ChunkMax", new int[]{this.chunkMax.x, this.chunkMax.z});

        LMHelperClass.logDebug("Dungeon", "Saved Dungeon Instance to NBT: " + this);
        return nbtTagCompound;
    }


    /**
     * Returns a descriptive string of this Dungeon Instance.
     *
     * @return A formatted string.
     */
    @Override
    public String toString() {
        String schematic = "";
        if (this.schematic != null)
            schematic = " - Schematic: " + this.schematic.getName();
        String tpCommand = "/tp " + this.originPos.getX() + " " + (this.originPos.getY() + 2) + " " + this.originPos.getZ() + " ";
        return "Dungeon Instance" + schematic + " - TP Command: " + tpCommand + " - Origin: " + this.originPos + " - Complete: " + complete + " - Seed: " + this.seed + " - ID: " + this.uuid;
    }

    public BlockPos getOrigin() {
        return originPos;
    }

    protected static boolean DEBUG_PLACEMENT = true;
    protected static boolean DISABLE_ACTUAL_PLACEMENT = false;

    public static boolean isActualPlacementDisabled() {
        return DISABLE_ACTUAL_PLACEMENT;
    }
}
