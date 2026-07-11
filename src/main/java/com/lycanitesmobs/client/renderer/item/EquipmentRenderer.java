package com.lycanitesmobs.client.renderer.item;

import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.model.item.EquipmentModel;
import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import com.lycanitesmobs.client.renderer.util.VBOBatcher;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EquipmentRenderer implements IItemModelRenderer {

    public EquipmentRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
    }

    public EquipmentRenderer() {
    }

    public void renderByItem(ItemStack itemStack,
                             ItemDisplayContext displayContext,
                             PoseStack poseStack,
                             MultiBufferSource buffer,
                             int packedLight,
                             int packedOverlay) {
        if (!(itemStack.getItem() instanceof ItemEquipment)) {
            return;
        }

        InteractionHand hand = null;

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(0.55f, 0.5f, 0.55f);
            poseStack.translate(-.45F, 1.45F, 0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(120F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-110F));
        } else {
            poseStack.translate(0.5F, .5F, -.9F);
            poseStack.mulPose(Axis.XP.rotationDegrees(40F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(0F));
        }

        if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
            poseStack.translate(0.3F, 0.3F, -.1F);
        }
        EquipmentModel equipmentModel = ModelManager.getInstance().getEquipmentModel();
        
        float loop = 0F;
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().isPaused()) {
            loop = Minecraft.getInstance().player.tickCount;
        }

        equipmentModel.render(itemStack, hand, poseStack, buffer, this, loop, packedLight);

        poseStack.popPose();
        VBOBatcher.getInstance().endBatches();
    }

    protected void drawBar(VertexConsumer vertexBuilder, int x, int y, int width, int height, int r, int g, int b, int a) {
        float z = 1F;
        vertexBuilder.addVertex(x, y, z).setColor(r, g, b, a);
        vertexBuilder.addVertex(x, y + height, z).setColor(r, g, b, a);
        vertexBuilder.addVertex(x + width, y + height, z).setColor(r, g, b, a);
        vertexBuilder.addVertex(x + width, y, z).setColor(r, g, b, a);
    }

    @Override
    public void bindItemTexture(Identifier location) {
        if (location == null) {
            return;
        }
        // 26.x: textures bind through the RenderType; nothing to do here.
    }

    @Override
    public List<LayerItem> addLayer(LayerItem renderLayer) {
        return new ArrayList<>();
    }
}
