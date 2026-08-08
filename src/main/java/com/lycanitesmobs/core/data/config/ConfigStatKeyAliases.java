package com.lycanitesmobs.core.data.config;

import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Backwards compatibility for the rangedSpeed stat key, which used to be spelled ranged_speed.
 *
 * A straight rename would silently drop the tuned value for any existing config, so the old key
 * is still defined (as a deprecated alias) when it is actually present in the toml, and it only
 * takes effect while the canonical entry is untouched at its default.
 */
public final class ConfigStatKeyAliases {
    public static final String RANGED_SPEED = "rangedSpeed";
    public static final String BROKEN_RANGED_SPEED = "ranged_speed";

    private static final boolean RANGED_SPEED_ALIAS_NEEDED = detectRangedSpeedAliasConfig();

    private ConfigStatKeyAliases() {
    }

    public static boolean isRangedSpeed(String statName) {
        return RANGED_SPEED.equals(statName);
    }

    public static boolean shouldDefineRangedSpeedAlias() {
        return RANGED_SPEED_ALIAS_NEEDED;
    }

    /**
     * The alias only wins when the canonical entry is still at its default and the alias is not,
     * so an explicitly tuned new-style value is never overridden by a stale old-style one.
     */
    public static double resolveCanonicalOrAlias(ModConfigSpec.ConfigValue<Double> canonicalValue,
                                                 ModConfigSpec.ConfigValue<Double> aliasValue) {
        double canonical = canonicalValue.get();
        if (aliasValue == null || Double.compare(canonical, canonicalValue.getDefault()) != 0) {
            return canonical;
        }
        double alias = aliasValue.get();
        return Double.compare(alias, aliasValue.getDefault()) != 0 ? alias : canonical;
    }

    private static boolean detectRangedSpeedAliasConfig() {
        Path commonConfig = FMLPaths.CONFIGDIR.get().resolve("lycanitesmobs-common.toml");
        if (!Files.isRegularFile(commonConfig)) {
            return false;
        }
        try {
            for (String line : Files.readAllLines(commonConfig)) {
                String trimmed = line.trim();
                if (trimmed.startsWith(BROKEN_RANGED_SPEED)
                        && trimmed.substring(BROKEN_RANGED_SPEED.length()).trim().startsWith("=")) {
                    return true;
                }
            }
        }
        catch (IOException ignored) {
            return false;
        }
        return false;
    }
}
