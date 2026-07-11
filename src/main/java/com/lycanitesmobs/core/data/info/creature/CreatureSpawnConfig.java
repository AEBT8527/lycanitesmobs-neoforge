package com.lycanitesmobs.core.data.info.creature;

import com.lycanitesmobs.core.data.config.ConfigCreatureSpawning;
import net.minecraft.world.level.Level;

public class CreatureSpawnConfig {
	protected int typeSpawnLimit = 64;
	protected double spawnLimitRange = 16D;
	protected boolean disableAllSpawning = false;
	protected boolean disableDungeonSpawners = false;
	protected boolean enforceBlockCost = true;
	protected boolean useSurfaceLightLevel = true;
	protected double spawnWeightScale = 1.0D;
	protected double dungeonSpawnerWeightScale = 1.0D;
	protected boolean ignoreWorldGenSpawning = false;
	protected boolean controlVanillaSpawns = false;

	/** A global list of dimension ids that overrides every other spawn setting in both the configs and json spawners. **/
	protected String[] dimensionList;

	/** If set to true the dimension list acts as a whitelist, otherwise it is a blacklist. **/
	protected boolean dimensionListWhitelist = false;
	

	public int typeSpawnLimit() {
		return this.typeSpawnLimit;
	}

	public double spawnLimitRange() {
		return this.spawnLimitRange;
	}

	public boolean disableAllSpawning() {
		return this.disableAllSpawning;
	}

	public boolean disableDungeonSpawners() {
		return this.disableDungeonSpawners;
	}

	public boolean enforceBlockCost() {
		return this.enforceBlockCost;
	}

	public boolean useSurfaceLightLevel() {
		return this.useSurfaceLightLevel;
	}

	public double spawnWeightScale() {
		return this.spawnWeightScale;
	}

	public double dungeonSpawnerWeightScale() {
		return this.dungeonSpawnerWeightScale;
	}

	public boolean ignoreWorldGenSpawning() {
		return this.ignoreWorldGenSpawning;
	}

	public boolean controlVanillaSpawns() {
		return this.controlVanillaSpawns;
	}

	public String[] dimensionList() {
		return this.dimensionList;
	}

	public boolean dimensionListWhitelist() {
		return this.dimensionListWhitelist;
	}

	/**
	 * Loads global spawning settings from the configs.
	 */
	public void loadConfig() {
		this.typeSpawnLimit = ConfigCreatureSpawning.INSTANCE.typeSpawnLimit.get();
		this.spawnLimitRange = ConfigCreatureSpawning.INSTANCE.spawnLimitRange.get();
		this.disableAllSpawning = ConfigCreatureSpawning.INSTANCE.disableAllSpawning.get();
		this.enforceBlockCost = ConfigCreatureSpawning.INSTANCE.enforceBlockCost.get();
		this.spawnWeightScale = ConfigCreatureSpawning.INSTANCE.spawnWeightScale.get();
		this.useSurfaceLightLevel = ConfigCreatureSpawning.INSTANCE.useSurfaceLightLevel.get();
		this.ignoreWorldGenSpawning = ConfigCreatureSpawning.INSTANCE.ignoreWorldGenSpawning.get();
		this.controlVanillaSpawns = ConfigCreatureSpawning.INSTANCE.controlVanillaSpawns.get();

		// Master Dimension List:
		this.dimensionList = ConfigCreatureSpawning.INSTANCE.globalDimensionList.get().replace(" ", "").split(",");
		this.dimensionListWhitelist = ConfigCreatureSpawning.INSTANCE.globalDimensionWhitelist.get();

		this.disableDungeonSpawners = ConfigCreatureSpawning.INSTANCE.disableDungeonSpawners.get();
		this.dungeonSpawnerWeightScale = ConfigCreatureSpawning.INSTANCE.dungeonSpawnerWeightScale.get();
	}

	public boolean isAllowedGlobal(Level world) {
		if(this.disableAllSpawning) {
			return false;
		}

		if(this.dimensionList.length > 0) {
			boolean inDimensionList = false;
			for (String dimensionId : this.dimensionList) {
				if (dimensionId.equals(world.dimension().location().toString())) {
					inDimensionList = true;
					break;
				}
			}
			if (inDimensionList && !this.dimensionListWhitelist) {
				return false;
			}
			if (!inDimensionList && this.dimensionListWhitelist) {
				return false;
			}
		}

		return true;
	}
}
