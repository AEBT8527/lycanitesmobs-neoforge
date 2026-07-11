package com.lycanitesmobs.core.data.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CoreConfig {
	public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static String CONFIG_PREFIX = "lycanitesmobs.config.";
	public static ModConfigSpec SPEC;

	public static void buildSpec() {
		ConfigGeneral.INSTANCE = new ConfigGeneral(BUILDER);
		ConfigPlayer.INSTANCE = new ConfigPlayer(BUILDER);
		ConfigExtra.INSTANCE = new ConfigExtra(BUILDER);
		ConfigDebug.INSTANCE = new ConfigDebug(BUILDER);
		ConfigAdmin.INSTANCE = new ConfigAdmin(BUILDER);
		ConfigClient.INSTANCE = new ConfigClient(BUILDER);
		ConfigDungeons.INSTANCE = new ConfigDungeons(BUILDER);
		ConfigCreatures.INSTANCE = new ConfigCreatures(BUILDER);
		ConfigCreatureSpawning.INSTANCE = new ConfigCreatureSpawning(BUILDER);
		ConfigCreatureSubspecies.INSTANCE = new ConfigCreatureSubspecies(BUILDER);
		ConfigItem.INSTANCE = new ConfigItem(BUILDER);
		ConfigMobEvent.INSTANCE = new ConfigMobEvent(BUILDER);
		SPEC = BUILDER.build();
	}
}
