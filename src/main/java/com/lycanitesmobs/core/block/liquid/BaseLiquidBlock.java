package com.lycanitesmobs.core.block.liquid;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.block.BlockTypeGetter;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.ScheduledTick;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Objects;
import java.util.function.Supplier;

public class BaseLiquidBlock extends LiquidBlock implements BlockTypeGetter {
    private String blockName;
    protected ElementInfo element;
    protected boolean destroyItems = true;
    private Identifier registryName;

    private static final String LAVA_ELEMENT = "lava";

    public BaseLiquidBlock(FlowingFluid fluid, Properties p_54695_) {
        super(fluid, p_54695_);
    }

    public BaseLiquidBlock(Supplier<? extends FlowingFluid> fluidSupplier, BlockBehaviour.Properties properties, String name, ElementInfo element, boolean destroyItems) {
        super(fluidSupplier.get(), com.lycanitesmobs.core.util.helpers.LMHelperClass.blockId(properties, name));
        this.setRegistryName(LycanitesMobs.MODID, name);
        this.blockName = name;
        this.element = element;
        this.destroyItems = destroyItems;
    }

    public Identifier getRegistryName() {
        return registryName;
    }

    public String getBlockName() {
        return this.blockName;
    }

    @Override
    public void setRegistryName(Identifier registryName) {
        this.registryName = registryName;
    }

    public FlowingFluid getFluid() {
        return ObjectManager.getFluid(this.blockName);
    }

    public Identifier setRegistryName(String modID, String blockName) {
        return registryName = Identifier.fromNamespaceAndPath(modID, blockName);
    }

    public void setup() {
        this.setRegistryName(LycanitesMobs.MODID, this.blockName);
    }

    public ElementInfo getElement() {
        return this.element;
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level world, BlockPos blockPos, Block neighborBlock, net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, world, blockPos, neighborBlock, orientation, movedByPiston);
        if (neighborBlock == this) {
            return;
        }
        // 26.x no longer passes the neighbor position, so check all sides for spreading.
        for (net.minecraft.core.Direction lycDir : net.minecraft.core.Direction.values()) {
            BlockPos neighborBlockPos = blockPos.relative(lycDir);
            if (world.getBlockState(neighborBlockPos).getBlock() == this) {
                continue;
            }
            if (this.shouldSpreadLiquid(world, neighborBlockPos, blockState)) {
                world.getFluidTicks().schedule(new ScheduledTick<>(blockState.getFluidState().getType(), blockPos, this.getFluid().getTickDelay(world), 0));
                break;
            }
        }
    }

    public boolean shouldSpreadLiquid(Level world, BlockPos neighborBlockPos, BlockState blockState) {
        BlockState neighborBlockState = world.getBlockState(neighborBlockPos);
        if (neighborBlockState.getBlock() instanceof LiquidBlock) {
            return false;
        }
        return true;
    }

    protected boolean isWaterLikeFluid(Level world, BlockPos pos) {
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.is(FluidTags.WATER)) return true;
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof BaseLiquidBlock liquidBlock) {
            ElementInfo neighborElement = liquidBlock.getElement();
            return neighborElement != null && !LAVA_ELEMENT.equals(neighborElement.getName());
        }
        return false;
    }

    protected boolean isLavaLikeFluid(Level world, BlockPos pos) {
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.is(FluidTags.LAVA)) return true;
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof BaseLiquidBlock liquidBlock) {
            ElementInfo neighborElement = liquidBlock.getElement();
            return neighborElement != null && LAVA_ELEMENT.equals(neighborElement.getName());
        }
        return false;
    }


    @Override
    protected void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (this.destroyItems && (entity instanceof ItemEntity || entity instanceof ExperienceOrb)
                && world instanceof net.minecraft.server.level.ServerLevel lycServerLevel) {
            entity.kill(lycServerLevel);
        }
        super.entityInside(blockState, world, pos, entity, effectApplier, isPrecise);
    }

    /**
     * Client side animation and sounds.
     **/
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (random.nextInt(52) == 0) {
            world.playLocalSound(x + 0.5D, y + 0.5D, z + 0.5D, Objects.requireNonNull(ObjectManager.getSound(this.blockName)), SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
        }
        super.animateTick(state, world, pos, random);
    }
}
