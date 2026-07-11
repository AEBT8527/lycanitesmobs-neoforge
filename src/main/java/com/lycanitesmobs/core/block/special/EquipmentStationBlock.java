package com.lycanitesmobs.core.block.special;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.base.BlockBase;
import com.lycanitesmobs.core.container.provider.EquipmentStationContainerProvider;
import com.lycanitesmobs.core.block.blockentity.EquipmentStationTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EquipmentStationBlock extends BlockBase implements EntityBlock {
    public static final EnumProperty<net.minecraft.core.Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public EquipmentStationBlock(Properties properties) {
        super(properties, "equipment_station");

        // Properties:
        this.blockName = "equipment_station";
        this.setRegistryName(LycanitesMobs.MODID, this.blockName.toLowerCase());
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel worldIn, BlockPos pos, boolean isMoving) {
        {
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            if (blockEntity instanceof EquipmentStationTileEntity station) {
                Containers.dropContents(worldIn, pos, station);
            }
        }
        super.affectNeighborsAfterRemoval(state, worldIn, pos, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation direction) {
        return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, entity, itemStack);
    }

/*	@Override
	public boolean hasTileEntity(BlockState blockState) {
		return true;
	}*/

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        EquipmentStationTileEntity tileEntity = new EquipmentStationTileEntity(p_153215_, p_153216_);
        return tileEntity;
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof EquipmentStationTileEntity) {
                ((ServerPlayer) player).openMenu(new EquipmentStationContainerProvider((EquipmentStationTileEntity) tileEntity), buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int eventID, int eventParam) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity != null && tileEntity.triggerEvent(eventID, eventParam);
    }
}
