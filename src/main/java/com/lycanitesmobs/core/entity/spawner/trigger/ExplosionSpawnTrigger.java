package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class ExplosionSpawnTrigger extends EntitySpawnTrigger {

	/** The minimum strength or size of the explosion. Currently not working as there isn';'t a way to get explosion size from an explosion event. **/
	protected int strength = 4;

	/** If true, only players can activate this Trigger, fake players are not counted. Players that prime TNT count. **/
	protected boolean playerOnly = false;


	/** Constructor **/
	public ExplosionSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("strength"))
			this.strength = json.get("strength").getAsInt();

		if(json.has("playerOnly"))
			this.playerOnly = json.get("playerOnly").getAsBoolean();

		super.loadFromJSON(json);
	}


	/** Called every time an explosion is created. **/
	public boolean onExplosion(Level world, Player player, Explosion explosion) {
		// TODO Figure out strength/size workaround.

		// Player Only:
		if(this.playerOnly && player == null) {
			return false;
		}

		// Chance:
		if(this.chance < 1 && world.random.nextDouble() > this.chance) {
			return false;
		}

		// Entity Check:
		if(!this.playerOnly && explosion.getDirectSourceEntity() != null && explosion.getDirectSourceEntity() instanceof LivingEntity exploder) {
			if(!this.isMatchingEntity(exploder)) {
				return false;
			}
		}

		return this.trigger(world, player, new BlockPos(LMHelperClass.convertToVec3i(explosion.center())), 0, 0);
	}
}
