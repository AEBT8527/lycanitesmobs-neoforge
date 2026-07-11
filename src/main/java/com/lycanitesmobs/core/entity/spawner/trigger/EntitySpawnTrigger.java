package com.lycanitesmobs.core.entity.spawner.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class EntitySpawnTrigger extends SpawnTrigger {
	/** A list of creature attributes that match this trigger. **/
	protected List<LycanitesMobType> creatureAttributes = new ArrayList<>();

	/** Determines if the entity types list is a blacklist or whitelist. **/
	protected String entityTypesListType = "whitelist";

	/** A list of entity ids that match this trigger. **/
	protected List<String> entityIds = new ArrayList<>();

	/** Determines if the entity ids list is a blacklist or whitelist. **/
	protected String entityIdsListType = "blacklist";


	/** Constructor **/
	public EntitySpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("entityTypes")) {
			JsonArray jsonArray = json.get("entityTypes").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				LycanitesMobType creatureAttribute = null;
				String creatureAttributeName = jsonIterator.next().getAsString();
				if("undead".equals(creatureAttributeName)) {
					creatureAttribute = LycanitesMobType.UNDEAD;
				}
				else if("arthropod".equals(creatureAttributeName)) {
					creatureAttribute = LycanitesMobType.ARTHROPOD;
				}
				else if("water".equals(creatureAttributeName)) {
					creatureAttribute = LycanitesMobType.WATER;
				}
				else if("illager".equals(creatureAttributeName)) {
					creatureAttribute = LycanitesMobType.ILLAGER;
				}
				else if("undefined".equals(creatureAttributeName)) {
					creatureAttribute = LycanitesMobType.UNDEFINED;
				}
				if(creatureAttribute != null) {
					this.creatureAttributes.add(creatureAttribute);
				}
			}
		}

		if(json.has("entityTypesListType"))
			this.entityTypesListType = json.get("entityTypesListType").getAsString();

		if(json.has("entityIds")) {
			JsonArray jsonArray = json.get("entityIds").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				String entityId = jsonIterator.next().getAsString();
				if(entityId != null) {
					entityIds.add(entityId);
				}
			}
		}

		if(json.has("entityIdsListType"))
			this.entityIdsListType = json.get("entityIdsListType").getAsString();

		super.loadFromJSON(json);
	}


	/** Returns true if the provided entity should trigger this Spawn Trigger. **/
	public boolean isMatchingEntity(LivingEntity killedEntity) {

		// Check Entity Type:
		if(!this.creatureAttributes.isEmpty()) {
			if (this.creatureAttributes.contains(com.lycanitesmobs.core.entity.LycanitesMobType.of(killedEntity))) {
				if ("blacklist".equalsIgnoreCase(this.entityTypesListType)) {
					return false;
				}
			}
			else {
				if ("whitelist".equalsIgnoreCase(this.entityTypesListType)) {
					return false;
				}
			}
		}

		// Check Entity Id:
		if(!this.entityIds.isEmpty()) {
			ResourceLocation entityResourceLocation = LMHelperClass.convertToResourceLocation(killedEntity.getType(), killedEntity.level().registryAccess());
			if(entityResourceLocation == null) {
				return false;
			}
			String entityId = entityResourceLocation.toString();
			if (this.entityIds.contains(entityId)) {
				if ("blacklist".equalsIgnoreCase(this.entityIdsListType)) {
					return false;
				}
			}
			else {
				if ("whitelist".equalsIgnoreCase(this.entityIdsListType)) {
					return false;
				}
			}
		}

		return true;
	}
}
