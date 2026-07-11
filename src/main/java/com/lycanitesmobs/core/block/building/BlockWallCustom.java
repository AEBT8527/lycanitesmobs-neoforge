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
import net.minecraft.world.level.block.WallBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class BlockWallCustom extends WallBlock implements BlockTypeGetter {
    private String blockName = "BlockBase";
    private Identifier registryName;

    // ==================================================
    //                   Constructor
    // ==================================================
    public BlockWallCustom(Block.Properties properties, BlockBase block) {
        super(com.lycanitesmobs.core.util.helpers.LMHelperClass.blockId(properties, block.getBlockName() + "_wall"));
        this.setRegistryName(LycanitesMobs.MODID, block.getBlockName() + "_wall");
    }

    public Identifier getRegistryName() {
        return registryName;
    }

    @Override
    public void setRegistryName(Identifier registryName) {
        this.registryName = registryName;
    }

    public Identifier setRegistryName(String modID, String blockName) {
        return registryName = Identifier.fromNamespaceAndPath(modID, blockName);
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable(this.getDescriptionId());
    }

    @OnlyIn(Dist.CLIENT)
        public void appendHoverTextLegacy(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(this.getDescription(stack, world));
    }

    public MutableComponent getDescription(ItemStack itemStack, net.minecraft.world.item.Item.TooltipContext world) {
        return Component.translatable(this.getDescriptionId() + ".description").withStyle(ChatFormatting.GREEN);
    }

}
