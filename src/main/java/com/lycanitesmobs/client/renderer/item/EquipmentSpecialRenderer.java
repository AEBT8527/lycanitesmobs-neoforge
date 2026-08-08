package com.lycanitesmobs.client.renderer.item;

import com.lycanitesmobs.client.renderer.util.SubmitBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * 26.x replacement for the old BlockEntityWithoutLevelRenderer equipment rendering:
 * a SpecialModelRenderer that drives the legacy OBJ equipment renderers and replays
 * their geometry through the submit pipeline.
 */
public class EquipmentSpecialRenderer implements SpecialModelRenderer<ItemStack> {

    protected final boolean partMode;
    protected final EquipmentRenderer equipmentRenderer = new EquipmentRenderer();
    protected final EquipmentPartRenderer partRenderer = new EquipmentPartRenderer();

    public EquipmentSpecialRenderer(boolean partMode) {
        this.partMode = partMode;
    }

    @Override
    public void submit(ItemStack itemStack, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }
        SubmitBufferSource buffer = new SubmitBufferSource();
        try {
            if (this.partMode) {
                this.partRenderer.renderByItem(itemStack, ItemDisplayContext.GUI, poseStack, buffer, lightCoords, overlayCoords);
            } else {
                this.equipmentRenderer.renderByItem(itemStack, ItemDisplayContext.GUI, poseStack, buffer, lightCoords, overlayCoords);
            }
        } catch (Exception e) {
            return;
        }
        buffer.submitAll(poseStack, collector);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(-0.5F, -0.5F, -0.5F));
        output.accept(new Vector3f(0.5F, 0.5F, 0.5F));
    }

    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    public record Unbaked(boolean part) implements SpecialModelRenderer.Unbaked<ItemStack> {
        public static final MapCodec<Unbaked> EQUIPMENT_CODEC = MapCodec.unit(new Unbaked(false));
        public static final MapCodec<Unbaked> PART_CODEC = MapCodec.unit(new Unbaked(true));

        @Override
        public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
            return new EquipmentSpecialRenderer(this.part);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<ItemStack>> type() {
            return this.part ? PART_CODEC : EQUIPMENT_CODEC;
        }
    }
}
