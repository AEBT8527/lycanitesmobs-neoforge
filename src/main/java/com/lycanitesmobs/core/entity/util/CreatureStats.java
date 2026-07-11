package com.lycanitesmobs.core.entity.util;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.Variant;
import com.lycanitesmobs.core.data.info.creature.ExtraMobBehaviour;
import net.minecraft.world.Difficulty;

import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

/** Manages the stats of an EntityCreature. This applies difficulty multipliers, subspecies, levels, etc also. **/
public class CreatureStats {
	/** The base amount of experience needed to level up, this is increased by the creature's level scaled. **/
	public static final int BASE_LEVELUP_EXPERIENCE = 100;

	/** A static array of all stat names used by configs, etc. **/
	public static final List<String> STAT_NAMES = List.of("health", "defense", "armor", "speed", "damage", "attackSpeed", "ranged_speed", "effect", "amplifier", "pierce", "sight");

	/** The entity using these stats. **/
	public BaseCreatureEntity entity;


	/**
	 * Constructor
	 * @param entity The entity instance that will use this stats instance.
	 */
	public CreatureStats(BaseCreatureEntity entity) {
		this.entity = entity;
	}


	/**
	 * Returns the maximum health the entity should have.
	 * @return Maximum base health.
	 */
	public double getHealth() {
		String statName = "health";
		double statValue = this.entity.getCreatureInfo().getHealth();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
			statValue = this.applyExtraBehaviour(statValue, ExtraMobBehaviour::multiplierHealth, ExtraMobBehaviour::boostHealth);
		}

		statValue *= this.getLevelMultiplier(statName);
		return Math.max(0, statValue);
	}


	/**
	 * Returns the defense this entity should use. Damage taken is reduced by defense (to a minimum cap of 1 damage).
	 * @return Base defense.
	 */
	public double getDefense() {
		String statName = "defense";
		double statValue = this.entity.getCreatureInfo().getDefense();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
			statValue = this.applyExtraBehaviour(statValue, ExtraMobBehaviour::multiplierDefense, ExtraMobBehaviour::boostDefense);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the armor this entity should use. This armor value is applied through the vanilla system and can be pierced by piercing effects unlike defense.
	 * @return Base armor.
	 */
	public double getArmor() {
		String statName = "armor";
		double statValue = this.entity.getCreatureInfo().getArmor();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
			statValue = this.applyExtraBehaviour(statValue, ExtraMobBehaviour::multiplierArmor, ExtraMobBehaviour::boostArmor);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the speed this entity should use. Speed affects how fast an entity can travel.
	 * @return Base speed.
	 */
	public double getSpeed() {
		String statName = "speed";
		double statValue = this.entity.getCreatureInfo().getMovementSpeed();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
			statValue = this.applyExtraBehaviour(statValue, ExtraMobBehaviour::multiplierSpeed, ExtraMobBehaviour::boostSpeed);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the damage this entity should use. This is how much melee damage this creature applies to entities.
	 * @return Base damage.
	 */
	public double getDamage() {
		String statName = "damage";
		double statValue = this.entity.getCreatureInfo().getDamage();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
			statValue = this.applyExtraBehaviour(statValue, ExtraMobBehaviour::multiplierDamage, ExtraMobBehaviour::boostDamage);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the melee attack speed this entity should use. This affects how quickly this entity melee attacks, the default is 1 for once per second.
	 * @return Attack speed.
	 */
	public double getAttackSpeed() {
		String statName = "attackSpeed";
		double statValue = this.entity.getCreatureInfo().getAttackSpeed();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
			statValue = this.applyExtraBehaviour(statValue, ExtraMobBehaviour::multiplierHaste, ExtraMobBehaviour::boostHaste);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the ranged attack speed this entity should use. This affects how quickly this entity fires projectiles, the default is 0.5 for once every 2 seconds.
	 * @return Attack speed.
	 */
	public double getRangedSpeed() {
		String statName = "ranged_speed";
		double statValue = this.entity.getCreatureInfo().getRangedSpeed();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
			statValue = this.applyExtraBehaviour(statValue, ExtraMobBehaviour::multiplierHaste, ExtraMobBehaviour::boostHaste);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the effect this entity should use. This affects how long any debuffs applied by this entity's element last for where 1 is default and 1 second for element effects.
	 * @return Base effect duration.
	 */
	public double getEffect() {
		String statName = "effect";
		double statValue = this.entity.getCreatureInfo().getEffectDuration();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
			statValue = this.applyExtraBehaviour(statValue, ExtraMobBehaviour::multiplierEffect, ExtraMobBehaviour::boostEffect);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the effect amplifier this entity should use. This affects the amplifier of the effects applied by this creatures element. If less than 0, no effect is applied.
	 * @return Base effect amplifier.
	 */
	public double getAmplifier() {
		String statName = "amplifier";
		double statValue = this.entity.getCreatureInfo().getEffectAmplifier();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the pierce this entity should use. Pierce affects how much damage dealt by this entity will ignore the targets armor.
	 * @return Base pierce.
	 */
	public double getPierce() {
		String statName = "pierce";
		double statValue = this.entity.getCreatureInfo().getPierce();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
			statValue = this.applyExtraBehaviour(statValue, ExtraMobBehaviour::multiplierPierce, ExtraMobBehaviour::boostPierce);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the sight this entity should use. How far this entity can see other entities. Aka Follow Range.
	 * @return Base sight.
	 */
	public double getSight() {
		String statName = "sight";
		double statValue = this.entity.getCreatureInfo().getSight();

		// Wild:
		if(!this.entity.isTamed()) {
			statValue *= this.getDifficultyMultiplier(statName);
			statValue *= this.getVariantMultiplier(statName);
		}

		statValue *= this.getLevelMultiplier(statName);
		return statValue;
	}


	/**
	 * Returns the knockback resistance this entity should use. The chance from 0.0-1.0 of an entity being knocked back when hit.
	 * @return Base knockback resistance.
	 */
	public double getKnockbackResistance() {
		return this.entity.getCreatureInfo().getKnockbackResistance();
	}
	/**
	 * Returns the Bag Size this entity should use.
	 * @return Base Bag Size.
	 */


	private double applyExtraBehaviour(double statValue, ToDoubleFunction<ExtraMobBehaviour> multiplier, ToDoubleFunction<ExtraMobBehaviour> boost) {
		ExtraMobBehaviour extraBehaviour = this.entity.getExtraMobBehaviour();
		if (extraBehaviour == null) {
			return statValue;
		}
		return (statValue * multiplier.applyAsDouble(extraBehaviour)) + boost.applyAsDouble(extraBehaviour);
	}

	/**
	 * Returns a difficulty stat multiplier for the provided stat name and the world that the entity is in.
	 * @param stat The name of the stat to get the multiplier for.
	 * @return The stat multiplier.
	 */
	protected double getDifficultyMultiplier(String stat) {
		Difficulty difficulty = this.entity.getCommandSenderWorld().getDifficulty();
		String difficultyName = "Easy";
		if(difficulty.getId() >= 3)
			difficultyName = "Hard";
		else if(difficulty == Difficulty.NORMAL)
			difficultyName = "Normal";
		return CreatureManager.getInstance().getDifficultyMultiplier(difficultyName.toUpperCase(Locale.ENGLISH), stat.toUpperCase(Locale.ENGLISH));
	}


	/**
	 * Returns a variant stat multiplier for the provided stat name and the variant that the entity is.
	 * @param stat The name of the stat to get the multiplier for.
	 * @return The stat multiplier.
	 */
	protected double getVariantMultiplier(String stat) {
		if(this.entity.getVariant() != null) {
			return Variant.getStatMultiplier(this.entity.getVariant().getRarity(), stat);
		}
		return 1;
	}


	/**
	 * Returns a level stat multiplier for the provided stat name and the creature's current level.
	 * @param stat The name of the stat to get the multiplier for.
	 * @return The stat multiplier.
	 */
	protected double getLevelMultiplier(String stat) {
		double statLevel = Math.max(0, this.entity.getMobLevel() - 1);
		return 1 + (statLevel * CreatureManager.getInstance().getLevelMultiplier(stat.toUpperCase(Locale.ENGLISH)));
	}

	/**
	 * Determines how much experience the creature needs in order to level up.
	 * @return Experience required for a level up.
	 */
	public int getExperienceForNextLevel() {
		return BASE_LEVELUP_EXPERIENCE + Math.round(BASE_LEVELUP_EXPERIENCE * (this.entity.getMobLevel() - 1) * 0.25F);
	}
}
