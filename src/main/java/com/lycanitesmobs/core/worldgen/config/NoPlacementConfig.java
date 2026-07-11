package com.lycanitesmobs.core.worldgen.config;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class NoPlacementConfig extends PlacementModifier {
    public static final NoPlacementConfig INSTANCE = new NoPlacementConfig();
    public static final MapCodec<NoPlacementConfig> CODEC = MapCodec.unit(() -> {
        return INSTANCE;
    });
    PlacementModifierType<NoPlacementConfig> NO_PLACEMENT_CONFIG = () -> CODEC;

    public NoPlacementConfig() {
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
        return Stream.of(new BlockPos(blockPos));
    }

    @Override
    public PlacementModifierType<?> type() {
        return NO_PLACEMENT_CONFIG;
    }
}