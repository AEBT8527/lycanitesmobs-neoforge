package com.lycanitesmobs.core.item.summoningstaff;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.special.PortalEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.item.base.BaseItem;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import com.lycanitesmobs.core.network.packet.MessageScreenRequest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public class ItemStaffSummoning extends BaseItem {
    protected float damageScale = 1.0F;
    protected int weaponFlash = 0;

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemStaffSummoning(Item.Properties properties, String itemName, String textureName) {
        super(properties);
        this.itemName = itemName;
        this.setup();
    }


    // ==================================================
    //                       Use
    // ==================================================
    public void damageItemCharged(ItemStack itemStack, LivingEntity entity, float power) {
        ExtendedPlayer playerExt = null;
        if (entity instanceof Player) {
            playerExt = ExtendedPlayer.getForPlayer((Player) entity);
        }
        if (playerExt != null && playerExt.hasStaffPortal()) {
            this.damageStaff(itemStack, playerExt.getStaffPortal().getSummonAmount(), (ServerPlayer) entity, entity.getUsedItemHand());
        }
    }

    // ========== Prevent Swing ==========
    @Override
    public boolean onEntitySwing(ItemStack itemStack, LivingEntity entity, net.minecraft.world.InteractionHand lycHand) {
        if (entity instanceof Player) {
            entity.startUsingItem(InteractionHand.MAIN_HAND);
            return true;
        }
        return super.onEntitySwing(itemStack, entity, lycHand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity entity) {
        return true;
    }

    // ========== Start ==========
    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!CreatureManager.getInstance().getConfig().isSummoningAllowed(world)) {
            return InteractionResult.FAIL;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt != null) {
            // Summon Selected Mob:
            SummonSet summonSet = playerExt.getSelectedSummonSet();
            if (summonSet.isUseable()) {
                if (!player.level().isClientSide()) {
                    PortalEntity staffPortal = new PortalEntity((EntityType<? extends PortalEntity>) ProjectileManager.getInstance().getOldProjectileType(PortalEntity.class), world, player, summonSet, this);
                    playerExt.setStaffPortal(staffPortal);
                    staffPortal.snapTo(player.position().x(), player.position().y(), player.position().z(), world.getRandom().nextFloat() * 360.0F, 0.0F);
                    DeferredLevelActionManager.spawnEntity(world, player.blockPosition(), "staff_portal:" + staffPortal.getUUID(), staffPortal);
                }
            }
            // Open Minion GUI If None Selected:
            else {
                playerExt.clearStaffPortal();
                if (!player.level().isClientSide())
                    playerExt.sendAllSummonSetsToPlayer();
                else
                    LycanitesMobs.PROXY.openScreen(MessageScreenRequest.GuiRequest.BEASTIARY.id, player);
            }
        }
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack);
    }


    // ========== Using ==========
    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack itemStack, int useRemaining) {
        if (!user.isUsingItem()) {
            return;
        }
        int useTime = this.getUseDuration(itemStack, user) - useRemaining;
        if (useTime >= this.getRapidTime(itemStack)) {
            int rapidRemainder = useTime % this.getRapidTime(itemStack);
            if (rapidRemainder == 0) {
                if (this.rapidAttack(itemStack, user.level(), user)) {
                    this.weaponFlash = Math.max(20, this.getRapidTime(itemStack));
                }
            }
        }
        if (useTime >= this.getChargeTime(itemStack))
            this.weaponFlash = Math.max(20, this.getChargeTime(itemStack));

        super.onUseTick(level, user, itemStack, useRemaining);
    }

    // ========== Stop ==========
    @Override
    public boolean releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        int useTime = this.getUseDuration(stack, entityLiving) - timeLeft;
        float power = (float) useTime / (float) this.getChargeTime(stack);

        this.weaponFlash = 0;

        if ((double) power < 0.1D)
            return false;
        if (power > 1.0F)
            power = 1.0F;

        if (this.chargedAttack(stack, worldIn, entityLiving, power)) {
            this.damageItemCharged(stack, entityLiving, power);
            this.weaponFlash = Math.min(20, this.getChargeTime(stack));
        }

        ExtendedPlayer playerExt = null;
        if (entityLiving instanceof Player) {
            playerExt = ExtendedPlayer.getForPlayer((Player) entityLiving);
        }
        if (playerExt != null) {
            playerExt.clearStaffPortal();
        }
        return true;
    }

    // ========== Animation ==========
    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }

    // ========== Max Use Duration ==========
    @Override
    public int getUseDuration(ItemStack itemStack, net.minecraft.world.entity.LivingEntity useDurationUser) {
        return 72000;
    }

    // ========== Charge Time ==========
    public int getChargeTime(ItemStack itemStack) {
        return 1;
    }

    // ========== Rapid Time ==========
    public int getRapidTime(ItemStack itemStack) {
        return 20;
    }

    // ========== Summon Cost ==========
    public int getSummonCostBoost() {
        return 0;
    }

    public float getSummonCostMod() {
        return 1.0F;
    }

    // ========== Summon Duration ==========
    public int getSummonDuration() {
        return 60 * 20;
    }

    // ========== Summon Amount ==========
    public int getSummonAmount() {
        return 1;
    }

    // ========== Additional Costs ==========
    public boolean getAdditionalCosts(Player player) {
        return true;
    }

    // ========== Minion Behaviour ==========
    public void applyMinionBehaviour(TameableCreatureEntity minion, Player player) {
        SummonSet summonSet = ExtendedPlayer.getForPlayer(player).getSelectedSummonSet();
        summonSet.applyBehaviour(minion);
        minion.setSubspecies(summonSet.getSubspecies());
        minion.applyVariant(summonSet.getVariant());
    }

    // ========== Minion Effects ==========
    public void applyMinionEffects(BaseCreatureEntity minion) {
    }


    // ==================================================
    //                      Attack
    // ==================================================
    // ========== Rapid ==========
    public boolean rapidAttack(ItemStack itemStack, Level world, LivingEntity entity) {
        return false;
    }

    protected void damageStaff(ItemStack itemStack, int amountToDamage, ServerPlayer entity, InteractionHand hand) {
        itemStack.hurtAndBreak(amountToDamage, entity, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        if (itemStack.getCount() == 0) {
            entity.setItemInHand(hand, ItemStack.EMPTY);
        }
    }

    // ========== Charged ==========
    public boolean chargedAttack(ItemStack itemStack, Level world, LivingEntity entity, float power) {
        ExtendedPlayer playerExt = null;
        if (entity instanceof Player) {
            playerExt = ExtendedPlayer.getForPlayer((Player) entity);
        }
        if (playerExt != null && playerExt.hasStaffPortal()) {
            PortalEntity staffPortal = playerExt.getStaffPortal();
            int successCount = staffPortal.summonCreatures();
            this.damageStaff(itemStack, successCount, (ServerPlayer) entity, entity.getUsedItemHand());
            return successCount > 0;
        }
        return false;
    }


    // ==================================================
    //                     Repairs
    // ==================================================
    // TODO 26.x: repairability moved to the REPAIRABLE data component; re-add gold-ingot repair
    // via Item.Properties.repairable(Items.GOLD_INGOT) when items get their component pass.
}
