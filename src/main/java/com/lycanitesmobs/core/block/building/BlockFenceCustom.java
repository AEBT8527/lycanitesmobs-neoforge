package com.lycanitesmobs.core.block.building;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.BlockTypeGetter;
import com.lycanitesmobs.core.block.base.BlockBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;

import javax.annotation.Nullable;
import java.util.List;

public class BlockFenceCustom extends FenceBlock implements BlockTypeGetter {
    private String blockName = "BlockBase";
    private Identifier registryName;

    public BlockFenceCustom(Block.Properties properties, BlockBase block) {
        super(com.lycanitesmobs.core.util.helpers.LMHelperClass.blockId(properties, block.getBlockName() + "_fence"));
        this.setRegistryName(LycanitesMobs.MODID, block.getBlockName() + "_fence");
    }

    public Identifier getRegistryName() {
        return registryName;
    }

    @Override
    public void setRegistryName(Identifier registryName) {
        this.registryName = registryName;
    }

    public String lycFenceDescriptionId() {
        return "block." + LycanitesMobs.modInfo.modid + "." + this.blockName;
    }

    public Identifier setRegistryName(String modID, String blockName) {
        return registryName = Identifier.fromNamespaceAndPath(modID, blockName);
    }

    public void setup() {
        this.setRegistryName(LycanitesMobs.MODID, this.blockName);
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable(this.lycFenceDescriptionId());
    }

        public void appendHoverTextLegacy(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(this.getDescription(stack, world));
    }

    public MutableComponent getDescription(ItemStack itemStack, net.minecraft.world.item.Item.TooltipContext world) {
        return Component.translatable(this.lycFenceDescriptionId() + ".description").withStyle(ChatFormatting.GREEN);
    }

}
