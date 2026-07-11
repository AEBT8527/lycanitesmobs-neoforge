package com.lycanitesmobs.core.command;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.config.ConfigDebug;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import com.lycanitesmobs.core.data.info.BiomeClimateType;

import java.util.ArrayList;
import java.util.List;

public class DebugCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("debug")
                .then(Commands.literal("log").then(Commands.argument("category", StringArgumentType.string()).executes(DebugCommand::log)))
                .then(Commands.literal("list").executes(DebugCommand::list))
                .then(Commands.literal("biomesfromtag").then(Commands.argument("biometag", StringArgumentType.string()).executes(DebugCommand::biomesfromtag)))
                .then(Commands.literal("listbiometags").executes(DebugCommand::listbiometags).then(Commands.argument("biome", StringArgumentType.string()).executes(DebugCommand::listbiometagsforbiome)))
                .then(Commands.literal("overlay").executes(DebugCommand::overlay));
    }

    public static int log(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
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
        if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
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
        if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            return 0;
        }
        String biomeTag = StringArgumentType.getString(context, "biometag").toLowerCase();
        BiomeClimateType biomeType = null;
        try {
            biomeType = BiomeClimateType.valueOf(biomeTag);
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("Unknown biome tag: " + biomeTag + ".");
        }
        if (biomeType == null) {
            return 0;
        }
        // NeoForge removed BiomeManager's per-climate biome index; per-climate biome listing is unavailable.
        context.getSource().sendSuccess(() -> Component.literal("Per-climate biome listing is not available on NeoForge."), true);
        return 0;
    }

    public static int listbiometags(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            return 0;
        }
        for (BiomeClimateType biomeType : BiomeClimateType.values()) {
            context.getSource().sendSuccess(() -> Component.literal(biomeType.name()), true);
        }
        return 0;
    }

    public static int listbiometagsforbiome(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            return 0;
        }
        String biomeId = StringArgumentType.getString(context, "biome");
        Identifier biomeResourceLocation = Identifier.parse(biomeId);
        Biome biome = context.getSource().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).get(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, biomeResourceLocation)).map(net.minecraft.core.Holder::value).orElse(null);
        if (biome == null) {
            context.getSource().sendSuccess(() -> Component.literal("Cannot find a biome with that id."), true);
            return 0;
        }
        // NeoForge removed BiomeManager's per-climate biome index; this lookup is unavailable.
        context.getSource().sendSuccess(() -> Component.literal("Tags for: " + biomeId), true);
        return 0;
    }

    public static int overlay(final CommandContext<CommandSourceStack> context) {
        if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            return 0;
        }
        ConfigDebug.INSTANCE.creatureOverlay.set(!ConfigDebug.INSTANCE.creatureOverlay.get());
        ConfigDebug.INSTANCE.creatureOverlay.save();
        context.getSource().sendSuccess(() -> Component.translatable("lyc.command.debug.overlay"), true);
        return 0;
    }
}
