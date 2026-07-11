package com.lycanitesmobs.core.capabilities.util;


import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class BossEntry {
    protected UUID uuid;
    protected Entity entity;
    protected long lastUpdated = -1;
    protected int nearbyRange = 60;

    public UUID getUuid() {
        return this.uuid;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public long getLastUpdated() {
        return this.lastUpdated;
    }

    public int getNearbyRange() {
        return this.nearbyRange;
    }

    public void overrideNearbyRange(int nearbyRange) {
        this.nearbyRange = nearbyRange;
    }

    public boolean isAlive() {
        return this.entity != null && this.entity.isAlive();
    }

    public boolean isNear(Vec3 pos) {
        return this.isAlive() && this.entity.distanceToSqr(pos) <= (long) this.nearbyRange * this.nearbyRange;
    }

    /**
     * Updates this boss entry with the provided entity.
     *
     * @param entity The entity to update this boss entry with.
     */
    public void update(Entity entity) {
        this.uuid = entity.getUUID();
        this.entity = entity;
        this.lastUpdated = entity.level().getGameTime();
        if (entity instanceof BaseCreatureEntity) {
            BaseCreatureEntity creatureEntity = (BaseCreatureEntity) entity;
            if (creatureEntity.getCreatureInfo() != null) {
                this.nearbyRange = creatureEntity.getBossNearbyRange();
            }
        }
    }
}
