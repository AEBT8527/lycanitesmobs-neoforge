package com.lycanitesmobs.core.block.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class TileEntityBase extends BlockEntity implements WorldlyContainer {
    protected Identifier registryName = null;

    /**
     * Constructor
     */
    public TileEntityBase(BlockPos pos, BlockState state) {
        super(BlockEntityType.CHEST, pos, state);

    }

    public Identifier getRegistryName() {
        return registryName;
    }

    public Identifier setRegistryName(String modID, String blockName) {
        return registryName = Identifier.fromNamespaceAndPath(modID, blockName);
    }

    @Override
    public BlockEntityType<?> getType() {
        BlockEntityType<?> type = ObjectManager.getTileEntityType(this.getClass());
        return type != null ? type : super.getType();
    }


    /**
     * The main update called every tick.
     */
    public void tick() {
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.isRemoved()) {
            return false;
        }
        return this.getBlockPos().distSqr(player.blockPosition()) < 16F;
    }

    /**
     * Removes this Tile Entity.
     */
    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    /**
     * Called when receiving an event from a client, used for opening GUIs, etc.
     *
     * @param eventID  The ID of the event.
     * @param eventArg The argument ID of the event.
     * @return
     */
    @Override
    public boolean triggerEvent(int eventID, int eventArg) {
        return false;
    }

    /**
     * Gets the update packet for this Tile Entity.
     *
     * @return The update packet to use or null if not needed.
     */
    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return super.getUpdatePacket();
    }

    /**
     * Called when this Tile Entity receives a Data Packet.
     *
     * @param net The Network Manager.
     * @param pkt The packet received.
     */
    @Override
    public void onDataPacket(Connection net, net.minecraft.world.level.storage.ValueInput input) {
        super.onDataPacket(net, input);
    }

    /**
     * Called when a GUI opened by this Tile Entity has a button pressed.
     *
     * @param buttonId The ID of the button pressed.
     */
    public void onGuiButton(int buttonId) {
    }

    /**
     * Reads from saved NBT data.
     *
     * @param nbtTagCompound The NBT to read from.
     */
    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        CompoundTag lycNbt = input.read("LycanitesMobs", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        this.loadLegacy(lycNbt);
    }

    /** Legacy CompoundTag load hook; subclasses override this instead of loadAdditional. */
    protected void loadLegacy(CompoundTag nbtTagCompound) {
    }


    /**
     * Writes to NBT data.
     *
     * @param nbtTagCompound The NBT data to write to.
     * @return The written to NBT data.
     */
    @Override
    public void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag lycNbt = new CompoundTag();
        this.saveLegacy(lycNbt);
        output.store("LycanitesMobs", CompoundTag.CODEC, lycNbt);
    }

    /** Legacy CompoundTag save hook; subclasses override this instead of saveAdditional. */
    protected void saveLegacy(CompoundTag nbtTagCompound) {
    }


    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return false;
    }


    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }
}
