package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.data.config.ConfigMobEvent;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.manager.MobEventManager;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MobEventsCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("mobevents")
				.then(Commands.literal("reload").executes(MobEventsCommand::reload))
				.then(Commands.literal("enable").executes(MobEventsCommand::enable))
				.then(Commands.literal("disable").executes(MobEventsCommand::disable))
				.then(Commands.literal("creative")
						.then(Commands.literal("enable").executes(MobEventsCommand::creativeEnable))
						.then(Commands.literal("disable").executes(MobEventsCommand::creativeDisable)))
				.then(Commands.literal("list").executes(MobEventsCommand::list));
	}

	public static int reload(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		MobEventManager.getInstance().reload();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevents.reload"), true);
		return 0;
	}

	public static int enable(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		MobEventManager.getInstance().setMobEventsEnabled(true);
		ConfigMobEvent.INSTANCE.mobEventsEnabled.set(true);
		ConfigMobEvent.INSTANCE.mobEventsEnabled.save();
		MobEventManager.getInstance().setRandomMobEventsEnabled(true);
		ConfigMobEvent.INSTANCE.mobEventsRandom.set(true);
		ConfigMobEvent.INSTANCE.mobEventsRandom.save();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevents.enable"), true);
		return 0;
	}

	public static int disable(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		MobEventManager.getInstance().setRandomMobEventsEnabled(false);
		ConfigMobEvent.INSTANCE.mobEventsRandom.set(false);
		ConfigMobEvent.INSTANCE.mobEventsRandom.save();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevents.disable"), true);
		return 0;
	}

	public static int creativeEnable(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		MobEventPlayerServer.setTestOnCreative(true);
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevents.creative.enable"), true);
		return 0;
	}

	public static int creativeDisable(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		MobEventPlayerServer.setTestOnCreative(false);
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevents.creative.disable"), true);
		return 0;
	}

	public static int list(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
			return 0;
		}
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.mobevents.list"), true);
		for(MobEvent mobEvent : MobEventManager.getInstance().getMobEvents()) {
			String eventName = mobEvent.getName() + " (" + mobEvent.getTitle().getString() + ")";
			context.getSource().sendSuccess(() -> Component.literal(eventName), true);
		}
		return 0;
	}
}
