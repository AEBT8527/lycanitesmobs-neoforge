package com.lycanitesmobs.core.data.config;

import com.google.common.collect.Lists;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ConfigAdmin {
	public static ConfigAdmin INSTANCE;

	public final ModConfigSpec.ConfigValue<List<? extends String>> forceRemoveEntityIds;

	public ConfigAdmin(ModConfigSpec.Builder builder) {
		builder.push("Admin");
		builder.comment("Special tools for server admins.");

		this.forceRemoveEntityIds = builder
				.comment("Here you can add a list of entity IDs for entity that you want to be forcefully removed.")
				.translation(CoreConfig.CONFIG_PREFIX + "admin.removeEntityIds")
				.defineList("removeEntityIds", Lists.newArrayList(), o -> o instanceof String);

		builder.pop();
	}
}
