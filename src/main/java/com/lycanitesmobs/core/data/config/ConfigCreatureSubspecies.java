package com.lycanitesmobs.core.data.config;

import com.lycanitesmobs.core.entity.util.CreatureStats;
import com.lycanitesmobs.core.data.info.Variant;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

public class ConfigCreatureSubspecies {
	public static ConfigCreatureSubspecies INSTANCE;

	public final ModConfigSpec.ConfigValue<Integer> baseWeight;
	public Map<String,ModConfigSpec.ConfigValue<Integer>> commonWeights = new HashMap<>();
	public Map<String, Map<String, ModConfigSpec.ConfigValue<Double>>> variantMultipliers = new HashMap<>();
	public Map<String, ModConfigSpec.ConfigValue<Double>> legacyRangedSpeedVariantMultipliers = new HashMap<>();

	public final ModConfigSpec.ConfigValue<Integer> uncommonDropScale;
	public final ModConfigSpec.ConfigValue<Integer> rareDropScale;

	public final ModConfigSpec.ConfigValue<Double> uncommonExperienceScale;
	public final ModConfigSpec.ConfigValue<Double> rareExperienceScale;

	public final ModConfigSpec.ConfigValue<Integer> uncommonSpawnDayMin;
	public final ModConfigSpec.ConfigValue<Integer> rareSpawnDayMin;

	public final ModConfigSpec.ConfigValue<Boolean> rareDespawning;

	public final ModConfigSpec.ConfigValue<Boolean> rareHealthBars;

	public ConfigCreatureSubspecies(ModConfigSpec.Builder builder) {
		builder.push("Creature Variants");
		builder.comment("Global creature variant settings.");

		baseWeight = builder.comment("The minimum base starting level of every mob. Cannot be less than 1.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.baseWeight")
				.define("baseWeight", 400);

		for(String variantName : Variant.getSubspeciesNames()) {
			commonWeights.put(variantName, builder
					.comment("Subspecies weight for " + variantName + ".")
					.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.weights." + variantName)
					.define("weights." + variantName, Variant.getCommonWeight(variantName)));
		}

		for(String variantName : Variant.getSubspeciesNames()) {
			Map<String, ModConfigSpec.ConfigValue<Double>> statMultipliers = new HashMap<>();
			for (String statName : CreatureStats.STAT_NAMES) {
				// RLCraft 2.10 tuning: uncommon = x1.25 stats (health x2, speed x1.5),
				// rare = x1.5 stats (health x20, speed x2).
				double defaultValue = 1.0;
				if("uncommon".equals(variantName)) {
					defaultValue = 1.25;
					if("health".equals(statName)) {
						defaultValue = 2;
					}
					else if("speed".equals(statName)) {
						defaultValue = 1.5;
					}
				}
				if("rare".equals(variantName)) {
					defaultValue = 1.5;
					if("health".equals(statName)) {
						defaultValue = 20;
					}
					else if("speed".equals(statName)) {
						defaultValue = 2;
					}
				}

				statMultipliers.put(statName, builder
						.comment("Stat multiplier for " + statName + " for " + variantName + " variant.")
						.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.multipliers." + variantName + "." + statName)
						.define("multipliers." + variantName + "." + statName, defaultValue));
			}
			if (ConfigStatKeyAliases.shouldDefineRangedSpeedAlias()) {
				this.legacyRangedSpeedVariantMultipliers.put(variantName, builder
						.comment("Deprecated migration alias for the rangedSpeed multiplier for " + variantName + " variant.")
						.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.multipliers." + variantName + "." + ConfigStatKeyAliases.BROKEN_RANGED_SPEED)
						.define("multipliers." + variantName + "." + ConfigStatKeyAliases.BROKEN_RANGED_SPEED, 1.0D));
			}
			this.variantMultipliers.put(variantName, statMultipliers);
		}

		uncommonDropScale = builder.comment("When a creature with the uncommon variant (Azure, Verdant, etc) dies, its item drops amount is multiplied by this value.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.uncommonDropScale")
				.define("uncommonDropScale", 2);
		rareDropScale = builder.comment("When a creature with the rare variant (Celestial, Lunar, etc) dies, its item drops amount is multiplied by this value.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareDropScale")
				.define("rareDropScale", 5);

		uncommonExperienceScale = builder.comment("When a creature with the uncommon variant (Azure, Verdant, etc) dies, its experience amount is multiplied by this value.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.uncommonExperienceScale")
				.define("uncommonExperienceScale", 2.0D);
		rareExperienceScale = builder.comment("When a creature with the rare variant (Celestial, Lunar, etc) dies, its experience amount is multiplied by this value.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareExperienceScale")
				.define("rareExperienceScale", 10.0D);

		uncommonSpawnDayMin = builder.comment("The minimum amount of days before uncommon variants start to spawn.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.uncommonSpawnDayMin")
				.define("uncommonSpawnDayMin", 0);
		rareSpawnDayMin = builder.comment("The minimum amount of days before rare variants start to spawn.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareSpawnDayMin")
				.define("rareSpawnDayMin", 0);

		rareDespawning = builder.comment("If set to true, rare variants will despawn naturally over time.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareDespawning")
				.define("rareDespawning", true);

		rareHealthBars = builder.comment("If set to true, rare variants such as the Lunar Grue or Celestial Geonach will display boss health bars.")
				.translation(CoreConfig.CONFIG_PREFIX + "creature.variant.rareHealthBars")
				.define("rareHealthBars", true);

		builder.pop();
	}

	public ModConfigSpec.ConfigValue<Double> getLegacyRangedSpeedVariantMultiplier(String variantName, String statName) {
		if (!ConfigStatKeyAliases.isRangedSpeed(statName)) {
			return null;
		}
		return this.legacyRangedSpeedVariantMultipliers.get(variantName);
	}
}
