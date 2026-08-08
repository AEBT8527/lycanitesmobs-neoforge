package com.lycanitesmobs.core.worldgen.structure;

import com.lycanitesmobs.core.entity.spawner.condition.WorldSpawnCondition;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonLayout;
import com.lycanitesmobs.core.worldgen.dungeon.instance.SectorInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class LMDungeonPiece extends StructurePiece {
    private boolean placementChecked;
    private boolean placementAllowed;


    private String schematicName;
    private DungeonLayout layout;
    private long layoutSeed;
    private BlockPos originPos;

    public LMDungeonPiece(BoundingBox boundingBox, String schematicName, DungeonLayout layout) {
        super(ModStructurePieceTypes.LM_DUNGEON_PIECE, 0, boundingBox);
        this.schematicName = schematicName;
        this.layout = layout;
        this.originPos = layout.getDungeonInstance().getOrigin();
        this.layoutSeed = layout.getDungeonInstance().getSeed();
    }

    public LMDungeonPiece(BoundingBox boundingBox, String schematicName, BlockPos originPos, long layoutSeed) {
        super(ModStructurePieceTypes.LM_DUNGEON_PIECE, 0, boundingBox);
        this.schematicName = schematicName;
        this.originPos = originPos;
        this.layoutSeed = layoutSeed;
    }

    public LMDungeonPiece(CompoundTag tag) {
        super(ModStructurePieceTypes.LM_DUNGEON_PIECE, tag);
        this.schematicName = tag.getStringOr("SchematicName", "");
        this.layoutSeed = tag.getLongOr("LayoutSeed", 0L);
        int[] origin = tag.getIntArray("Origin").orElse(new int[0]);
        this.originPos = new BlockPos(origin[0], origin[1], origin[2]);
        if (!this.hasDimensionFilter()) {
            this.layout = this.regenerateLayout();
        }
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("SchematicName", this.schematicName);
        tag.putLong("LayoutSeed", this.layoutSeed);
        tag.putIntArray("Origin", new int[] {
                this.originPos.getX(),
                this.originPos.getY(),
                this.originPos.getZ()
        });
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager,
            ChunkGenerator chunkGenerator, RandomSource random,
            BoundingBox chunkBoundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        if (!this.canPlaceInWorld(worldGenLevel)) {
            return;
        }

        if (this.layout == null) {
            this.layout = this.regenerateLayout();
        }

        if (this.layout == null) {
            LMHelperClass.logWarning("Dungeon",
                    "postProcess: layout is null for " + this.schematicName + ", skipping chunk " + chunkPos);
            return;
        }

        if (!this.layout.hasSectorsInChunk(chunkPos)) {
            return;
        }

        this.layout.getDungeonInstance().setWorldIfMissing(worldGenLevel.getLevel());

        try {
            for (SectorInstance sector : this.layout.getSectorsInChunk(chunkPos)) {
                sector.build(worldGenLevel, worldGenLevel.getLevel(), chunkPos, random);
            }

            LMHelperClass.logDebug("Dungeon",
                    "postProcess: built " + this.layout.getSectorsInChunk(chunkPos).size() +
                            " sectors in chunk " + chunkPos + " for " + this.schematicName);
        } catch (Exception e) {
            LMHelperClass.logErrorMessageOnceCatchable(
                    "postProcess failed for " + this.schematicName + " chunk " + chunkPos + ": ", e);
        }
    }

    private DungeonLayout regenerateLayout() {
        DungeonSchematic schematic = DungeonManager.getInstance().getSchematic(this.schematicName);
        if (schematic == null) {
            LMHelperClass.logWarning("Dungeon",
                    "Cannot regenerate layout: schematic '" + this.schematicName + "' not found");
            return null;
        }

        DungeonInstance tempInstance = new DungeonInstance();
        tempInstance.setSchematic(schematic);
        tempInstance.setOrigin(this.originPos);
        tempInstance.setSeed(this.layoutSeed);

        RandomSource layoutRandom = RandomSource.create(this.layoutSeed);
        DungeonLayout newLayout = new DungeonLayout(tempInstance);
        newLayout.generate(layoutRandom);

        if (!newLayout.hasSectors()) {
            LMHelperClass.logWarning("Dungeon",
                    "Regenerated layout has no sectors for " + this.schematicName);
            return null;
        }

        LMHelperClass.logDebug("Dungeon",
                "Regenerated layout from seed for " + this.schematicName +
                        " with " + newLayout.getSectors().size() + " sectors");
        return newLayout;
    }

    private synchronized boolean canPlaceInWorld(WorldGenLevel worldGenLevel) {
        // Asked once per chunk of the structure; the answer cannot change mid-generation.
        if (this.placementChecked) {
            return this.placementAllowed;
        }
        this.placementAllowed = this.computePlaceInWorld(worldGenLevel);
        this.placementChecked = true;
        return this.placementAllowed;
    }

    private boolean computePlaceInWorld(WorldGenLevel worldGenLevel) {
        DungeonSchematic schematic = DungeonManager.getInstance().getSchematic(this.schematicName);
        if (schematic == null || !schematic.isEnabled()) {
            return false;
        }

        WorldSpawnCondition condition = schematic.getWorldSpawnCondition();
        if (condition == null) {
            return true;
        }

        String dimensionId = worldGenLevel.getLevel().dimension().identifier().toString();
        if (condition.isAllowedDimensionId(dimensionId)) {
            return true;
        }

        LMHelperClass.logDebug("Dungeon",
                "postProcess: skipping " + this.schematicName + " in disallowed dimension " + dimensionId);
        return false;
    }

    private boolean hasDimensionFilter() {
        DungeonSchematic schematic = DungeonManager.getInstance().getSchematic(this.schematicName);
        return schematic != null
                && schematic.getWorldSpawnCondition() != null
                && schematic.getWorldSpawnCondition().hasDimensionFilter();
    }

}
