package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;

/**
 * Creature classification tag used throughout Lycanites Mobs.
 * <p>
 * Vanilla's {@code net.minecraft.world.entity.LycanitesMobType} was removed in 1.21 (its behaviour
 * moved to entity-type tags and the enchantment data-component system). Lycanites only ever
 * used LycanitesMobType as an internal label for its own creatures and for matching killed entities in
 * spawn triggers, so we replace it with this self-contained enum.
 */
public enum LycanitesMobType {
    UNDEFINED,
    UNDEAD,
    ARTHROPOD,
    WATER,
    ILLAGER;

    /**
     * Best-effort classification for any living entity. Lycanites creatures report their own
     * stored attribute; vanilla/other-mod entities are mapped from their entity-type tags.
     */
    public static LycanitesMobType of(LivingEntity entity) {
        if (entity == null) {
            return UNDEFINED;
        }
        if (entity instanceof BaseCreatureEntity creature) {
            return creature.getLycMobType();
        }
        var type = entity.getType();
        if (type.is(EntityTypeTags.UNDEAD)) {
            return UNDEAD;
        }
        if (type.is(EntityTypeTags.ARTHROPOD)) {
            return ARTHROPOD;
        }
        if (type.is(EntityTypeTags.ILLAGER)) {
            return ILLAGER;
        }
        if (entity instanceof WaterAnimal) {
            return WATER;
        }
        return UNDEFINED;
    }
}
