package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.entity.spawner.Spawner;
import com.lycanitesmobs.core.event.SpawnerEventListener;
import com.lycanitesmobs.core.manager.SpawnerManager;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SpawnersCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("spawners")
				.then(Commands.literal("reload").executes(SpawnersCommand::reload))
				.then(Commands.literal("creative")
						.then(Commands.literal("enable").executes(SpawnersCommand::creativeEnable))
						.then(Commands.literal("disable").executes(SpawnersCommand::creativeDisable)))
				.then(Commands.literal("list").executes(SpawnersCommand::list));
	}

	public static int reload(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		SpawnerManager.getInstance().reload();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.spawners.reload"), true);
		return 0;
	}

	public static int creativeEnable(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		SpawnerEventListener.setTestOnCreative(true);
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.spawners.creative.enable"), true);
		return 0;
	}

	public static int creativeDisable(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		SpawnerEventListener.setTestOnCreative(false);
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.spawners.creative.disable"), true);
		return 0;
	}

	public static int list(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.spawners.list"), true);
		for(Spawner spawner : SpawnerManager.getInstance().getSpawners()) {
			if(spawner.hasEventName()) {
				continue;
			}
			String spawnerName = spawner.getName();
			context.getSource().sendSuccess(() -> Component.literal(spawnerName), true);
		}
		return 0;
	}
}
