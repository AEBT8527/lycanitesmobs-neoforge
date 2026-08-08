package com.lycanitesmobs.core.manager;

import net.minecraft.core.registries.BuiltInRegistries;
import com.google.common.collect.ImmutableSet;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.liquid.BaseLiquidBlock;
import com.lycanitesmobs.core.manager.FluidManager;
import com.lycanitesmobs.core.worldgen.feature.ChunkSpawnFeature;
import com.lycanitesmobs.core.worldgen.feature.DungeonFeature;
import com.lycanitesmobs.core.worldgen.feature.DungeonFeatureConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class WorldGenManager {
    private static final WorldGenManager INSTANCE = new WorldGenManager();

    public static WorldGenManager getInstance() {
        return INSTANCE;
    }

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, LycanitesMobs.MODID);

    public static final DeferredHolder<Feature<?>, ChunkSpawnFeature> CHUNK_SPAWN_FEATURE =
            FEATURES.register("chunkspawn", () -> new ChunkSpawnFeature(NoneFeatureConfiguration.CODEC));

/*
    public static final DeferredHolder<DungeonFeature, ? extends DungeonFeature> DUNGEON_FEATURE =
            FEATURES.register("dungeon", () -> new DungeonFeature(NoneFeatureConfiguration.CODEC));
*/

    private final Map<String, Holder<PlacedFeature>> fluidPlacedFeatures = new HashMap<>();

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    public void setupFluidFeatures() {
        FluidManager.getInstance().forEachWorldgenFluidBlock((fluidName, fluidBlock) -> {
            BlockState state = fluidBlock.defaultBlockState();

            LakeFeature.Configuration lakeConfig = new LakeFeature.Configuration(
                    BlockStateProvider.simple(state),
                    BlockStateProvider.simple(Blocks.STONE.defaultBlockState())
            );
            var list = new ArrayList<Holder<Block>>();
            list.add(Holder.direct(Blocks.STONE));
            list.add(Holder.direct(Blocks.GRANITE));
            list.add(Holder.direct(Blocks.DIORITE));
            list.add(Holder.direct(Blocks.ANDESITE));
            // Springs also generate in the deepslate layer now; the data JSONs list these too,
            // but this code path builds its own configuration and would otherwise ignore them.
            list.add(Holder.direct(Blocks.DEEPSLATE));
            list.add(Holder.direct(Blocks.TUFF));
            SpringConfiguration springConfig = new SpringConfiguration(
                    state.getFluidState(),
                    true,
                    4,
                    1,
                    HolderSet.direct(list)
            );

            List<PlacementModifier> lakePlacement;
            List<PlacementModifier> springPlacement;

            boolean isWaterLike = state.getFluidState().isSource();

            if (isWaterLike) {
                lakePlacement = List.of(
                        RarityFilter.onAverageOnceEvery(40),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(8),
                                VerticalAnchor.absolute(256)
                        ),
                        BiomeFilter.biome()
                );
                springPlacement = List.of(
                        InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(8),
                                VerticalAnchor.absolute(256)
                        ),
                        BiomeFilter.biome()
                );
            } else {
                lakePlacement = List.of(
                        RarityFilter.onAverageOnceEvery(40),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(8),
                                VerticalAnchor.absolute(256)
                        ),
                        BiomeFilter.biome()
                );
                springPlacement = List.of(
                        InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(8),
                                VerticalAnchor.absolute(256)
                        ),
                        BiomeFilter.biome()
                );
            }

            Holder<PlacedFeature> lakePlaced = PlacementUtils.inlinePlaced(
                    Feature.LAKE,
                    lakeConfig,
                    lakePlacement.toArray(new PlacementModifier[0])
            );

            Holder<PlacedFeature> springPlaced = PlacementUtils.inlinePlaced(
                    Feature.SPRING,
                    springConfig,
                    springPlacement.toArray(new PlacementModifier[0])
            );

            addFluidPlacedFeature(fluidName + "_lake", lakePlaced);
            addFluidPlacedFeature(fluidName + "_spring", springPlaced);
        });
    }

    private void addFluidPlacedFeature(String name, Holder<PlacedFeature> placedFeature) {
        this.fluidPlacedFeatures.put(name, placedFeature);
    }

    public void forEachFluidPlacedFeature(BiConsumer<String, Holder<PlacedFeature>> action) {
        this.fluidPlacedFeatures.forEach(action);
    }
}
