package com.lycanitesmobs.core.item.base;


import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemBlockBase extends BlockItem {
    public Identifier registryName;

    public ItemBlockBase(Block block, String name, Item.Properties properties) {
        super(block, properties);
        this.setRegistryName(LycanitesMobs.MODID, name);
    }

    public Identifier getRegistryName() {
        return registryName;
    }

    public Identifier setRegistryName(String modID, String blockName) {
        return registryName = Identifier.fromNamespaceAndPath(modID, blockName);
    }

    @Override
    public MutableComponent getName(ItemStack stack) {
        return this.getBlock().getName();
    }
}
