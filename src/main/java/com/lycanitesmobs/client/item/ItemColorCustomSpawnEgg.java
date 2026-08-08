package com.lycanitesmobs.client.item;

import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.item.consumable.entity.ItemCustomSpawnEgg;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

public class ItemColorCustomSpawnEgg implements ItemColor {
    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        if (!(itemStack.getItem() instanceof ItemCustomSpawnEgg))
            return 16777215;

        ItemCustomSpawnEgg itemCustomSpawnEgg = (ItemCustomSpawnEgg) itemStack.getItem();
        CreatureInfo creatureInfo = itemCustomSpawnEgg.getCreatureInfo(itemStack);
        if (creatureInfo != null) {
            return tintIndex == 0 ? creatureInfo.getEggBackColor() : creatureInfo.getEggForeColor();
        }
        return tintIndex == 0 ? 0x227744 : 0x11EE44;
    }
}
