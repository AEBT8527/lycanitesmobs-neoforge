package com.lycanitesmobs.core.item.base;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.ModInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 26.x: DiggerItem/Tier were removed (tools are data-component driven now). This base is
 * currently unused by registered items; kept minimal for potential future tools.
 */
public class BaseToolItem extends Item {
    public static int DESCRIPTION_WIDTH = 200;

    public String itemName = "unamed_item";
    public ModInfo modInfo = LycanitesMobs.modInfo;

    public BaseToolItem(Properties properties) {
        super(properties);
    }

    public void appendHoverTextLegacy(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {
        Component description = this.getDescription(stack, worldIn, tooltip, flag);
        if (!"".equalsIgnoreCase(description.getString())) {
            tooltip.add(description);
        }
    }

    public Component getDescription(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {
        return Component.translatable("item." + this.modInfo.modid + "." + this.itemName + ".description").withStyle(ChatFormatting.GREEN);
    }

    public CompoundTag getTagCompound(ItemStack itemStack) {
        return itemStack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
    }

    public void playSound(Level world, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(null, x, y, z, sound, category, volume, pitch);
    }

    public void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(null, pos, sound, category, volume, pitch);
    }
}
