package com.lycanitesmobs.core.entity.base;

import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.data.info.creature.CreatureType;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.entity.damagesources.MinionEntityDamageSource;
import com.lycanitesmobs.core.entity.goals.actions.BegGoal;
import com.lycanitesmobs.core.entity.goals.actions.FollowOwnerGoal;
import com.lycanitesmobs.core.entity.goals.actions.StayGoal;
import com.lycanitesmobs.core.entity.goals.targeting.CopyOwnerAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendOwnerGoal;
import com.lycanitesmobs.core.entity.goals.targeting.RevengeOwnerGoal;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.item.consumable.entity.ChargeItem;
import com.lycanitesmobs.core.item.consumable.entity.CreatureTreatItem;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulstone;
import com.lycanitesmobs.core.manager.CreatureManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public abstract class TameableCreatureEntity extends AgeableCreatureEntity {
    // Stats:
    protected float hunger = this.getCreatureHungerMax();
    protected float stamina = this.getStaminaMax();
    protected float staminaRecovery = 0.5F;
    protected float sittingGuardRange = 16F;

    // Owner:
    protected UUID ownerUUID;

    // Datawatcher:
    protected static final EntityDataAccessor<Byte> TAMED = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.BYTE); // TODO Tame Type IDs.
    // 26.x: OPTIONAL_UUID serializer was replaced by entity references.
    protected static final EntityDataAccessor<Optional<net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity>>> OWNER_ID = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    protected static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> STAMINA = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> LOYALTY = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.INT);

    protected TameableCreatureEntity(EntityType<? extends AgeableCreatureEntity> entityType, Level world) {
        super(entityType, world);
    }

    /**
     * Used for the TAMED WATCHER_ID, this holds a series of booleans that describe the tamed status as well as instructed behaviour.
     **/
    public static enum TAMED_ID {
        IS_TAMED((byte) 1), MOVE_SIT((byte) 2), MOVE_FOLLOW((byte) 4),
        STANCE_PASSIVE((byte) 8), STANCE_AGGRESSIVE((byte) 16), STANCE_ASSIST((byte) 32), PVP((byte) 64);
        public final byte id;

        TAMED_ID(byte value) {
            this.id = value;
        }

        public byte getValue() {
            return id;
        }
    }

    // ==================================================
    //                    Constructor
    // ==================================================
	/*protected TameableCreatureEntity(EntityType<? extends TameableCreatureEntity> entityType, Level world) {
		super(entityType, world);
	}
	*/
    // ========== Init ==========
    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TAMED, (byte) 0);
        builder.define(OWNER_ID, Optional.empty());
        builder.define(HUNGER, this.getCreatureHungerMax());
        builder.define(STAMINA, this.getStaminaMax());
        // Same strict-Builder issue as BABY: without this every Tameable creature fails to construct.
        builder.define(LOYALTY, 0);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        // Greater Targeting:
        this.targetSelector.addGoal(this.claimReactTargetGoalIndex(), new RevengeOwnerGoal(this));
        this.targetSelector.addGoal(this.claimReactTargetGoalIndex(), new CopyOwnerAttackTargetGoal(this));

        // Greater Actions:
        this.goalSelector.addGoal(this.claimTravelGoalIndex(), new StayGoal(this));

        super.registerGoals();

        // Lesster Targeting:
        this.targetSelector.addGoal(this.claimSpecialTargetGoalIndex(), new DefendOwnerGoal(this));

        // Lesser Actions:
        this.goalSelector.addGoal(this.claimTravelGoalIndex(), new FollowOwnerGoal(this).setStrayDistance(CreatureManager.getInstance().getConfig().petFollowDistance()).setLostDistance(32).setSpeed(1D));
        this.goalSelector.addGoal(this.claimIdleGoalIndex(), new BegGoal(this));
    }

    // ========== Name ==========
    @Override
    public MutableComponent getName() {
        if (!this.isTamed() || !CreatureManager.getInstance().getConfig().ownerTags()) {
            return super.getName().copy();
        }

        MutableComponent ownedName = Component.literal("");
        boolean customName = this.hasCustomName();
        if (customName) {
            ownedName.append(super.getName()).append(" (");
        }

        MutableComponent ownerName = this.getOwnerName().copy();
        String ownerSuffix = "'s ";
        String ownerFormatted = ownerName.getString();
        if (ownerFormatted.length() > 0) {
            if ("s".equalsIgnoreCase(ownerFormatted.substring(ownerFormatted.length() - 1)))
                ownerSuffix = "' ";
        }
        ownedName.append(ownerName).append(ownerSuffix).append(this.getFullName());
        if (customName) {
            ownedName.append(")");
        }

        return ownedName;
    }


    // ==================================================
    //                     Spawning
    // ==================================================
    // ========== Despawning ==========
    @Override
    protected boolean canDespawnNaturally() {
        if (this.isTamed()) {
            return false;
        }
        return super.canDespawnNaturally();
    }

    @Override
    public boolean despawnCheck() {
        if (this.level().isClientSide())
            return false;

        // Bound Pet:
        if (this.getPetEntry() != null) {
            if (this.getPetEntry().getEntity() != this && this.getPetEntry().getEntity() != null)
                return true;
            if (this.getPetEntry().getOwner() == null || !this.getPetEntry().getOwner().isAlive()) {
                this.getPetEntry().saveEntityNBT();
                return true;
            }
        }

        if (this.isTamed() && !this.isTemporary())
            return false;
        return super.despawnCheck();
    }

    @Override
    public boolean isPersistant() {
        return this.isTamed() || super.isPersistant();
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Can leash ==========
    @Override
    public boolean canBeLeashed() {
        if (this.isTamed()) {
            return true;
        }
        return super.canBeLeashed();
    }

    // ========== Test Leash ==========
    @Override
    public void testLeash(float distance) {
        if (this.isSitting() && distance > 10.0F)
            this.dropLeash();
        else
            super.testLeash(distance);
    }


    // ==================================================
    //                       Update
    // ==================================================
    @Override
    public void aiStep() {
        super.aiStep();
        this.staminaUpdate();

        // Owner Buffs:
        this.updateOwnerEffects();
    }

    public void staminaUpdate() {
        if (this.level().isClientSide())
            return;
        if (this.stamina < this.getStaminaMax() && this.staminaRecovery >= this.getStaminaRecoveryMax() / 2)
            this.setStamina(Math.min(this.stamina + this.staminaRecovery, this.getStaminaMax()));
        if (this.staminaRecovery < this.getStaminaRecoveryMax())
            this.staminaRecovery = Math.min(this.staminaRecovery + (this.getStaminaRecoveryMax() / this.getStaminaRecoveryWarmup()), this.getStaminaRecoveryMax());
    }

    /**
     * Grants constant effects to the owner if the owner is nearby.
     */
    public void ownerEffects() {
        Player owner = this.getPlayerOwner();
        if (owner == null) {
            return;
        }
        this.ownerEffects(owner);
    }

    private void updateOwnerEffects() {
        if (this.level().isClientSide()) {
            return;
        }
        if (!this.isPet()) {
            return;
        }

        Player owner = this.getPlayerOwner();
        if (owner == null) {
            return;
        }
        if (this.distanceToSqr(owner) > 64) {
            return;
        }

        this.ownerEffects(owner);
    }

    private void ownerEffects(Player owner) {
        if (!this.canBurn()) {
            owner.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (5 * 20) + 5, 1));
        }
        for (Object possibleEffect : owner.getActiveEffects().toArray(new Object[0])) {
            if (possibleEffect instanceof MobEffectInstance effectInstance && !this.canBeAffected(effectInstance)) {
                owner.removeEffect(effectInstance.getEffect());
            }
        }
    }

    /**
     * Returns true if the is a standard pet and is tamed with a player owner, not a minion, mount, familiar, etc. Does not check if soulbound.
     *
     * @return True if a standard pet.
     */
    public boolean isPet() {
        if (!this.isTamed() || this.getPlayerOwner() == null) {
            return false;
        }
        if (this.creatureInfo.isMountable()) {
            return false;
        }
        if (this.isTemporary()) {
            return false;
        }
        return this.getPetEntry() == null || this.isPetType("pet");
    }

    /**
     * Copies this creature's behaviour to the provided tamed creature.
     *
     * @return The creature to copy pet behaviour to.
     */
    public void copyPetBehaviourTo(TameableCreatureEntity target) {
        target.setPVP(this.isPVP());
        target.setPassive(this.isPassive());
        target.setAssist(this.isAssisting());
        target.setAggressive(this.isAggressive());

        target.setSitting(this.isSitting());
        target.setFollowing(this.isFollowing());
    }


    // ==================================================
    //                       Perching
    // ==================================================
    public boolean canPerch(LivingEntity target) {
        if (!this.creatureInfo.isPerchable()) {
            return false;
        }
        return this.getPlayerOwner() == target;
    }


    // ==================================================
    //                       Interact
    // ==================================================
    // ========== Get Interact Commands ==========
    @Override
    public HashMap<Integer, String> getInteractCommands(Player player, @Nonnull ItemStack itemStack) {
        HashMap<Integer, String> commands = new HashMap<>();
        commands.putAll(super.getInteractCommands(player, itemStack));
        this.addTameableInteractCommands(commands, player, itemStack);
        return commands;
    }

    // ========== Perform Command ==========
    @Override
    public boolean performCommand(String command, Player player, ItemStack itemStack, InteractionHand hand) {
        Boolean result = this.performTameableCommand(command, player, itemStack);
        if (result != null) {
            return result;
        }

        return super.performCommand(command, player, itemStack, hand);
    }

    private void addTameableInteractCommands(HashMap<Integer, String> commands, Player player, ItemStack itemStack) {
        if (this.canPerch(player) && !player.isShiftKeyDown() && !this.level().isClientSide()) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.MAIN.id, "Perch");
        }
        else if (!this.level().isClientSide() && player.isShiftKeyDown() && this.isTamed() && (itemStack.isEmpty() || player.isShiftKeyDown()) && player == this.getPlayerOwner()) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.MAIN.id, "GUI");
        }

        if (this.level().isClientSide() || itemStack.isEmpty() || player.isShiftKeyDown()) {
            return;
        }

        if (!this.isTamed() && this.isTamingItem(itemStack) && CreatureManager.getInstance().getConfig().tamingEnabled()) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.IMPORTANT.id, "Tame");
        }

        if (this.isTamed() && this.isHealingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Feed");
        }

        if (this.isTamed() && !this.isTemporary() && !this.isPetType("minion") && itemStack.getItem() instanceof ChargeItem) {
            if (this.isLevelingChargeItem(itemStack)) {
                commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Charge");
            }
            else {
                player.sendSystemMessage(Component.translatable("item.lycanitesmobs.charge.creature.invalid"));
            }
        }

        if (this.isTamed() && !this.isBaby() && this.canEquip() && player == this.getPlayerOwner()) {
            String equipSlot = this.inventory.getSlotForEquipment(itemStack);
            if (equipSlot != null && (this.inventory.getEquipmentStack(equipSlot) == null || this.inventory.getEquipmentStack(equipSlot).getItem() != itemStack.getItem())) {
                commands.put(BaseCreatureEntity.COMMAND_PIORITIES.EQUIPPING.id, "Equip Item");
            }
        }

        if (itemStack.getItem() instanceof ItemSoulstone && this.isTamed()) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Soulstone");
        }
    }

    private Boolean performTameableCommand(String command, Player player, ItemStack itemStack) {
        if ("GUI".equals(command)) {
            this.playTameSound();
            this.openGUI(player);
            return true;
        }

        if ("Tame".equals(command)) {
            this.tame(player);
            this.consumePlayersItem(player, itemStack);
            return true;
        }

        if ("Feed".equals(command)) {
            this.heal((float) this.getHealAmount(itemStack));
            this.playEatSound();
            this.spawnFeedParticles();
            this.consumePlayersItem(player, itemStack);
            return true;
        }

        if ("Charge".equals(command)) {
            this.addExperience(this.getExperienceFromChargeItem(itemStack));
            this.consumePlayersItem(player, itemStack);
            return true;
        }

        if ("Equip Item".equals(command)) {
            this.equipHeldItem(itemStack);
            this.consumePlayersItem(player, itemStack);
            return true;
        }

        if ("Soulstone".equals(command)) {
            return false;
        }

        if ("Sit".equals(command)) {
            this.playTameSound();
            this.setTarget(null);
            this.clearMovement();
            this.setSitting(!this.isSitting());
            this.clearJumpingState();
            return true;
        }

        if ("Perch".equals(command)) {
            this.playTameSound();
            this.perchOnEntity(player);
            return true;
        }

        return null;
    }

    private int getHealAmount(ItemStack itemStack) {
        if (itemStack.has(net.minecraft.core.component.DataComponents.FOOD)) {
            return itemStack.get(net.minecraft.core.component.DataComponents.FOOD).nutrition();
        }
        return 4;
    }

    private void spawnFeedParticles() {
        if (!this.level().isClientSide()) {
            return;
        }

        ParticleOptions particle = ParticleTypes.HEART;
        double d0 = this.getRandom().nextGaussian() * 0.02D;
        double d1 = this.getRandom().nextGaussian() * 0.02D;
        double d2 = this.getRandom().nextGaussian() * 0.02D;
        for (int i = 0; i < 25; i++) {
            this.level().addParticle(
                    particle,
                    this.position().x() + (double) (this.getRandom().nextFloat() * this.getDimensions(Pose.STANDING).width() * 2.0F) - (double) this.getDimensions(Pose.STANDING).width(),
                    this.position().y() + 0.5D + (double) (this.getRandom().nextFloat() * this.getDimensions(Pose.STANDING).height()),
                    this.position().z() + (double) (this.getRandom().nextFloat() * this.getDimensions(Pose.STANDING).width() * 2.0F) - (double) this.getDimensions(Pose.STANDING).width(),
                    d0,
                    d1,
                    d2
            );
        }
    }

    private void equipHeldItem(ItemStack itemStack) {
        String equipSlot = this.inventory.getSlotForEquipment(itemStack);
        ItemStack equippedItem = this.inventory.getEquipmentStack(equipSlot);
        if (equippedItem != null) {
            this.dropItem(equippedItem);
        }
        ItemStack equipStack = itemStack.copy();
        equipStack.setCount(1);
        this.inventory.setEquipmentStack(equipStack.copy());
    }

    // ========== Can Name Tag ==========
    @Override
    public boolean canNameTag(Player player) {
        if (!this.isTamed()) {
            return super.canNameTag(player);
        }
        if (player == this.getPlayerOwner()) {
            return super.canNameTag(player);
        }
        return false;
    }

    // ========== Perform GUI Command ==========
    @Override
    public void performGUICommand(Player player, int guiCommandID) {
        if (!this.petControlsEnabled()) {
            return;
        }
        if (player != this.getOwner()) {
            return;
        }

        if (guiCommandID == PET_COMMAND_ID.PVP.id) {
            this.setPVP(!this.isPVP());
        } else if (guiCommandID == PET_COMMAND_ID.PASSIVE.id) {
            this.setPassive(true);
        } else if (guiCommandID == PET_COMMAND_ID.DEFENSIVE.id) {
            this.setPassive(false);
            this.setAssist(false);
            this.setAggressive(false);
        } else if (guiCommandID == PET_COMMAND_ID.ASSIST.id) {
            this.setPassive(false);
            this.setAssist(true);
            this.setAggressive(false);
        } else if (guiCommandID == PET_COMMAND_ID.AGGRESSIVE.id) {
            this.setPassive(false);
            this.setAssist(true);
            this.setAggressive(true);
        } else if (guiCommandID == PET_COMMAND_ID.FOLLOW.id) {
            this.setSitting(false);
            this.setFollowing(true);
        } else if (guiCommandID == PET_COMMAND_ID.WANDER.id) {
            this.setSitting(false);
            this.setFollowing(false);
        } else if (guiCommandID == PET_COMMAND_ID.SIT.id) {
            this.setSitting(true);
            this.setFollowing(false);
        }

        if (this.petEntry != null && this.petEntry.getSummonSet() != null) {
            this.petEntry.getSummonSet().updateBehaviour(this);
        }

        super.performGUICommand(player, guiCommandID);
    }


    // ==================================================
    //                       Targets
    // ==================================================
    // ========== Teams ==========
    @Override
    public net.minecraft.world.scores.PlayerTeam getTeam() {
        if (this.isTamed()) {
            Entity owner = this.getOwner();
            if (owner != null) {
                return owner.getTeam();
            }
        }
        return super.getTeam();
    }

    /**
     * Returns if this creature is on the same team as the target entity. If PvP is disabled and this creature is tamed then it is considered on the same team as all players and their tames.
     *
     * @param target The entity to check teams with.
     * @return True when on the same team.
     */
    @Override
    protected boolean considersEntityAsAlly(Entity target) {
        if (target == null) return false;
        if (this.level().isClientSide() || !this.isTamed()) {
            return super.considersEntityAsAlly(target);
        }

        if (target == this.getPlayerOwner() || target == this.getOwner()) {
            return true;
        }

        if (target instanceof Player && (!this.isServerPvpAllowed() || !this.isPVP())) {
            return true;
        }

        if (target instanceof TameableCreatureEntity tamedTarget) {
            if (tamedTarget.isTamed() && (!this.isServerPvpAllowed() || !this.isPVP() || tamedTarget.getPlayerOwner() == this.getPlayerOwner())) {
                return true;
            }
        }
        else if (target instanceof OwnableEntity tamedTarget) {
            if (tamedTarget.getOwner() != null && (!this.isServerPvpAllowed() || !this.isPVP() || tamedTarget.getOwner() == this.getOwner())) {
                return true;
            }
        }

        Entity owner = this.getPlayerOwner() != null ? this.getPlayerOwner() : this.getOwner();
        if (owner == null) {
            return false;
        }
        if (owner.getVehicle() == target) {
            return true;
        }
        return owner.isAlliedTo(target);
    }


    // ==================================================
    //                       Attacks
    // ==================================================
    @Override
    public boolean doRangedDamage(Entity target, ThrowableProjectile projectile, float damage, boolean noPierce) {
        float totalDamage = damage * ((float) this.creatureStats.getDamage() / 2);
        Boolean ownerCredit = this.tryRangedOwnerKillCredit(target, totalDamage);
        if (ownerCredit != null) {
            return ownerCredit;
        }

        return super.doRangedDamage(target, projectile, damage, noPierce);
    }

    @Override
    public boolean attackEntityAsMob(Entity target, double damageScale) {
        if (!this.isAlive()) {
            return false;
        }
        if (target == null) {
            return false;
        }
        if (!this.hasLineOfSight(target)) {
            return false;
        }

        float totalDamage = this.getAttackDamage(damageScale);
        Boolean ownerCredit = this.tryMeleeOwnerKillCredit(target, totalDamage);
        if (ownerCredit != null) {
            return ownerCredit;
        }

        return super.attackEntityAsMob(target, damageScale);
    }

    private Boolean tryRangedOwnerKillCredit(Entity target, float totalDamage) {
        if (!(target instanceof Mob mobTarget)) {
            return null;
        }
        if (!(this.getOwner() instanceof Player)) {
            return null;
        }
        if (mobTarget.getHealth() <= 0 || mobTarget.getHealth() - totalDamage > 0) {
            return null;
        }

        DamageSource creditSource = new MinionEntityDamageSource(this.getDamageSource(null).typeHolder(), this.getOwner());
        return target.hurtOrSimulate(creditSource, totalDamage);
    }

    private Boolean tryMeleeOwnerKillCredit(Entity target, float totalDamage) {
        if (!(target instanceof Mob mobTarget)) {
            return null;
        }
        if (!(this.getOwner() instanceof Player)) {
            return null;
        }
        if (mobTarget.getHealth() <= 0 || mobTarget.getHealth() - totalDamage > 0) {
            return null;
        }

        DamageSource creditSource = new MinionEntityDamageSource(this.getDamageSource(null).typeHolder(), this.getOwner());
        creditSource.is(DamageTypes.MAGIC);
        return target.hurtOrSimulate(creditSource, totalDamage);
    }

    // ========== Can Attack ==========
    @Override
    public boolean canAttackType(EntityType targetType) {
        if (this.isPassive()) {
            return false;
        }
        if (this.isTamed()) {
            return true;
        }
        return super.canAttackType(targetType);
    }

    @Override
    public boolean canAttack(LivingEntity targetEntity) {
        if (this.isPassive()) {
            return false;
        }
        if (!this.isTamed()) {
            return super.canAttack(targetEntity);
        }

        if (this.getOwner() == targetEntity || this.getPlayerOwner() == targetEntity) {
            return false;
        }
        if (!this.level().isClientSide()) {
            boolean canPVP = this.isServerPvpAllowed() && this.isPVP();
            if (targetEntity instanceof Player && !canPVP) {
                return false;
            }
            if (targetEntity instanceof TameableCreatureEntity targetTameable && targetTameable.isTamed()) {
                if (!canPVP) {
                    return false;
                }
                if (targetTameable.getPlayerOwner() == this.getPlayerOwner()) {
                    return false;
                }
            }
        }
        return true;
    }

    // ========= Get Damage Source ==========

    /**
     * Returns the damage source to be used by this mob when dealing damage.
     *
     * @param nestedDamageSource This can be null or can be a passed entity damage source for all kinds of use, mainly for minion damage sources.
     * @return
     */
    @Override
    public DamageSource getDamageSource(DamageSource nestedDamageSource) {
//        if(this.isTamed() && this.getOwner() != null) {
//            if(nestedDamageSource == null)
//                nestedDamageSource = (EntityDamageSource)DamageSource.mobAttack(this);
//            return new MinionEntityDamageSource(nestedDamageSource, this.getOwner());
//        }
        return super.getDamageSource(nestedDamageSource);
    }

    // ========== Attacked From ==========
    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel lycLevel, DamageSource damageSrc, float damageAmount) {
        if (com.lycanitesmobs.core.util.helpers.LMHelperClass.isInvulnerableTo(this, damageSrc)) {
            return false;
        }

        if (!this.isPassive()) {
            this.setSitting(false);
        }

        Entity entity = damageSrc.getDirectEntity();
        if (entity instanceof ThrowableProjectile projectile) {
            entity = projectile.getOwner();
        }

        if (this.isTamed() && this.getOwner() == entity) {
            return false;
        }

        return super.hurtServer(lycLevel, damageSrc, damageAmount);
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    // ========== Damage ==========

    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel lycLevel, DamageSource source) {
        Entity entity = source.getEntity();

        if (this.blocksOwnerOrPlayerDamage(entity)) {
            return true;
        }

        if (source.is(DamageTypes.IN_WALL) && this.isTamed()) {
            return true;
        }

        return super.isInvulnerableTo(lycLevel, source);
    }

    private boolean isServerPvpAllowed() {
        return com.lycanitesmobs.core.util.helpers.LMHelperClass.getGameRuleBool(this.level(), net.minecraft.world.level.gamerules.GameRules.PVP, true);
    }

    private boolean blocksOwnerOrPlayerDamage(Entity entity) {
        if (!this.isTamed()) {
            return false;
        }
        if (entity instanceof Player && !this.isServerPvpAllowed()) {
            return true;
        }
        return entity == this.getPlayerOwner();
    }


    // ==================================================
    //                       Owner
    // ==================================================

    /**
     * Sets the owner of this entity via the unique id of the owner entity. Also updates the tamed status of this entity.
     *
     * @param ownerUUID The owner entity UUID.
     */
    public void setOwnerId(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.getEntityData().set(OWNER_ID, ownerUUID == null ? Optional.empty() : Optional.of(net.minecraft.world.entity.EntityReference.of(ownerUUID)));
        this.setTamed(ownerUUID != null);
    }

    @Override
    public Entity getOwner() {
        UUID uuid = this.getOwnerId();
        if (uuid == null) {
            return this.superGetOwner();
        }
        return this.level().getPlayerByUUID(uuid);
    }

    /**
     * Sets the owner of this entity to the provided player entity. Also updates the tamed status of this entity.
     *
     * @param player The player to become the owner.
     */
    public void setPlayerOwner(Player player) {
        this.setOwnerId(player.getUUID());
    }

    @Override
    public UUID getOwnerId() {
        if (this.level().isClientSide()) {
            try {
                return this.getEntityData().get(OWNER_ID).map(net.minecraft.world.entity.EntityReference::getUUID).orElse(null);
            }
            catch (Exception ignored) {
            }
        }
        return this.ownerUUID;
    }

    /**
     * Returns the owner of this entity as a player or null if there is no player owner. This is separated from getOwner and getOwnerId as they behave inconsistently when built.
     *
     * @return The player owner.
     */
    public Player getPlayerOwner() {
        if (this.level().isClientSide()) {
            Entity owner = this.getOwner();
            if (owner instanceof Player player) {
                return player;
            }
            return null;
        }
        if (this.ownerUUID == null) {
            return null;
        }
        return this.level().getPlayerByUUID(this.ownerUUID);
    }

    /**
     * Gets the display name of the entity that owns this entity or an empty string if none. TODO Maybe this hack is no longer needed now.
     *
     * @return The owner display name.
     */
    public Component getOwnerName() {
        Entity owner = this.getOwner();
        if (owner != null) {
            return owner.getDisplayName();
        }
        return Component.literal("");
    }


    // ==================================================
    //                       Taming
    // ==================================================
    @Override
    public boolean isTamed() {
        try {
            return (this.getEntityData().get(TAMED) & TAMED_ID.IS_TAMED.id) != 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void setTamed(boolean isTamed) {
        byte tamed = this.behaviourBitMask();
        if (isTamed) {
            this.getEntityData().set(TAMED, (byte) (tamed | TAMED_ID.IS_TAMED.id));
            this.clearSpawnEventTracking();
        }
        else {
            this.getEntityData().set(TAMED, (byte) (tamed - (tamed & TAMED_ID.IS_TAMED.id)));
        }
        this.setCustomNameVisible(isTamed);
    }

    public boolean isTamingItem(ItemStack itemstack) {
        CreatureType creatureType = this.creatureInfo.getCreatureType();
        if (itemstack.isEmpty() || creatureType == null || this.isBoss()) {
            return false;
        }

        if (itemstack.getItem() instanceof CreatureTreatItem itemTreat && itemTreat.getCreatureType() == creatureType) {
            return this.creatureInfo.isTameable();
        }

        return false;
    }

    // ========== Tame Entity ==========

    /**
     * Attempts to tame this entity to the provided player.
     *
     * @param player The player taming this entity.
     * @return True if the entity is tamed, false on failure.
     */
    public boolean tame(Player player) {
        if (this.level().isClientSide() || this.isRareVariant() || this.isBoss()) {
            return super.isTamed();
        }

        ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
        if (extendedPlayer == null) {
            return this.isTamed();
        }

        extendedPlayer.studyCreature(this, CreatureManager.getInstance().getConfig().creatureTreatKnowledge(), true, true);

        if (this.isTamed()) {
            return true;
        }

        CreatureKnowledge creatureKnowledge = extendedPlayer.getBeastiary().getCreatureKnowledge(this.creatureInfo.getName());
        if (creatureKnowledge == null || creatureKnowledge.getRank() < 2) {
            return this.isTamed();
        }

        CreatureRelationshipEntry relationshipEntry = this.relationships.getOrCreateEntry(player);
        int reputationAmount = 50 + this.getRandom().nextInt(50);
        relationshipEntry.increaseReputation(reputationAmount);

        if (this.creatureInfo.isTameable() && relationshipEntry.getReputation() >= this.creatureInfo.getTamingReputation()) {
            this.setPlayerOwner(player);
            this.onTamedByPlayer();
            this.unsetTemporary();
            MutableComponent tameMessage = Component.translatable("message.pet.tamed.prefix")
                    .append(" ")
                    .append(this.getSpeciesName())
                    .append(" ")
                    .append(Component.translatable("message.pet.tamed.suffix"));
            player.sendSystemMessage(tameMessage);
            this.playTameEffect(this.isTamed());
            this.clampPortalTimeAfterTame();
        }

        this.playTameEffect(this.isTamed());
        return this.isTamed();
    }

    /**
     * Called when this creature is first tamed by a player, this clears movement, targets, etc and sets default the pet behaviour.
     */
    public void onTamedByPlayer() {
        this.refreshAttributes();
        this.clearMovement();
        this.setTarget(null);
        this.setSitting(false);
        this.setFollowing(true);
        this.setPassive(false);
        this.setAggressive(false);
        this.setPVP(true);
        this.playTameSound();
        this.clearSpawnEventTracking();
    }

    @Override
    public void onCreateBaby(AgeableCreatureEntity partner, AgeableCreatureEntity baby) {
        if (this.isTamed() && this.getOwner() instanceof Player && partner instanceof TameableCreatureEntity && baby instanceof TameableCreatureEntity) {
            TameableCreatureEntity partnerTameable = (TameableCreatureEntity) partner;
            TameableCreatureEntity babyTameable = (TameableCreatureEntity) baby;
            if (partnerTameable.getPlayerOwner() == this.getPlayerOwner()) {
                babyTameable.setPlayerOwner((Player) this.getOwner());
            }
        }
        super.onCreateBaby(partner, baby);
    }


    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte status) {
        if (status == 7)
            this.playTameEffect(true);
        else if (status == 6)
            this.playTameEffect(false);
        else
            super.handleEntityEvent(status);
    }

    /**
     * Determines if the provided itemstack can be consumed to heal this entity.
     *
     * @param itemStack The possible healing itemstack.
     * @return True if this entity should eat the itemstack and heal.
     */
    public boolean isHealingItem(ItemStack itemStack) {
        return this.creatureInfo.canEat(itemStack);
    }

    /**
     * Determines if the provided itemstack can be consumed to add experience this entity.
     *
     * @param itemStack The possible leveling itemstack.
     * @return True if this entity should eat the itemstack and gain experience.
     */
    public boolean isLevelingChargeItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ChargeItem chargeItem) {
            for (ElementInfo elementInfo : this.getElements()) {
                if (chargeItem.getElements().contains(elementInfo)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines how much experience the provided charge itemstack can grant this creature.
     *
     * @param itemStack The possible leveling itemstack.
     * @return The amount of experience to gain.
     */
    public int getExperienceFromChargeItem(ItemStack itemStack) {
        int experience = 0;
        if (itemStack.getItem() instanceof ChargeItem chargeItem) {
            for (ElementInfo elementInfo : this.getElements()) {
                if (chargeItem.getElements().contains(elementInfo)) {
                    experience += ChargeItem.CHARGE_EXPERIENCE;
                }
            }
        }
        return experience;
    }


    // ==================================================
    //                      Breeding
    // ==================================================
    @Override
    public boolean isBreedingItem(ItemStack itemStack) {
        if (!this.creatureInfo.isFarmable()) {
            if (!this.isTamed() || this.isPetType("minion") || this.isPetType("familiar") || this.getHealth() < this.getMaxHealth()) {
                return false;
            }
        }
        return super.isBreedingItem(itemStack);
    }


    // ============================================
    //                   Minions
    // ============================================
    @Override
    public void summonMinion(LivingEntity minion, double angle, double distance) {
        if (this.getPlayerOwner() != null && minion instanceof TameableCreatureEntity) {
            ((TameableCreatureEntity) minion).setPlayerOwner(this.getPlayerOwner());
            this.copyPetBehaviourTo((TameableCreatureEntity) minion);
        }
        super.summonMinion(minion, angle, distance);
    }


    // ==================================================
    //                    Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
    }

    public byte behaviourBitMask() {
        return this.getEntityData().get(TAMED);
    }

    // ========== Sitting ==========
    public boolean isSitting() {
        if (!this.isTamed()) {
            return false;
        }
        return this.hasBehaviourBit(TAMED_ID.MOVE_SIT.id);
    }

    public void setSitting(boolean set) {
        if (!this.petControlsEnabled()) {
            set = false;
        }
        if (set) {
            this.enableBehaviourBit(TAMED_ID.MOVE_SIT.id);
            this.setHome((int) this.position().x(), (int) this.position().y(), (int) this.position().z(), this.sittingGuardRange);
        } else {
            this.disableBehaviourBit(TAMED_ID.MOVE_SIT.id);
            this.hasRestriction();
        }
    }

    // ========== Following ==========
    public boolean isFollowing() {
        if (!this.isTamed()) {
            return false;
        }
        if (this.getLeashHolder() instanceof LeashFenceKnotEntity) {
            return false;
        }
        return this.hasBehaviourBit(TAMED_ID.MOVE_FOLLOW.id);
    }

    public void setFollowing(boolean set) {
        if (!this.petControlsEnabled()) {
            set = false;
        }
        if (set) {
            this.enableBehaviourBit(TAMED_ID.MOVE_FOLLOW.id);
        } else {
            this.disableBehaviourBit(TAMED_ID.MOVE_FOLLOW.id);
        }
    }

    // ========== Passiveness ==========
    public boolean isPassive() {
        if (!this.isTamed()) {
            return false;
        }
        return this.hasBehaviourBit(TAMED_ID.STANCE_PASSIVE.id);
    }

    public void setPassive(boolean set) {
        if (!this.petControlsEnabled()) {
            set = false;
        }
        if (set) {
            this.enableBehaviourBit(TAMED_ID.STANCE_PASSIVE.id);
            this.setTarget(null);
            this.setStealth(0);
        } else {
            this.disableBehaviourBit(TAMED_ID.STANCE_PASSIVE.id);
        }
    }

    // ========== Agressiveness ==========
    @Override
    public boolean isAggressive() {
        if (!this.isTamed()) {
            return super.isAggressive();
        }
        return this.hasBehaviourBit(TAMED_ID.STANCE_AGGRESSIVE.id);
    }

    boolean superIsAggressive() {
        return super.isAggressive();
    }

    boolean superIsTamedBase() {
        return super.isTamed();
    }

    boolean superCanBeLeashed(Player player) {
        return super.canBeLeashed();
    }

    boolean superCanNameTag(Player player) {
        return super.canNameTag(player);
    }

    Entity superGetOwner() {
        return super.getOwner();
    }

    Team superGetTeam() {
        return super.getTeam();
    }

    boolean superIsAlliedTo(Entity target) {
        return super.considersEntityAsAlly(target);
    }

    boolean superCanAttackType(EntityType targetType) {
        return super.canAttackType(targetType);
    }

    boolean superCanAttack(LivingEntity targetEntity) {
        return super.canAttack(targetEntity);
    }

    boolean superHurt(DamageSource damageSrc, float damageAmount) {
        return this.level() instanceof net.minecraft.server.level.ServerLevel lycSrv && super.hurtServer(lycSrv, damageSrc, damageAmount);
    }

    boolean superIsInvulnerableTo(DamageSource source) {
        return this.level() instanceof net.minecraft.server.level.ServerLevel lycSrv && super.isInvulnerableTo(lycSrv, source);
    }

    void clearJumpingState() {
        this.jumping = false;
    }

    void clampPortalTimeAfterTame() {
        if (false) { // 1.21: portalTime moved into private PortalProcessor; no external override.
        }
    }

    public void setAggressive(boolean set) {
        if (!this.petControlsEnabled()) {
            set = false;
        }
        if (set) {
            this.enableBehaviourBit(TAMED_ID.STANCE_AGGRESSIVE.id);
        } else {
            this.disableBehaviourBit(TAMED_ID.STANCE_AGGRESSIVE.id);
        }
    }

    // ========== Assist ==========
    public boolean isAssisting() {
        if (!this.isTamed()) {
            return false;
        }
        return this.hasBehaviourBit(TAMED_ID.STANCE_ASSIST.id);
    }

    public void setAssist(boolean set) {
        if (!this.petControlsEnabled()) {
            set = true;
        }
        if (set) {
            this.enableBehaviourBit(TAMED_ID.STANCE_ASSIST.id);
        } else {
            this.disableBehaviourBit(TAMED_ID.STANCE_ASSIST.id);
        }
    }

    // ========== PvP ==========
    public boolean isPVP() {
        return this.hasBehaviourBit(TAMED_ID.PVP.id);
    }

    public void setPVP(boolean set) {
        if (!this.petControlsEnabled()) {
            set = false;
        }
        if (set) {
            this.enableBehaviourBit(TAMED_ID.PVP.id);
        } else {
            this.setTarget(null);
            this.disableBehaviourBit(TAMED_ID.PVP.id);
        }
    }

    private boolean hasBehaviourBit(byte bit) {
        return (this.behaviourBitMask() & bit) != 0;
    }

    private void enableBehaviourBit(byte bit) {
        byte tamedStatus = this.behaviourBitMask();
        this.getEntityData().set(TAMED, (byte) (tamedStatus | bit));
    }

    private void disableBehaviourBit(byte bit) {
        byte tamedStatus = this.behaviourBitMask();
        this.getEntityData().set(TAMED, (byte) (tamedStatus - (tamedStatus & bit)));
    }


    // ==================================================
    //                       Hunger
    // ==================================================
    public float getCreatureHunger() {
        if (this.level() == null)
            return this.getCreatureHungerMax();
        if (!this.level().isClientSide())
            return this.hunger;
        else {
            try {
                return this.getFloatFromDataManager(HUNGER);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    public void setCreatureHunger(float setHunger) {
        this.hunger = setHunger;
    }

    public float getCreatureHungerMax() {
        return 20;
    }


    // ==================================================
    //                       Stamina
    // ==================================================
    public float getStamina() {
        if (this.level() != null && this.level().isClientSide()) {
            try {
                this.stamina = this.getFloatFromDataManager(STAMINA);
            } catch (Exception e) {
            }
        }
        return this.stamina;
    }

    public void setStamina(float setStamina) {
        this.stamina = setStamina;
        if (this.level() != null && !this.level().isClientSide()) {
            this.entityData.set(STAMINA, setStamina);
        }
    }

    public float getStaminaMax() {
        return 100;
    }

    public float getStaminaRecoveryMax() {
        return 1F;
    }

    public int getStaminaRecoveryWarmup() {
        return 10 * 20;
    }

    public float getStaminaCost() {
        return 1;
    }

    public void applyStaminaCost() {
        float newStamina = this.getStamina() - this.getStaminaCost();
        if (newStamina < 0)
            newStamina = 0;
        this.setStamina(newStamina);
        this.staminaRecovery = 0;
    }

    // ========== GUI Feedback ==========
    public float getStaminaPercent() {
        return this.getStamina() / this.getStaminaMax();
    }

    // "energy" = Usual blue-orange bar. "toggle" = Solid purple bar for on and off.
    public String getStaminaType() {
        return "energy";
    }


    // ==================================================
    //                      Breeding
    // ==================================================
    // ========== Create Child ==========
    @Override
    public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
        AgeableCreatureEntity spawnedBaby = super.createChild(partner);
        if (this.getOwnerId() != null && spawnedBaby instanceof TameableCreatureEntity) {
            ((TameableCreatureEntity) spawnedBaby).setOwnerId(this.getOwnerId());
        }
        return spawnedBaby;
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    /*@Override
    public boolean shouldDismountInWater(Entity rider) { TODO Possibly in EntityType as some obfuscated method?
        return false;
    }*/

    @Override
    public boolean canBeControlledByRider() {
        return this.isTamed();
    }


    // ==================================================
    //                       Client
    // ==================================================
    protected void playTameEffect(boolean success) {
        ParticleOptions particle = ParticleTypes.HEART;
        if (!success)
            particle = ParticleTypes.SMOKE;

        for (int i = 0; i < 7; ++i) {
            double d0 = this.getRandom().nextGaussian() * 0.02D;
            double d1 = this.getRandom().nextGaussian() * 0.02D;
            double d2 = this.getRandom().nextGaussian() * 0.02D;
            this.level().addParticle(particle, this.position().x() + (double) (this.getRandom().nextFloat() * this.getDimensions(Pose.STANDING).width() * 2.0F) - (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + 0.5D + (double) (this.getRandom().nextFloat() * this.getDimensions(Pose.STANDING).height()), this.position().z() + (double) (this.getRandom().nextFloat() * this.getDimensions(Pose.STANDING).width() * 2.0F) - (double) this.getDimensions(Pose.STANDING).width(), d0, d1, d2);
        }
    }


    // ==================================================
    //                       Visuals
    // ==================================================
    // ========== Coloring ==========

    /**
     * Returns true if this mob can be dyed different colors. Usually for wool and collars.
     *
     * @param player The player to check for when coloring, this is to stop players from dying other players pets. If provided with null it should return if this creature can be dyed in general.
     */
    @Override
    public boolean canBeColored(Player player) {
        if (player == null) return true;
        return this.isTamed() && player == this.getPlayerOwner();
    }


    // ========== Boss Health Bar ==========
    public boolean showBossInfo() {
        if (this.isTamed())
            return false;
        return super.showBossInfo();
    }


    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        // UUID NBT:
        if (nbt.read("OwnerId", net.minecraft.core.UUIDUtil.CODEC).isPresent()) {
            this.setOwnerId(nbt.read("OwnerId", net.minecraft.core.UUIDUtil.CODEC).orElse(null));
        } else {
            this.setOwnerId(null);
        }

        this.readPetControlFlags(nbt);

        if (nbt.contains("Hunger")) {
            this.setCreatureHunger(nbt.getFloatOr("Hunger", 0.0F));
        } else {
            this.setCreatureHunger(this.getCreatureHungerMax());
        }

        if (nbt.contains("Stamina")) {
            this.setStamina(nbt.getFloatOr("Stamina", 0.0F));
        }
    }

    // ========== Write ==========
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.getOwnerId() != null) {
            nbt.store("OwnerId", net.minecraft.core.UUIDUtil.CODEC, this.getOwnerId());
        }
        this.writePetControlFlags(nbt);
        nbt.putFloat("Hunger", this.getCreatureHunger());
        nbt.putFloat("Stamina", this.getStamina());
    }

    private void readPetControlFlags(CompoundTag nbt) {
        this.setSitting(this.readPetControlFlag(nbt, "Sitting", false));
        this.setFollowing(this.readPetControlFlag(nbt, "Following", true));
        this.setPassive(this.readPetControlFlag(nbt, "Passive", false));
        this.setAggressive(this.readPetControlFlag(nbt, "Aggressive", false));
        this.setPVP(this.readPetControlFlag(nbt, "PVP", true));
    }

    private boolean readPetControlFlag(CompoundTag nbt, String key, boolean fallback) {
        return nbt.getBooleanOr(key, fallback);
    }

    private void writePetControlFlags(CompoundTag nbt) {
        nbt.putBoolean("Sitting", this.isSitting());
        nbt.putBoolean("Following", this.isFollowing());
        nbt.putBoolean("Passive", this.isPassive());
        nbt.putBoolean("Aggressive", this.isAggressive());
        nbt.putBoolean("PVP", this.isPVP());
    }


    // ==================================================
    //                       Sounds
    // ==================================================
    // ========== Idle ==========

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     **/
    @Override
    public int getAmbientSoundInterval() {
        if (this.isTamed())
            return 600;
        return super.getAmbientSoundInterval();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        String sound = "_say";
        if (this.isTamed() && this.getHealth() < this.getMaxHealth())
            sound = "_beg";
        return ObjectManager.getSound(this.getSoundName() + sound);
    }


    // ========== Tame ==========
    public void playTameSound() {
        this.playSound(ObjectManager.getSound(this.getSoundName() + "_tame"), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // ========== Eat ==========
    public void playEatSound() {
        this.playSound(ObjectManager.getSound(this.getSoundName() + "_eat"), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}
