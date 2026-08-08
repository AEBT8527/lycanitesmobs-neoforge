package com.lycanitesmobs.core.command;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.config.ConfigDebug;
import com.lycanitesmobs.core.entity.spawner.MobSpawn;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import com.lycanitesmobs.core.entity.spawner.SpawnerMobRegistry;
import com.lycanitesmobs.core.entity.spawner.SpawnerTriggerDispatcher;
import com.lycanitesmobs.core.manager.SpawnerManager;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import com.lycanitesmobs.core.data.info.BiomeClimateType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DebugCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("debug")
                .then(Commands.literal("log").then(Commands.argument("category", StringArgumentType.string()).executes(DebugCommand::log)))
                .then(Commands.literal("list").executes(DebugCommand::list))
                .then(Commands.literal("biomesfromtag").then(Commands.argument("biometag", StringArgumentType.greedyString()).executes(DebugCommand::biomesfromtag)))
                .then(Commands.literal("listbiometags").executes(DebugCommand::listbiometags).then(Commands.argument("biome", StringArgumentType.greedyString()).executes(DebugCommand::listbiometagsforbiome)))
                .then(Commands.literal("spawners").executes(DebugCommand::spawners))
                .then(Commands.literal("spawntest").executes(DebugCommand::spawntest))
                .then(Commands.literal("overlay").executes(DebugCommand::overlay));
    }

    public static int log(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        String category = StringArgumentType.getString(context, "category").toLowerCase();
        List<String> enabledLogs = new ArrayList<>();
        enabledLogs.addAll(ConfigDebug.INSTANCE.enabled.get());

        if (enabledLogs.contains(category)) {
            enabledLogs.remove(category);
            LMHelperClass.logInfoMessage(category + " debug logging disabled.");
        } else {
            enabledLogs.add(category);
            LMHelperClass.logInfoMessage(category + " debug logging enabled.");
        }
        ConfigDebug.INSTANCE.enabled.set(enabledLogs);
        ConfigDebug.INSTANCE.enabled.save();

        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.debug.log").append(" " + category), true);
        return 0;
    }

    public static int list(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.debug.list"), true);
        String[] debugCategories = new String[]{"jsonspawner", "mobspawns", "entity", "subspecies", "creature", "mobevents", "dungeon", "items", "equipment"};
        for (String debugCategory : debugCategories) {
            context.getSource().sendSuccess(() -> Component.literal(debugCategory), true);
        }
        return 0;
    }

    public static int biomesfromtag(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        // Resolves the tag exactly the way the spawn conditions do, so an empty result here is an
        // empty result there. Forge's BiomeDictionary climates are gone; spawn json uses tag ids now.
        String biomeTag = StringArgumentType.getString(context, "biometag").toLowerCase();
        List<String> biomes = JSONHelper.getBiomesFromTags(context.getSource().getLevel(), List.of(biomeTag));
        context.getSource().sendSuccess(() -> Component.literal(biomeTag + ": " + biomes.size() + " biome(s)"), false);
        for (String biome : biomes.subList(0, Math.min(biomes.size(), 24))) {
            context.getSource().sendSuccess(() -> Component.literal("  " + biome), false);
        }
        if (biomes.size() > 24) {
            context.getSource().sendSuccess(() -> Component.literal("  ... and " + (biomes.size() - 24) + " more"), false);
        }
        return 0;
    }

    public static int listbiometags(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        for (BiomeClimateType biomeType : BiomeClimateType.values()) {
            context.getSource().sendSuccess(() -> Component.literal(biomeType.name()), true);
        }
        return 0;
    }

    public static int listbiometagsforbiome(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        String biomeId = StringArgumentType.getString(context, "biome");
        ResourceLocation biomeResourceLocation = ResourceLocation.parse(biomeId);
        var biomeHolder = context.getSource().registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getHolder(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, biomeResourceLocation))
                .orElse(null);
        if (biomeHolder == null) {
            context.getSource().sendSuccess(() -> Component.literal("Cannot find a biome with that id."), true);
            return 0;
        }
        // Forge's BiomeDictionary types became plain registry tags; these are the ids that spawn
        // json biomeTags entries have to match.
        List<String> tags = biomeHolder.tags().map(tag -> tag.location().toString()).sorted().toList();
        context.getSource().sendSuccess(() -> Component.literal("Tags for " + biomeId + ": " + tags.size()), false);
        for (String tag : tags) {
            context.getSource().sendSuccess(() -> Component.literal("  " + tag), false);
        }
        return 0;
    }

    /**
     * Dumps the state of the JSON spawner runtime: which spawners loaded, how many mobs each has
     * registered to it, and whether the trigger dispatcher is actually being ticked.
     */
    public static int spawners(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        for (String line : SpawnerTriggerDispatcher.getInstance().getDispatchSummary()) {
            context.getSource().sendSuccess(() -> Component.literal(line), false);
        }

        int enabled = 0;
        int withMobs = 0;
        StringBuilder empties = new StringBuilder();
        for (Spawner spawner : SpawnerManager.getInstance().getSpawners()) {
            Collection<MobSpawn> globalSpawns = SpawnerMobRegistry.getMobSpawns(spawner.getSharedName());
            int mobCount = (globalSpawns != null ? globalSpawns.size() : 0) + spawner.getMobSpawns().size();
            if (spawner.isDefinitionEnabled()) {
                enabled++;
            }
            if (mobCount > 0) {
                withMobs++;
            }
            else if (!spawner.hasEventName()) {
                empties.append(empties.length() == 0 ? "" : ", ").append(spawner.getName());
            }
        }
        final int totalSpawners = SpawnerManager.getInstance().getSpawners().size();
        final int enabledCount = enabled;
        final int withMobsCount = withMobs;
        context.getSource().sendSuccess(() -> Component.literal(
                "spawners: " + totalSpawners + " loaded, " + enabledCount + " enabled, " + withMobsCount + " with mobs"), false);
        if (empties.length() > 0) {
            context.getSource().sendSuccess(() -> Component.literal("no mobs registered: " + empties), false);
        }
        return 0;
    }

    /**
     * Fires every world spawner once at the caller's position, bypassing tick rate and chance.
     * Enable the jsonspawner debug channel first to see why each one did or did not spawn.
     */
    public static int spawntest(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        Level level = context.getSource().getLevel();
        BlockPos pos = BlockPos.containing(context.getSource().getPosition());
        context.getSource().sendSuccess(() -> Component.literal("spawn test at " + pos + " in " + level.dimension().location()), false);
        for (String line : SpawnerTriggerDispatcher.getInstance().debugTriggerWorldSpawners(level, pos)) {
            context.getSource().sendSuccess(() -> Component.literal("  " + line), false);
        }
        return 0;
    }

    public static int overlay(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(2)) {
            return 0;
        }
        ConfigDebug.INSTANCE.creatureOverlay.set(!ConfigDebug.INSTANCE.creatureOverlay.get());
        ConfigDebug.INSTANCE.creatureOverlay.save();
        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.debug.overlay"), true);
        return 0;
    }
}
