package com.lycanitesmobs.client.renderer.layer.creature;

import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class LayerCreatureEquipment extends LayerCreatureBase {
    public String equipmentSlot;

    public LayerCreatureEquipment(CreatureRenderer renderer, String equipmentSlot) {
        super(renderer);
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
        if (!super.canRenderLayer(entity, scale) || this.equipmentSlot == null)
            return false;
        return entity.getEquipmentName(this.equipmentSlot) != null;
    }

    @Override
    public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
        return entity.getEquipmentTexture(entity.getEquipmentName(this.equipmentSlot));
    }

    @Override
    public Vector4f getPartColor(String partName, BaseCreatureEntity entity, boolean trophy) {
        ItemStack equipmentStack = entity.getCreatureInventory().getEquipmentStack("chest");
        if (equipmentStack != null && equipmentStack.has(net.minecraft.core.component.DataComponents.DYED_COLOR)) {
            int color = equipmentStack.get(net.minecraft.core.component.DataComponents.DYED_COLOR).rgb();
            float r = (float) (color >> 16 & 255) / 255.0F;
            float g = (float) (color >> 8 & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;
            return new Vector4f(r, g, b, 1);
        }
        return new Vector4f(1, 1, 1, 1);
    }

}
