package com.lycanitesmobs.core.data.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigPlayer {
    public static ConfigPlayer INSTANCE;

    public final ModConfigSpec.ConfigValue<Integer> summoningFocusRecharge;

    public ConfigPlayer(ModConfigSpec.Builder builder) {
        builder.push("Player");
        builder.comment("Player stat and ability settings.");

        this.summoningFocusRecharge = builder
                .comment("How much summoning focus a player regains per tick. Default is 5, was 1 in earlier RLCraft-style configs.")
                .translation(CoreConfig.CONFIG_PREFIX + "player.summoningfocus.recharge")
                .defineInRange("Summoning Focus Recharge", 5, 0, Integer.MAX_VALUE);

        builder.pop();
    }
}
