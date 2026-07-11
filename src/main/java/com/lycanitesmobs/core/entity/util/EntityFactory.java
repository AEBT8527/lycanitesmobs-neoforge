package com.lycanitesmobs.core.entity.util;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class EntityFactory implements EntityType.EntityFactory<Entity> {
    private static EntityFactory INSTANCE;
    private final Map<EntityType, EntityType.EntityFactory<? extends Entity>> entityTypeFactoryMap = new HashMap<>();
    private final Map<String, EntityType> entityTypeNetworkMap = new HashMap<>();

    /**
     * Returns the main Entity Factory instance or creates it and returns it.
     **/
    public static EntityFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EntityFactory();
        }
        return INSTANCE;
    }

    public EntityType getEntityTypeByNetworkName(String networkName) {
        return this.entityTypeNetworkMap.get(networkName);
    }

    public boolean hasEntityTypeNetworkName(String networkName) {
        return this.entityTypeNetworkMap.containsKey(networkName);
    }

    /**
     * Adds a new Entity Type and factory mapping for this Factory to create.
     *
     * @param entityType  The Entity Type to create from.
     * @param factory     The factory to instantiate the entity.
     */
    public void addEntityType(EntityType entityType, EntityType.EntityFactory<? extends Entity> factory, String networkName) {
        LMHelperClass.logDebug("Entity", "Adding entity factory: " + factory + " Type: " + entityType.getDescription() + " Classification: " + entityType.getCategory());

        this.entityTypeFactoryMap.put(entityType, factory);
        this.entityTypeNetworkMap.put(networkName, entityType);
    }

    /**
     * Creates an entity from an entity type in the provided world.
     *
     * @param entityType The entity type to create an entity from.
     * @param world      The world to create the entity in.
     * @return The created entity or null if no entity could be created.
     */
    @Override
    public Entity create(EntityType entityType, Level world) {
        if (!this.entityTypeFactoryMap.containsKey(entityType)) {
            LMHelperClass.logWarningMessage("Unable to find factory for Entity Type: " + entityType);
            for (EntityType<?> type : this.entityTypeFactoryMap.keySet()) {
                LMHelperClass.logWarningMessage("Values: " + type);
            }
            return null;
        }
        LMHelperClass.logDebug("Entity", "Spawning entity: " + this.entityTypeFactoryMap.get(entityType) + " - " + entityType.getCategory());
        EntityType.EntityFactory<? extends Entity> factory = this.entityTypeFactoryMap.get(entityType);
        return factory.create(entityType, world);
    }

}
