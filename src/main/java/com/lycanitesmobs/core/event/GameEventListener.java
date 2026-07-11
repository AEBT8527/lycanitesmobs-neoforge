package com.lycanitesmobs.core.event;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.block.base.BlockFireBase;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.config.ConfigExtra;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.item.CustomItemEntity;
import com.lycanitesmobs.core.data.info.item.ItemConfig;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.network.message.MessagePlayerLeftClick;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityEvent.EntityConstructing;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import static com.lycanitesmobs.core.util.helpers.LMHelperClass.cast;

public class GameEventListener {

    // ==================================================
    //                     Constructor
    // ==================================================
    public GameEventListener() {
    }


    // ==================================================
    //                    World Load
    // ==================================================
    @SubscribeEvent
    public void onWorldLoading(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof Level))
            return;

        // ========== Extended World ==========
        ExtendedWorld.getForWorld((Level) event.getLevel());
    }

    @SubscribeEvent
    public void onWorldUnloading(LevelEvent.Unload event) {
        ExtendedWorld.unloadWorld(event.getLevel());
    }


    // ==================================================
    //                    Player Clone
    // ==================================================
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        // ExtendedEntity/ExtendedPlayer are now NeoForge data attachments (see LycanitesAttachments):
        // the player attachment is copied automatically on clone. We still snapshot it here so the
        // mod's own restore path in ExtendedPlayer.setPlayer keeps working unchanged.
        ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getOriginal());
        if (extendedPlayer != null) {
            extendedPlayer.backupPlayer();
        }
    }

    // ==================================================
    //                Entity Constructing
    // ==================================================
    @SubscribeEvent
    public void onEntityConstructing(EntityConstructing event) {
        if (event.getEntity() == null || event.getEntity().level() == null || event.getEntity().level().isClientSide())
            return;

        // ========== Force Remove Entity ==========
        if (!(event.getEntity() instanceof LivingEntity)) {
            if (!ExtendedEntity.getForceRemoveEntityIds().isEmpty()) {
                LMHelperClass.logDebug("ForceRemoveEntity", "Forced entity removal, checking: " + event.getEntity().getName());
                for (String forceRemoveID : ExtendedEntity.getForceRemoveEntityIds()) {
                    if (forceRemoveID.equalsIgnoreCase(LMHelperClass.convertToResourceLocation(event.getEntity().getType(), event.getEntity().level().registryAccess()).toString())) {
                        event.getEntity().remove(Entity.RemovalReason.DISCARDED);
                        break;
                    }
                }
            }
        }
    }


    // ==================================================
    //                Entity Leave World
    // ==================================================
    @SubscribeEvent
    public void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity((LivingEntity) event.getEntity());
        if (extendedEntity != null) {
            extendedEntity.onEntityRemoved();
        }
        if (event.getEntity() instanceof Player) {
            ExtendedPlayer.unloadClientPlayer((Player) event.getEntity());
        }
    }


    // ==================================================
    //                 Living Death Event
    // ==================================================
    @SubscribeEvent
    public void onLivingDeathEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        // ========== Extended Entity ==========
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
        if (extendedEntity != null)
            extendedEntity.onDeath();

        // ========== Extended Player ==========
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
            if (extendedPlayer != null)
                extendedPlayer.onDeath();
        }
    }


    // ==================================================
    //                   Entity Update
    // ==================================================
    @SubscribeEvent
    public void onEntityUpdate(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // ========== Extended Entity ==========
        ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
        if (extendedEntity != null)
            extendedEntity.onUpdate();

        // ========== Extended Player ==========
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            if (playerExt != null)
                playerExt.onUpdate();
        }
    }


    // ==================================================
    //                    Player Click
    // ==================================================
    @SubscribeEvent
    public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player == null)
            return;

        ItemStack itemStack = player.getItemInHand(event.getHand());
        Item item = itemStack.getItem();
        if (item instanceof ItemEquipment) {
            MessagePlayerLeftClick message = new MessagePlayerLeftClick();
            LycanitesMobs.PACKET_MANAGER.sendToServer(message);
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player == null || event.getSide().isClient())
            return;

        ItemStack itemStack = player.getItemInHand(event.getHand());
        Item item = itemStack.getItem();
        if (item instanceof ItemEquipment) {
            ((ItemEquipment) item).onItemLeftClick(event.getLevel(), player, event.getHand());
        }
    }


    // ==================================================
    //               Entity Interact Event
    // ==================================================
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity entity = event.getTarget();
        if (player == null || !(entity instanceof LivingEntity))
            return;

		/*ItemStack itemStack = player.getHeldItem(event.getHand());
		Item item = itemStack.getItem();
		if (item instanceof ItemBase) {
			if (item.itemInteractionForEntity(itemStack, player, (LivingEntity)entity, event.getHand())) {
				if (event.isCancelable())
					event.setCanceled(true);
			}
		}*/
    }


    // ==================================================
    //                 Attack Target Event
    // ==================================================
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackTarget(LivingChangeTargetEvent event) {
        Entity targetEntity = event.getNewAboutToBeSetTarget();
        if (event.getEntity() == null || targetEntity == null) {
            return;
        }

        // Better Invisibility:
        if (!event.getEntity().hasEffect(MobEffects.INVISIBILITY)) {
            if (targetEntity.isInvisible()) {
                event.setCanceled(true);
                //event.getEntity().setRevengeTarget(null);
                return;
            }
        }

        // Can Be Targeted:
        if (event.getEntity() instanceof Mob && targetEntity instanceof BaseCreatureEntity) {
            if (!((BaseCreatureEntity) targetEntity).canBeTargetedBy(event.getEntity())) {
                //event.getEntity().setRevengeTarget(null);
                event.setCanceled(true);
                //((MobEntity)event.getEntity()).setAttackTarget(null);
            }
        }
    }


    // ==================================================
    //                 Living Hurt Event
    // ==================================================
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.isCanceled())
            return;
        DamageSource damageSource = event.getSource();
        if (damageSource == null || event.getEntity() == null)
            return;

        LivingEntity damagedEntity = event.getEntity();
        ExtendedEntity damagedEntityExt = ExtendedEntity.getForEntity(damagedEntity);

        // True Source Extended Entity:
        if (damageSource.getEntity() != null &&
                damageSource.getEntity() instanceof LivingEntity entity) {
            ExtendedEntity attackerExtendedEntity = ExtendedEntity.getForEntity(entity);
            if (attackerExtendedEntity != null) {
                attackerExtendedEntity.setLastAttackedEntity(damagedEntity);
            }
        }

        // ========== Mounted Protection ==========
        if (damagedEntity.getVehicle() != null) {
            if (damagedEntity.getVehicle() instanceof RideableCreatureEntity) {
                RideableCreatureEntity creatureRideable = (RideableCreatureEntity) event.getEntity().getVehicle();

                // Shielding:
                if (creatureRideable.isBlocking()) {
                    event.setCanceled(true);
                    return;
                }

                // Prevent Mounted Entities from Suffocating:
                if (damageSource.is(DamageTypes.IN_WALL)) {
                    event.setCanceled(true);
                    return;
                }

                // Copy Mount Immunities to Rider:
                if (com.lycanitesmobs.core.util.helpers.LMHelperClass.isInvulnerableTo(creatureRideable, damageSource)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        // ========== Picked Up/Feared Protection ==========
        if (damagedEntityExt != null && damagedEntityExt.isPickedUp()) {
            // Prevent Picked Up and Feared Entities from Suffocating:
            if (damageSource.is(DamageTypes.IN_WALL)) {
                event.setCanceled(true);
                return;
            }
        }
    }


    // ==================================================
    //                 Living Drops Event
    // ==================================================
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        Level world = event.getEntity().level();

        if (!this.shouldDropSeasonalItem(event.getEntity())) {
            return;
        }

        Item seasonalItem = this.getSeasonalDropItem(world);
        if (seasonalItem == null) {
            return;
        }

        this.dropSeasonalItem(world, event.getEntity(), seasonalItem);
    }

    private boolean shouldDropSeasonalItem(LivingEntity entity) {
        if (ItemConfig.getSeasonalItemDropChance() <= 0) {
            return false;
        }
        if (!LMHelperClass.isHalloween() && !LMHelperClass.isYuletide() && !LMHelperClass.isNewYear()) {
            return false;
        }
        if (entity instanceof BaseCreatureEntity baseCreatureEntity && baseCreatureEntity.isMinion()) {
            return false;
        }
        return entity.getRandom().nextFloat() < ItemConfig.getSeasonalItemDropChance();
    }

    private Item getSeasonalDropItem(Level world) {
        if (LMHelperClass.isHalloween()) {
            return ObjectManager.getItem("halloweentreat");
        }
        if (LMHelperClass.isYuletide()) {
            if (LMHelperClass.isYuletidePeak() && world.getRandom().nextBoolean()) {
                return ObjectManager.getItem("wintergiftlarge");
            }
            return ObjectManager.getItem("wintergift");
        }
        return null;
    }

    private void dropSeasonalItem(Level world, LivingEntity entity, Item seasonalItem) {
        ItemStack dropStack = new ItemStack(seasonalItem, 1);
        CustomItemEntity entityItem = new CustomItemEntity(world, entity.position().x(), entity.position().y(), entity.position().z(), dropStack);
        entityItem.setPickUpDelay(10);
        DeferredLevelActionManager.spawnEntity(world, entity.blockPosition(), null, entityItem);
    }


    // ==================================================
    //                 Bucket Fill Event
    // ==================================================
    // NeoForge removed FillBucketEvent; custom-fluid bucket pickup is now handled by the fluid's
    // own bucket item / FluidType. This handler is dropped as there is no direct replacement event.


    // ==================================================
    //                 Break Block Event
    // ==================================================
    @SubscribeEvent
    public void onBlockBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent event) {
        if (event.getState() == null || event.getLevel() == null || event.getLevel().isClientSide() || event.isCanceled()) {
            return;
        }

        if (event.getPlayer() != null && !event.getPlayer().isCreative()) {
            if (event.getLevel() instanceof Level) {
                ExtendedWorld extendedWorld = ExtendedWorld.getForWorld((Level) event.getLevel());
                if (!(event.getState().getBlock() instanceof BlockFireBase) && extendedWorld.isBossNearby(Vec3.atLowerCornerOf(event.getPos()))) {
                    event.setCanceled(true);
                    event.getPlayer().sendOverlayMessage(Component.translatable("boss.block.protection.break"));
                    return;
                }
            }
        }

        if (event.getPlayer() != null) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getPlayer());
            if (extendedPlayer == null) {
                return;
            }
            extendedPlayer.setJustBrokenBlock(event.getState());
        }
    }


    // ==================================================
    //                 Block Place Event
    // ==================================================

    /**
     * This uses the block place events to update Block Spawn Triggers.
     **/
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getState() == null || event.getLevel() == null || event.getLevel().isClientSide() || event.isCanceled()) {
            return;
        }

        if (event.getEntity() instanceof Player && !((Player) event.getEntity()).isCreative()) {
            if (event.getLevel() instanceof Level) {
                ExtendedWorld extendedWorld = ExtendedWorld.getForWorld((Level) event.getLevel());
                if (extendedWorld.isBossNearby(Vec3.atLowerCornerOf(event.getPos()))) {
                    event.setCanceled(true);
                    ((Player) event.getEntity()).sendOverlayMessage(Component.translatable("boss.block.protection.place"));
                    return;
                }
            }
        }
    }


    // ==================================================
    //                   Check Spawn
    // ==================================================
    @SubscribeEvent
    public void onCheckSpawn(MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() != EntitySpawnReason.SPAWNER) {
            return;
        }

        Mob mob = event.getEntity();
        if (!(mob instanceof BaseCreatureEntity baseCreatureEntity)) {
            return;
        }

        if (!(event.getLevel() instanceof Level level)) {
            return;
        }

        BaseSpawner spawner = event.getSpawner();
        BlockPos originPos;

        if (spawner != null) {
            // 1.21: BaseSpawner no longer exposes its owning block entity/entity; the mob's own
            // position is close enough for the origin check here.
            originPos = mob.blockPosition();
        } else {
            originPos = mob.blockPosition();
        }

        if (!baseCreatureEntity.checkSpawnGroupLimit(level, originPos, 16)) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }


    // ==================================================
    //               Mounting / Dismounting
    // ==================================================
    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event) {
        if (!ConfigExtra.INSTANCE.disableSneakDismount.get() || true) { // Disabled for now as cancelling this event doesn't work correctly for players atm.
            return;
        }
        if (!(event.getEntityMounting() instanceof Player)) {
            return;
        }

        // Override Sneak to Dismount for Lycanites Mobs:
        if (event.isDismounting() && event.getEntityBeingMounted() instanceof RideableCreatureEntity) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer((Player) event.getEntityMounting());
            if (extendedPlayer == null) {
                return;
            }
            event.setCanceled(event.getEntityMounting().isShiftKeyDown() && !extendedPlayer.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT));
        }
    }


    // ==================================================
    //                 Projectile Impact
    // ==================================================
    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        Entity shooter = null;
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityRayTraceResult)) {
            return;
        }
        Entity target = entityRayTraceResult.getEntity();
        if (!(target instanceof BaseCreatureEntity)) {
            return;
        }
        BaseCreatureEntity targetCreature = (BaseCreatureEntity) target;

        if (event.getEntity() instanceof Projectile projectileEntity) {
            shooter = projectileEntity.getOwner();
        }
        if (event.getEntity() instanceof ThrowableItemProjectile projectileItemEntity) {
            shooter = projectileItemEntity.getOwner();
        }

        if (shooter instanceof LivingEntity living && LMHelperClass.isInvulnerableTo(targetCreature, targetCreature.level().damageSources().mobAttack(living))) {
            event.setCanceled(true);
        }
    }
}
