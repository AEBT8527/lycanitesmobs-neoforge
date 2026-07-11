package com.lycanitesmobs.core.worldgen.dungeon.instance;

import com.lycanitesmobs.core.worldgen.dungeon.definition.SectorLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Shared ordered block sequence for direct sector placement and planned builds.
 */
final class SectorBuildSequence {
    private SectorBuildSequence() {
    }

    static void generate(SectorInstance sector, PlacementSink sink, RandomSource random) {
        clearArea(sector, sink);
        buildFloor(sector, sink, random, 0);
        buildWalls(sector, sink, random);
        buildCeiling(sector, sink, random);
        if (sector.isSectorType("stairs")) {
            buildStairs(sector, sink, random);
            buildFloor(sector, sink, random, -(sector.getRoomSize().y() * 2));
        }
        if (sector.isSectorType("tower")) {
            buildStairs(sector, sink, random);
        }
    }

    private static void clearArea(SectorInstance sector, PlacementSink sink) {
        SectorBounds area = sink.clip(sector.getClearArea());
        if (!area.hasHorizontalArea()) {
            return;
        }

        for (int x = area.minX(); x <= area.maxX(); x++) {
            for (int y = area.minY(); y <= area.maxY(); y++) {
                if (!sink.canBuildAtY(y)) {
                    continue;
                }
                for (int z = area.minZ(); z <= area.maxZ(); z++) {
                    sink.place(new SectorBuildStep(new BlockPos(x, y, z), Blocks.CAVE_AIR.defaultBlockState(), Direction.SOUTH));
                }
            }
        }
    }

    private static void buildFloor(SectorInstance sector, PlacementSink sink, RandomSource random, int offsetY) {
        SectorBounds worldArea = sector.getRoomArea(offsetY);
        SectorBounds area = sink.clip(worldArea);
        if (!area.hasHorizontalArea()) {
            return;
        }

        for (int layerIndex : sector.getFloorLayers().keySet()) {
            int y = worldArea.minY() + layerIndex;
            if (!sink.canBuildAtY(y)) {
                continue;
            }
            SectorLayer layer = sector.getFloorLayers().get(layerIndex);
            for (int x = area.minX(); x <= area.maxX(); x++) {
                List<Character> row = layer.getRow(x - worldArea.minX(), worldArea.widthX());
                for (int z = area.minZ(); z <= area.maxZ(); z++) {
                    char buildChar = layer.getColumn(x - worldArea.minX(), worldArea.widthX(), z - worldArea.minZ(),
                            worldArea.widthZ(), row);
                    BlockState blockState = sector.getFloorBlock(buildChar, random);
                    if (blockState.getBlock() != Blocks.CAVE_AIR) {
                        sink.place(new SectorBuildStep(new BlockPos(x, y, z), blockState, Direction.UP));
                    }
                }
            }
        }
    }

    private static void buildWalls(SectorInstance sector, PlacementSink sink, RandomSource random) {
        SectorBounds worldArea = sector.getWallBuildArea();
        SectorBounds area = sink.clip(worldArea);
        if (!area.hasHorizontalArea()) {
            return;
        }

        for (int layerIndex : sector.getWallLayers().keySet()) {
            SectorLayer layer = sector.getWallLayers().get(layerIndex);
            for (int y = worldArea.minY(); y <= worldArea.maxY(); y++) {
                if (!sink.canBuildAtY(y)) {
                    continue;
                }

                int progressY = y - worldArea.minY();
                int fullY = worldArea.maxY() - worldArea.minY();
                List<Character> row = layer.getRow(progressY, fullY);

                int frontZ = worldArea.minZ() + layerIndex;
                int backZ = worldArea.maxZ() - layerIndex;
                for (int x = area.minX(); x <= area.maxX(); x++) {
                    char buildChar = layer.getColumn(progressY, fullY, x - worldArea.minX(), worldArea.widthX(), row);
                    BlockState blockState = sector.getWallBlock(buildChar, random);
                    if (blockState.getBlock() != Blocks.CAVE_AIR) {
                        sink.place(new SectorBuildStep(new BlockPos(x, y, frontZ), blockState, Direction.SOUTH));
                        sink.place(new SectorBuildStep(new BlockPos(x, y, backZ), blockState, Direction.NORTH));
                    }
                }

                int leftX = worldArea.minX() + layerIndex;
                int rightX = worldArea.maxX() - layerIndex;
                for (int z = area.minZ(); z <= area.maxZ(); z++) {
                    char buildChar = layer.getColumn(progressY, fullY, z - worldArea.minZ(), worldArea.widthZ(), row);
                    BlockState blockState = sector.getWallBlock(buildChar, random);
                    if (blockState.getBlock() != Blocks.CAVE_AIR) {
                        sink.place(new SectorBuildStep(new BlockPos(leftX, y, z), blockState, Direction.EAST));
                        sink.place(new SectorBuildStep(new BlockPos(rightX, y, z), blockState, Direction.WEST));
                    }
                }
            }
        }
    }

    private static void buildCeiling(SectorInstance sector, PlacementSink sink, RandomSource random) {
        SectorBounds worldArea = sector.getRoomArea();
        SectorBounds area = sink.clip(worldArea);
        if (!area.hasHorizontalArea()) {
            return;
        }

        for (int layerIndex : sector.getCeilingLayers().keySet()) {
            int y = worldArea.maxY() + layerIndex;
            if (!sink.canBuildAtY(y)) {
                continue;
            }
            SectorLayer layer = sector.getCeilingLayers().get(layerIndex);
            for (int x = area.minX(); x <= area.maxX(); x++) {
                List<Character> row = layer.getRow(x - worldArea.minX(), worldArea.widthX());
                for (int z = area.minZ(); z <= area.maxZ(); z++) {
                    char buildChar = layer.getColumn(x - worldArea.minX(), worldArea.widthX(), z - worldArea.minZ(),
                            worldArea.widthZ(), row);
                    BlockState blockState = sector.getCeilingBlock(buildChar, random);
                    if (blockState.getBlock() != Blocks.CAVE_AIR) {
                        sink.place(new SectorBuildStep(new BlockPos(x, y, z), blockState, Direction.DOWN));
                    }
                }
            }
        }
    }

    private static void buildStairs(SectorInstance sector, PlacementSink sink, RandomSource random) {
        SectorBounds worldArea = sector.getStairArea();
        SectorBounds area = sink.clip(worldArea);
        if (!area.hasHorizontalArea()) {
            return;
        }

        for (int y = worldArea.maxY(); y >= worldArea.minY(); y--) {
            if (!sink.canBuildAtY(y)) {
                continue;
            }
            for (int x = area.minX(); x <= area.maxX(); x++) {
                for (int z = area.minZ(); z <= area.maxZ(); z++) {
                    BlockState blockState = sector.getStairBuildState(worldArea, x, y, z, random);
                    if (blockState.getBlock() != Blocks.CAVE_AIR || sink.includesEmptyStairSpace()) {
                        sink.place(new SectorBuildStep(new BlockPos(x, y, z), blockState, Direction.UP));
                    }
                }
            }
        }
    }

    interface PlacementSink {
        SectorBounds clip(SectorBounds area);

        int maxBuildHeight();

        void place(SectorBuildStep step);

        default boolean canBuildAtY(int y) {
            return y > 0 && y < this.maxBuildHeight();
        }

        default boolean includesEmptyStairSpace() {
            return false;
        }
    }
}

record SectorBuildStep(BlockPos pos, BlockState state, Direction facing) {
}
