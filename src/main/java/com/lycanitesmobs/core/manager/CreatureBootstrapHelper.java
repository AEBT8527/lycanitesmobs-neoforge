package com.lycanitesmobs.core.manager;

import net.minecraft.core.registries.BuiltInRegistries;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.creature.CreatureSpawnConfig;
import com.lycanitesmobs.core.data.info.creature.Subspecies;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.util.EntityFactory;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Owns creature startup/bootstrap side effects that are not part of JSON definition parsing.
 */
public final class CreatureBootstrapHelper {
    private CreatureBootstrapHelper() {
    }

    public static void registerEntityTypeSuppliers(Collection<CreatureInfo> creatures) {
        for (CreatureInfo creatureInfo : creatures) {
            EntityType.Builder<?> entityTypeBuilder = createEntityTypeBuilder(creatureInfo);
            ObjectManager.addEntityType(creatureInfo.getName(), entityTypeBuilder);
        }
    }

    public static void bindRegisteredValues(Collection<CreatureInfo> creatures, CreatureSpawnConfig spawnConfig) {
        for (CreatureInfo creatureInfo : creatures) {
            Identifier id = AssetHelper.modResource(creatureInfo.getName());
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
            if (entityType == null || !id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType))) {
                continue;
            }
            creatureInfo.bindEntityType((EntityType<? extends LivingEntity>) entityType);
            EntityFactory.getInstance().addEntityType(creatureInfo.getEntityType(), (type, level) -> creatureInfo.createEntity(level), creatureInfo.getName());
            initializeCreature(creatureInfo, spawnConfig);
        }
    }

    public static void initializeCreature(CreatureInfo creatureInfo, CreatureSpawnConfig spawnConfig) {
        if (creatureInfo.isDummy()) {
            return;
        }

        registerSounds(creatureInfo, "");
        for (Subspecies subspecies : creatureInfo.getSubspeciesEntries()) {
            registerSubspeciesSounds(creatureInfo, subspecies);
        }

        creatureInfo.getCreatureSpawn().registerVanillaSpawns(creatureInfo, spawnConfig);

        LMHelperClass.logDebug("Creature", "Creature Loaded: " + creatureInfo.getName() + " - " + creatureInfo.getEntityClass() + " (" + creatureInfo.getModInfo().name + ")");
    }

    private static void registerSubspeciesSounds(CreatureInfo creatureInfo, Subspecies subspecies) {
        if (subspecies.getName() != null && !creatureInfo.hasLoadedSubspeciesSkin(subspecies.getName())) {
            registerSounds(creatureInfo, "." + subspecies.getName());
            creatureInfo.markSubspeciesSkinLoaded(subspecies.getName());
        }
    }

    private static void registerSounds(CreatureInfo creatureInfo, String suffix) {
        ObjectManager.addSound(creatureInfo.getName() + suffix + "_say", "entity." + creatureInfo.getName() + suffix + ".say");
        ObjectManager.addSound(creatureInfo.getName() + suffix + "_hurt", "entity." + creatureInfo.getName() + suffix + ".hurt");
        ObjectManager.addSound(creatureInfo.getName() + suffix + "_death", "entity." + creatureInfo.getName() + suffix + ".death");
        ObjectManager.addSound(creatureInfo.getName() + suffix + "_step", "entity." + creatureInfo.getName() + suffix + ".step");
        ObjectManager.addSound(creatureInfo.getName() + suffix + "_attack", "entity." + creatureInfo.getName() + suffix + ".attack");
        ObjectManager.addSound(creatureInfo.getName() + suffix + "_jump", "entity." + creatureInfo.getName() + suffix + ".jump");
        ObjectManager.addSound(creatureInfo.getName() + suffix + "_fly", "entity." + creatureInfo.getName() + suffix + ".fly");
        if (creatureInfo.isSummonable() || creatureInfo.isTameable() || creatureInfo.isEntityClassAssignableTo(TameableCreatureEntity.class)) {
            ObjectManager.addSound(creatureInfo.getName() + suffix + "_tame", "entity." + creatureInfo.getName() + suffix + ".tame");
            ObjectManager.addSound(creatureInfo.getName() + suffix + "_beg", "entity." + creatureInfo.getName() + suffix + ".beg");
        }
        if (creatureInfo.isTameable())
            ObjectManager.addSound(creatureInfo.getName() + suffix + "_eat", "entity." + creatureInfo.getName() + suffix + ".eat");
        if (creatureInfo.isMountable())
            ObjectManager.addSound(creatureInfo.getName() + suffix + "_mount", "entity." + creatureInfo.getName() + suffix + ".mount");
        if (creatureInfo.isBoss())
            ObjectManager.addSound(creatureInfo.getName() + suffix + "_phase", "entity." + creatureInfo.getName() + suffix + ".phase");
    }

    @NotNull
    private static EntityType.Builder<?> createEntityTypeBuilder(CreatureInfo creatureInfo) {
        EntityType.Builder<?> entityTypeBuilder = EntityType.Builder.of((entityType, level) -> creatureInfo.createEntity(level), creatureInfo.isPeaceful() ? MobCategory.CREATURE : MobCategory.MONSTER);
        //entityTypeBuilder.setCustomClientFactory(EntityFactory.getInstance().createOnClientFunction); // Was created when experimenting with custom spawn packets, not in use currently but may be needed in the future.
        entityTypeBuilder.setTrackingRange(creatureInfo.isBoss() ? 32 : 10);
        entityTypeBuilder.setUpdateInterval(3);
        entityTypeBuilder.setShouldReceiveVelocityUpdates(false);
        entityTypeBuilder.sized((float) creatureInfo.getWidth(), (float) creatureInfo.getHeight());
        return entityTypeBuilder;
    }
}
