package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import net.minecraft.world.level.Level;

/** Called when a Mob Event is active. **/
public class MobEventSpawnTrigger extends SpawnTrigger {

	/** If set, this is the name of the mob event that must be active for this trigger. **/
	protected String eventName = "";

	/** The current time percentage of the active Mob Event. This is a fraction of the Mob Event's duration rounded down. So at 1200 ticks (1 minute) 0.5 would be 30 seconds in. **/
	protected double eventTime = 0;

	/** Constructor **/
	public MobEventSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("eventName"))
			this.eventName = json.get("eventName").getAsString();

		if(json.has("eventTime"))
			this.eventTime = json.get("eventTime").getAsDouble();

		super.loadFromJSON(json);
	}


	/** Called every world tick when an event is active. **/
	public void onTick(Level world, MobEventPlayerServer mobEventPlayerServer) {
		if(mobEventPlayerServer == null) {
			return;
		}

		// Event Spawner Check:
		if(!mobEventPlayerServer.matchesEvent(this.eventName)) {
			return;
		}

		// Trigger Tick:
		int triggerTick = mobEventPlayerServer.getTriggerTick(this.eventTime);
		if(mobEventPlayerServer.getTicks() != triggerTick) {
			return;
		}

		this.trigger(world, mobEventPlayerServer.getPlayer(), mobEventPlayerServer.getOrigin(), mobEventPlayerServer.getLevel(), 0);
	}
}
