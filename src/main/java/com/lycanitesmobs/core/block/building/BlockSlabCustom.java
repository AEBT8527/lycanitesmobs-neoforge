package com.lycanitesmobs.core.block.building;

import com.lycanitesmobs.core.block.BlockTypeGetter;
import com.lycanitesmobs.core.block.base.BlockBase;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockSlabCustom extends SlabBlock implements BlockTypeGetter {
    private String blockName = "BlockBase";
    private ResourceLocation registryName;

    // ==================================================
    //                   Constructor
    // ==================================================
    public BlockSlabCustom(Block.Properties properties, BlockBase block) {
        super(properties);
        String slabName = "_slab";
        this.registryName = AssetHelper.modResource(block.getBlockName() + slabName);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(this.getDescription(stack, world));
    }

    public MutableComponent getDescription(ItemStack itemStack, net.minecraft.world.item.Item.TooltipContext world) {
        return Component.translatable(this.getDescriptionId() + ".description").withStyle(ChatFormatting.GREEN);
    }


    // ==================================================
    //                    Harvesting
    // ==================================================
    @Nullable
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return Item.byBlock(this);
    }

    public ItemStack getItem(Level worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public void setRegistryName(ResourceLocation registryName) {
        this.registryName = registryName;
    }
}
