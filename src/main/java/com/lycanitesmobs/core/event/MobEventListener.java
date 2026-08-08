package com.lycanitesmobs.core.event;

import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.data.config.ConfigExtra;
import com.lycanitesmobs.core.entity.creature.aberration.EntityFear;
import com.lycanitesmobs.core.entity.effect.EffectBase;
import com.lycanitesmobs.core.entity.IGroupBoss;
import com.lycanitesmobs.core.event.mobevent.MobEventSchedule;
import com.lycanitesmobs.core.event.mobevent.trigger.AltarMobEventTrigger;
import com.lycanitesmobs.core.event.mobevent.trigger.MobEventTrigger;
import com.lycanitesmobs.core.event.mobevent.trigger.RandomMobEventTrigger;
import com.lycanitesmobs.core.event.mobevent.trigger.TickMobEventTrigger;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.EffectManager;
import com.lycanitesmobs.core.manager.MobEventManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.network.message.MessageEntityVelocity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.util.math.SchismMath;
import com.lycanitesmobs.LycanitesMobs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class MobEventListener {
    private static MobEventListener INSTANCE;

    // Resolved once - these are registry lookups and this listener runs per entity per tick.
    private boolean tickEffectsResolved;
    private EffectBase paralysisEffect;
    private EffectBase weightEffect;
    private EffectBase fearEffect;
    private EffectBase instabilityEffect;
    private EffectBase plagueEffect;
    private EffectBase smitedEffect;
    private EffectBase bleedEffect;
    private EffectBase smoulderingEffect;
    private EffectBase swiftswimmingEffect;
    private EffectBase immunizationEffect;
    private EffectBase cleansedEffect;


    private final List<RandomMobEventTrigger> randomMobEventTriggers = new ArrayList<>();
    private final List<TickMobEventTrigger> tickMobEventTriggers = new ArrayList<>();


    /**
     * Returns the main Mob Event Listener instance.
     **/
    public static MobEventListener getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MobEventListener();
        }
        return INSTANCE;
    }


    /**
     * Adds a new Mob Event Trigger.
     *
     * @return True on success, false if it failed to add (could happen if the Trigger type has no matching list created yet).
     */
    public boolean addTrigger(MobEventTrigger mobEventTrigger) {
        if (mobEventTrigger instanceof RandomMobEventTrigger randomMobEventTrigger) {
            return this.addTrigger(this.randomMobEventTriggers, randomMobEventTrigger);
        }
        if (mobEventTrigger instanceof TickMobEventTrigger tickMobEventTrigger) {
            return this.addTrigger(this.tickMobEventTriggers, tickMobEventTrigger);
        }
        return false;
    }

    private <T extends MobEventTrigger> boolean addTrigger(List<T> triggers, T mobEventTrigger) {
        if (triggers.contains(mobEventTrigger)) {
            return false;
        }
        triggers.add(mobEventTrigger);
        return true;
    }

    /**
     * Removes a Mob Event Trigger.
     */
    public void removeTrigger(MobEventTrigger mobEventTrigger) {
        this.removeTrigger(this.randomMobEventTriggers, mobEventTrigger);
        this.removeTrigger(this.tickMobEventTriggers, mobEventTrigger);
        if (mobEventTrigger instanceof AltarMobEventTrigger altarMobEventTrigger) {
            altarMobEventTrigger.onRemove();
        }
    }

    private void removeTrigger(List<? extends MobEventTrigger> triggers, MobEventTrigger mobEventTrigger) {
        triggers.remove(mobEventTrigger);
    }


    /**
     * Called every tick in a world and counts down to the next event then fires it! The countdown is paused during an event.
     **/
    @SubscribeEvent
    public void onWorldUpdate(LevelTickEvent.Post event) {
        Level world = event.getLevel();
        if (world.isClientSide()) {
            return;
        }
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        if (worldExt == null) {
            return;
        }

        if (!MobEventManager.getInstance().areMobEventsEnabled() || world.getDifficulty() == Difficulty.PEACEFUL) {
            if (worldExt.hasServerWorldEventPlayer()) {
                worldExt.stopWorldEvent();
            }
            return;
        }

        if (!worldExt.markEventScheduleTickIfFresh(world.getGameTime())) {
            return;
        }
        long eventScheduleTick = worldExt.getLastEventScheduleTime();

        for (MobEventSchedule mobEventSchedule : MobEventManager.getInstance().getMobEventSchedules()) {
            if (mobEventSchedule.canStart(world)) {
                mobEventSchedule.start(worldExt);
            }
        }

        for (TickMobEventTrigger mobEventTrigger : this.tickMobEventTriggers) {
            mobEventTrigger.onTick(world, eventScheduleTick);
        }

        if (MobEventManager.getInstance().areRandomMobEventsEnabled()) {
            if (MobEventManager.getInstance().getMinEventsRandomDay() > 0
                    && Math.floor(worldExt.getConfiguredDayBaseTime(world) / 24000D) < MobEventManager.getInstance().getMinEventsRandomDay()) {
                return;
            }
            if (worldExt.getWorldEventStartTargetTime() <= 0
                    || worldExt.getWorldEventStartTargetTime() > world.getGameTime() + MobEventManager.getInstance().getMaxTicksUntilEvent()) {
                worldExt.setWorldEventStartTargetTime(world.getGameTime() + worldExt.getRandomEventDelay(world.getRandom()));
            }
            if (world.getGameTime() == worldExt.getWorldEventStartTargetTime()) {
                this.triggerRandomMobEvent(world, worldExt, 1);
            } else if (world.getGameTime() > worldExt.getWorldEventStartTargetTime()) {
                worldExt.setWorldEventStartTargetTime(0);
            }
        }
    }


    /**
     * Triggers a Random Mob Event Trigger if one is available.
     **/
    public void triggerRandomMobEvent(Level world, ExtendedWorld worldExt, int level) {
        List<RandomMobEventTrigger> validTriggers = new ArrayList<>();
        int totalWeights = 0;
        int highestPriority = 0;
        for (RandomMobEventTrigger mobEventTrigger : this.randomMobEventTriggers) {
            if (mobEventTrigger.getPriority() >= highestPriority && mobEventTrigger.canTrigger(world, null)) {
                if (mobEventTrigger.getPriority() > highestPriority) {
                    totalWeights = 0;
                    validTriggers.clear();
                }
                totalWeights += mobEventTrigger.getWeight();
                highestPriority = mobEventTrigger.getPriority();
                validTriggers.add(mobEventTrigger);
            }
        }
        if (totalWeights <= 0) {
            return;
        }

        int randomWeight = 1;
        if (totalWeights > 1) {
            randomWeight = world.getRandom().nextInt(totalWeights - 1) + 1;
        }
        int searchWeight = 0;
        for (RandomMobEventTrigger mobEventTrigger : validTriggers) {
            if (mobEventTrigger.getWeight() + searchWeight > randomWeight) {
                mobEventTrigger.trigger(world, null, new BlockPos(0, 0, 0), level, -1);
                return;
            }
            searchWeight += mobEventTrigger.getWeight();
        }
    }


    private static final Identifier SWIFTSWIMMING_MOVE_BOOST_UUID = Identifier.fromNamespaceAndPath("lycanitesmobs", "swiftswimming_speed_boost_1");
    private static final AttributeModifier SWIFTSWIMMING_MOVE_BOOST = new AttributeModifier(SWIFTSWIMMING_MOVE_BOOST_UUID, 1D, AttributeModifier.Operation.ADD_VALUE);
    private static final Identifier SWIFTSWIMMING_MOVE_BOOST_UUID_2 = Identifier.fromNamespaceAndPath("lycanitesmobs", "swiftswimming_speed_boost_2");
    private static final AttributeModifier SWIFTSWIMMING_MOVE_BOOST_2 = new AttributeModifier(SWIFTSWIMMING_MOVE_BOOST_UUID_2, 2D, AttributeModifier.Operation.ADD_VALUE);
    private static final Identifier SWIFTSWIMMING_MOVE_BOOST_UUID_3 = Identifier.fromNamespaceAndPath("lycanitesmobs", "swiftswimming_speed_boost_3");
    private static final AttributeModifier SWIFTSWIMMING_MOVE_BOOST_3 = new AttributeModifier(SWIFTSWIMMING_MOVE_BOOST_UUID_3, 3D, AttributeModifier.Operation.ADD_VALUE);
    private static final Identifier SWIFTSWIMMING_MOVE_BOOST_UUID_4 = Identifier.fromNamespaceAndPath("lycanitesmobs", "swiftswimming_speed_boost_4");
    private static final AttributeModifier SWIFTSWIMMING_MOVE_BOOST_4 = new AttributeModifier(SWIFTSWIMMING_MOVE_BOOST_UUID_4, 4D, AttributeModifier.Operation.ADD_VALUE);


    @SubscribeEvent
    public void onEntityUpdate(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        this.resolveTickEffects();

        this.clearNullEffects(entity);
        this.clearBlindnessForNightVision(entity);
        this.removeDisabledNausea(event, entity);
        this.handleSwiftswimming(entity);

        if (!this.hasLycanitesTickEffect(entity)) {
            return;
        }

        boolean invulnerable = this.isInvulnerable(entity);

        this.handleParalysis(entity, invulnerable);
        this.handleWeight(entity, invulnerable);
        this.handleFear(entity, invulnerable);
        this.handleInstability(entity, invulnerable);
        this.handlePlague(entity, invulnerable);
        this.handleSmited(entity, invulnerable);
        this.handleBleed(event, entity, invulnerable);
        this.handleSmouldering(entity, invulnerable);

        this.handleImmunization(entity);
        this.handleCleansed(entity);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) {
            return;
        }

        for (EntityFear fearEntity : player.level().getEntitiesOfClass(
                EntityFear.class,
                player.getBoundingBox().inflate(128.0D),
                fear -> player.equals(fear.getHauntTarget())
        )) {
            fearEntity.discard();
        }
    }

    @SubscribeEvent
    public void onEntityJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || this.isInvulnerable(entity)) {
            return;
        }

        // NeoForge's LivingJumpEvent is not cancellable, so paralysis/weight can no longer
        // veto a jump here (this was already effectively a no-op under Forge, where the event
        // was not @Cancelable either).
    }

    @SubscribeEvent
    public void onLivingAttack(LivingIncomingDamageEvent event) {
        if (event.isCanceled()) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target == null) {
            return;
        }

        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity livingAttacker ? livingAttacker : null;
        if (attacker == null) {
            return;
        }

        EffectBase lifeleak = ObjectManager.getEffect("lifeleak");
        if (lifeleak != null && !target.level().isClientSide() && attacker.hasEffect(ObjectManager.holder(lifeleak))) {
            event.setCanceled(true);
            target.heal(event.getAmount());
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.isCanceled()) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target == null) {
            return;
        }

        Entity attacker = event.getSource().getEntity();

        this.handleFallResistance(event, target);
        this.handlePenetration(event, target);
        this.handleFearWallCollision(event, target);
        this.handleLeech(event);
        this.handleRepulsion(target, attacker);
    }

    @SubscribeEvent
    public void onEntityHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) {
            return;
        }

        EffectBase rejuvenation = ObjectManager.getEffect("rejuvenation");
        if (rejuvenation != null && entity.hasEffect(ObjectManager.holder(rejuvenation))) {
            event.setAmount((float) Math.ceil(event.getAmount() * (2 * (1 + entity.getEffect(ObjectManager.holder(rejuvenation)).getAmplifier()))));
        }

        EffectBase decay = ObjectManager.getEffect("decay");
        if (decay != null && entity.hasEffect(ObjectManager.holder(decay))) {
            event.setAmount((float) Math.floor(event.getAmount() / (2 * (1 + entity.getEffect(ObjectManager.holder(decay)).getAmplifier()))));
        }
    }

    @SubscribeEvent
    public void onSleep(CanPlayerSleepEvent event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) {
            return;
        }

        EffectBase insomnia = ObjectManager.getEffect("insomnia");
        if (insomnia != null && player.hasEffect(ObjectManager.holder(insomnia))) {
            event.setProblem(Player.BedSleepingProblem.NOT_SAFE);
        }
    }

    @SubscribeEvent
    public void onLivingUseItem(LivingEntityUseItemEvent.Start event) {
        if (event.isCanceled()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity == null) {
            return;
        }

        EffectBase aphagia = ObjectManager.getEffect("aphagia");
        if (aphagia != null && !entity.level().isClientSide() && entity.hasEffect(ObjectManager.holder(aphagia))) {
            event.setCanceled(true);
        }
    }

    private void clearNullEffects(LivingEntity entity) {
        for (Object effectObj : entity.getActiveEffects()) {
            if (effectObj == null) {
                entity.removeAllEffects();
                LMHelperClass.logWarning("EffectsSetup", "Found a null potion effect on entity: " + entity + " all effects have been removed from this entity.");
            }
        }
    }

    private void clearBlindnessForNightVision(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.BLINDNESS) && entity.hasEffect(MobEffects.NIGHT_VISION)) {
            entity.removeEffect(MobEffects.BLINDNESS);
        }
    }

    private void removeDisabledNausea(EntityTickEvent.Pre event, LivingEntity entity) {
        EffectManager effectManager = EffectManager.getInstance();
        effectManager.setNauseaDisabled(ConfigExtra.INSTANCE.disableNausea.get());
        if (effectManager.isNauseaDisabled() && event.getEntity() instanceof Player && entity.hasEffect(MobEffects.NAUSEA)) {
            entity.removeEffect(MobEffects.NAUSEA);
        }
    }

    private boolean isInvulnerable(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.isCreative() || player.isSpectator();
        }
        return false;
    }

    private void handleParalysis(LivingEntity entity, boolean invulnerable) {
        EffectBase paralysis = this.paralysisEffect;
        if (paralysis != null && !invulnerable && entity.hasEffect(ObjectManager.holder(paralysis))) {
            entity.setDeltaMovement(0, entity.getDeltaMovement().y() > 0 ? 0 : entity.getDeltaMovement().y(), 0);
            entity.setOnGround(false);
        }
    }

    private void handleWeight(LivingEntity entity, boolean invulnerable) {
        EffectBase weight = this.weightEffect;
        if (weight != null && !invulnerable && entity.hasEffect(ObjectManager.holder(weight)) && !entity.hasEffect(MobEffects.STRENGTH)) {
            if (entity.getDeltaMovement().y() > -0.2D) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.2D, 0));
            }
        }
    }

    private void handleFear(LivingEntity entity, boolean invulnerable) {
        EffectBase fear = this.fearEffect;
        if (fear != null && !entity.level().isClientSide() && !invulnerable && entity.hasEffect(ObjectManager.holder(fear)) && entity instanceof Player player) {
            EntityFear.spawnForPlayer(player, null);
        }
    }

    private void handleInstability(LivingEntity entity, boolean invulnerable) {
        EffectBase instability = this.instabilityEffect;
        if (instability == null || entity.level().isClientSide() || entity instanceof IGroupBoss) {
            return;
        }
        if (invulnerable || !entity.hasEffect(ObjectManager.holder(instability)) || entity.level().getRandom().nextDouble() > 0.1D) {
            return;
        }

        double strength = 1 + entity.getEffect(ObjectManager.holder(instability)).getAmplifier();
        double motionX = strength * (entity.level().getRandom().nextDouble() - 0.5D);
        double motionY = strength * (entity.level().getRandom().nextDouble() - 0.5D);
        double motionZ = strength * (entity.level().getRandom().nextDouble() - 0.5D);
        entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
        try {
            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(entity));
                MessageEntityVelocity messageEntityVelocity = new MessageEntityVelocity(
                        player,
                        strength * (entity.level().getRandom().nextDouble() - 0.5D),
                        strength * (entity.level().getRandom().nextDouble() - 0.5D),
                        strength * (entity.level().getRandom().nextDouble() - 0.5D)
                );
                LycanitesMobs.PACKET_MANAGER.sendToPlayer(messageEntityVelocity, player);
            }
            else {
                // Non-players get no velocity packet of their own; without this the server
                // never syncs the shove and the client rubber-bands them back.
                entity.hurtMarked = true;
            }
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("Failed to create and send a network packet for instability velocity!");
            e.printStackTrace();
        }
    }

    private void handlePlague(LivingEntity entity, boolean invulnerable) {
        EffectBase plague = this.plagueEffect;
        if (plague == null || entity.level().isClientSide() || invulnerable || !entity.hasEffect(ObjectManager.holder(plague))) {
            return;
        }

        int poisonAmplifier = entity.getEffect(ObjectManager.holder(plague)).getAmplifier();
        int poisonDuration = entity.getEffect(ObjectManager.holder(plague)).getDuration();
        if (entity.hasEffect(MobEffects.POISON)) {
            poisonAmplifier = Math.max(poisonAmplifier, entity.getEffect(MobEffects.POISON).getAmplifier());
            poisonDuration = Math.max(poisonDuration, entity.getEffect(MobEffects.POISON).getDuration());
        }
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, poisonAmplifier));

        if (entity.level().getGameTime() % 20 != 0) {
            return;
        }

        List aoeTargets = EffectManager.getInstance().getNearbyEntities(entity, LivingEntity.class, null, 2);
        for (Object entityObj : aoeTargets) {
            LivingEntity target = (LivingEntity) entityObj;
            if (target == entity || entity.isAlliedTo(target)) {
                continue;
            }
            if (target instanceof Player && !entity.hasLineOfSight(target)) {
                continue;
            }

            int amplifier = entity.getEffect(ObjectManager.holder(plague)).getAmplifier();
            int duration = entity.getEffect(ObjectManager.holder(plague)).getDuration();
            if (amplifier > 0) {
                target.addEffect(new MobEffectInstance(ObjectManager.holder(plague), duration, amplifier - 1));
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amplifier));
            }
        }
    }

    private void handleSmited(LivingEntity entity, boolean invulnerable) {
        EffectBase smited = this.smitedEffect;
        if (smited != null && !entity.level().isClientSide() && !invulnerable && entity.hasEffect(ObjectManager.holder(smited)) && entity.level().getGameTime() % 20 == 0) {
            float brightness = LMHelperClass.getBrightness(entity);
            if (brightness > 0.5F && entity.level().canSeeSkyFromBelowWater(entity.blockPosition())) {
                entity.igniteForSeconds(4);
            }
        }
    }

    private void handleBleed(EntityTickEvent.Pre event, LivingEntity entity, boolean invulnerable) {
        EffectBase bleed = this.bleedEffect;
        if (bleed != null && !entity.level().isClientSide() && !invulnerable && entity.hasEffect(ObjectManager.holder(bleed)) && entity.level().getGameTime() % 20 == 0 && entity.getVehicle() == null) {
            if (entity.xOld != entity.getX() || entity.zOld != entity.getZ()) {
                entity.hurt(event.getEntity().level().damageSources().magic(), entity.getEffect(ObjectManager.holder(bleed)).getAmplifier() + 1);
            }
        }
    }

    private void handleSmouldering(LivingEntity entity, boolean invulnerable) {
        EffectBase smouldering = this.smoulderingEffect;
        if (smouldering != null && !entity.level().isClientSide() && !invulnerable && entity.hasEffect(ObjectManager.holder(smouldering)) && entity.level().getGameTime() % 20 == 0) {
            entity.igniteForSeconds(4 + (4 * entity.getEffect(ObjectManager.holder(smouldering)).getAmplifier()));
        }
    }

    private void handleSwiftswimming(LivingEntity entity) {
        if (!(entity instanceof Player)) {
            return;
        }

        EffectBase swiftswimming = this.swiftswimmingEffect;
        if (swiftswimming == null) {
            return;
        }

        AttributeInstance movement = entity.getAttribute(NeoForgeMod.SWIM_SPEED);
        if (movement == null) {
            return;
        }

        int amplifier = -1;
        if (entity.hasEffect(ObjectManager.holder(swiftswimming))) {
            amplifier = entity.getEffect(ObjectManager.holder(swiftswimming)).getAmplifier();
        }

        this.syncSwiftswimmingModifier(movement, amplifier == 0, SWIFTSWIMMING_MOVE_BOOST_UUID, SWIFTSWIMMING_MOVE_BOOST);
        this.syncSwiftswimmingModifier(movement, amplifier == 1, SWIFTSWIMMING_MOVE_BOOST_UUID_2, SWIFTSWIMMING_MOVE_BOOST_2);
        this.syncSwiftswimmingModifier(movement, amplifier == 2, SWIFTSWIMMING_MOVE_BOOST_UUID_3, SWIFTSWIMMING_MOVE_BOOST_3);
        this.syncSwiftswimmingModifier(movement, amplifier >= 3, SWIFTSWIMMING_MOVE_BOOST_UUID_4, SWIFTSWIMMING_MOVE_BOOST_4);
    }

    private void syncSwiftswimmingModifier(AttributeInstance movement, boolean shouldApply, Identifier modifierId, AttributeModifier modifier) {
        if (shouldApply && movement.getModifier(modifierId) == null) {
            movement.addPermanentModifier(modifier);
        } else if (!shouldApply && movement.getModifier(modifierId) != null) {
            movement.removeModifier(modifier);
        }
    }

    private void handleImmunization(LivingEntity entity) {
        EffectBase immunization = this.immunizationEffect;
        if (immunization == null || entity.level().isClientSide() || !entity.hasEffect(ObjectManager.holder(immunization))) {
            return;
        }

        if (entity.hasEffect(MobEffects.POISON)) {
            entity.removeEffect(MobEffects.POISON);
        }
        if (entity.hasEffect(MobEffects.HUNGER)) {
            entity.removeEffect(MobEffects.HUNGER);
        }
        if (entity.hasEffect(MobEffects.WEAKNESS)) {
            entity.removeEffect(MobEffects.WEAKNESS);
        }
        if (entity.hasEffect(MobEffects.NAUSEA)) {
            entity.removeEffect(MobEffects.NAUSEA);
        }

        EffectBase paralysis = this.paralysisEffect;
        if (paralysis != null && entity.hasEffect(ObjectManager.holder(paralysis))) {
            entity.removeEffect(ObjectManager.holder(paralysis));
        }
    }

    private void handleCleansed(LivingEntity entity) {
        EffectBase cleansed = this.cleansedEffect;
        if (cleansed == null || entity.level().isClientSide() || !entity.hasEffect(ObjectManager.holder(cleansed))) {
            return;
        }

        if (entity.hasEffect(MobEffects.WITHER)) {
            entity.removeEffect(MobEffects.WITHER);
        }
        if (entity.hasEffect(MobEffects.UNLUCK)) {
            entity.removeEffect(MobEffects.UNLUCK);
        }

        EffectBase fear = this.fearEffect;
        if (fear != null && entity.hasEffect(ObjectManager.holder(fear))) {
            entity.removeEffect(ObjectManager.holder(fear));
        }

        EffectBase insomnia = ObjectManager.getEffect("insomnia");
        if (insomnia != null && entity.hasEffect(ObjectManager.holder(insomnia))) {
            entity.removeEffect(ObjectManager.holder(insomnia));
        }
    }

    private void handleFallResistance(LivingIncomingDamageEvent event, LivingEntity target) {
        EffectBase fallresist = ObjectManager.getEffect("fallresist");
        if (fallresist != null && target.hasEffect(ObjectManager.holder(fallresist)) && "fall".equals(event.getSource().getMsgId())) {
            event.setAmount(0);
            event.setCanceled(true);
        }
    }

    private void handlePenetration(LivingIncomingDamageEvent event, LivingEntity target) {
        EffectBase penetration = ObjectManager.getEffect("penetration");
        if (penetration != null && target.hasEffect(ObjectManager.holder(penetration))) {
            float damage = event.getAmount();
            float multiplier = 0.25F * (target.getEffect(ObjectManager.holder(penetration)).getAmplifier() + 1);
            event.setAmount(damage + (damage * multiplier));
        }
    }

    private void handleFearWallCollision(LivingIncomingDamageEvent event, LivingEntity target) {
        this.resolveTickEffects();
        EffectBase fear = this.fearEffect;
        if (fear != null && target.hasEffect(ObjectManager.holder(fear)) && "inWall".equals(event.getSource().getMsgId())) {
            event.setAmount(0);
            event.setCanceled(true);
        }
    }

    private void handleLeech(LivingIncomingDamageEvent event) {
        EffectBase leech = ObjectManager.getEffect("leech");
        if (leech == null || event.getSource().getEntity() == null) {
            return;
        }

        LivingEntity leechingEntity = null;
        if (event.getSource().getDirectEntity() instanceof LivingEntity directLiving) {
            leechingEntity = directLiving;
        } else if (event.getSource().getEntity() instanceof LivingEntity sourceLiving) {
            leechingEntity = sourceLiving;
        }

        if (leechingEntity != null && leechingEntity.hasEffect(ObjectManager.holder(leech))) {
            int leeching = leechingEntity.getEffect(ObjectManager.holder(leech)).getAmplifier() + 1;
            leechingEntity.heal(Math.max(leeching, 1));
        }
    }

    private void handleRepulsion(LivingEntity target, Entity attacker) {
        EffectBase repulsion = ObjectManager.getEffect("repulsion");
        if (repulsion == null) {
            return;
        }

        boolean attackerIsBoss = attacker instanceof IGroupBoss;
        if (!attackerIsBoss && CreatureManager.getInstance().getCreatureGroup("boss") != null) {
            attackerIsBoss = CreatureManager.getInstance().getCreatureGroup("boss").hasEntity(attacker);
        }
        if (attacker != null && !attackerIsBoss && target.hasEffect(ObjectManager.holder(repulsion))) {
            double knockback = target.getEffect(ObjectManager.holder(repulsion)).getAmplifier() + 2;
            double xDist = attacker.position().x() - target.position().x();
            double zDist = attacker.position().z() - target.position().z();
            double xzDist = SchismMath.horizontalDistanceAtLeast(xDist, zDist, 0.01D);
            double motionCap = 10;
            double xVel = xDist / xzDist * knockback;
            double zVel = zDist / xzDist * knockback;
            if (attacker.getDeltaMovement().x() < motionCap && attacker.getDeltaMovement().x() > -motionCap && attacker.getDeltaMovement().z() < motionCap && attacker.getDeltaMovement().z() > -motionCap) {
                attacker.push(xVel, 0, zVel);
                attacker.hurtMarked = true;
            }
        }
    }


    public static void logBiomeAt(Level world, BlockPos pos) {
        Identifier biomeId = world
                .getBiome(pos)
                .unwrapKey()
                .map(key -> key.identifier())
                .orElse(null);

        LMHelperClass.logInfo(
                "Dungeon",
                "[BiomeDebug] World biome at " + pos + " is " + biomeId
        );
    }

    /**
     * Effects are registered after this listener is constructed, so resolve lazily and keep
     * retrying until at least one resolves.
     */
    private void resolveTickEffects() {
        if (this.tickEffectsResolved) {
            return;
        }
        this.paralysisEffect = ObjectManager.getEffect("paralysis");
        this.weightEffect = ObjectManager.getEffect("weight");
        this.fearEffect = ObjectManager.getEffect("fear");
        this.instabilityEffect = ObjectManager.getEffect("instability");
        this.plagueEffect = ObjectManager.getEffect("plague");
        this.smitedEffect = ObjectManager.getEffect("smited");
        this.bleedEffect = ObjectManager.getEffect("bleed");
        this.smoulderingEffect = ObjectManager.getEffect("smouldering");
        this.swiftswimmingEffect = ObjectManager.getEffect("swiftswimming");
        this.immunizationEffect = ObjectManager.getEffect("immunization");
        this.cleansedEffect = ObjectManager.getEffect("cleansed");
        this.tickEffectsResolved = this.paralysisEffect != null || this.weightEffect != null
                || this.fearEffect != null || this.instabilityEffect != null
                || this.plagueEffect != null || this.smitedEffect != null
                || this.bleedEffect != null || this.smoulderingEffect != null
                || this.swiftswimmingEffect != null || this.immunizationEffect != null
                || this.cleansedEffect != null;
    }

    /** Cheap gate: skip the whole per-effect block for entities carrying none of ours. */
    private boolean hasLycanitesTickEffect(LivingEntity entity) {
        return this.hasEffect(entity, this.paralysisEffect)
                || this.hasEffect(entity, this.weightEffect)
                || this.hasEffect(entity, this.fearEffect)
                || this.hasEffect(entity, this.instabilityEffect)
                || this.hasEffect(entity, this.plagueEffect)
                || this.hasEffect(entity, this.smitedEffect)
                || this.hasEffect(entity, this.bleedEffect)
                || this.hasEffect(entity, this.smoulderingEffect)
                || this.hasEffect(entity, this.immunizationEffect)
                || this.hasEffect(entity, this.cleansedEffect);
    }

    private boolean hasEffect(LivingEntity entity, EffectBase effect) {
        // EffectBase is the mod-side effect; vanilla wants a Holder<MobEffect>.
        return effect != null && entity.hasEffect(ObjectManager.holder(effect));
    }
}
