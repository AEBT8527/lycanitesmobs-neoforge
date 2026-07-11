package com.lycanitesmobs.core.item.base;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class BaseItem extends Item {
    public static int DESCRIPTION_WIDTH = 200;
    public Identifier registryName = null;
    public String itemName = "unamed_item";

    public BaseItem(Properties properties) {
        super(properties);
        setup();
    }

    public Identifier getRegistryName() {
        return registryName;
    }

    public Identifier setRegistryName(String modID, String itemName) {
        return registryName = Identifier.fromNamespaceAndPath(modID, itemName);
    }

    public void setup() {
        this.setRegistryName(LycanitesMobs.MODID, this.itemName);
    }

    @Nonnull
    public String lycDescriptionId() {
        return "item." + LycanitesMobs.MODID + "." + this.itemName;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.lycDescriptionId());
    }


        public void appendHoverTextLegacy(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {
        Component description = this.getDescription(stack, worldIn, tooltip, flag);
        if (!"".equalsIgnoreCase(description.getString())) {
            tooltip.add(description);
        }
    }

    public Component getDescription(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {
        return Component.translatable(this.lycDescriptionId() + ".description").withStyle(ChatFormatting.GREEN);
    }

    public boolean hasContainerItem(ItemStack stack) {
        return !this.getContainerItem(stack).isEmpty();
    }

    public ItemStack getContainerItem(ItemStack itemStack) {
        net.minecraft.world.item.ItemStackTemplate lycRemainder = itemStack.getItem().getCraftingRemainder();
        return lycRemainder == null ? ItemStack.EMPTY : lycRemainder.create();
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return super.onEntityItemUpdate(stack, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        return super.use(world, player, hand);
    }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        return super.interactLivingEntity(stack, player, entity, hand);
    }


    @Override
    public void onUseTick(Level p_41428_, LivingEntity p_41429_, ItemStack p_41430_, int p_41431_) {
        super.onUseTick(p_41428_, p_41429_, p_41430_, p_41431_);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        return super.releaseUsing(stack, worldIn, entityLiving, timeLeft);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return super.getUseAnimation(itemStack);
    }

    /**
     * Gets or creates an NBT Compound for the provided itemstack.
     **/
    public CompoundTag getTagCompound(ItemStack itemStack) {
        return itemStack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
    }

    public void playSound(Level world, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(null, x, y, z, sound, category, volume, pitch);
    }

    public void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(null, pos, sound, category, volume, pitch);
    }
    @Override
    public void appendHoverText(ItemStack lycStack, net.minecraft.world.item.Item.TooltipContext lycCtx, net.minecraft.world.item.component.TooltipDisplay lycDisplay, java.util.function.Consumer<Component> lycBuilder, TooltipFlag lycFlag) {
        List<Component> lycTooltip = new java.util.ArrayList<>();
        this.appendHoverTextLegacy(lycStack, lycCtx, lycTooltip, lycFlag);
        lycTooltip.forEach(lycBuilder);
    }

}
