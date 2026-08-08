package com.lycanitesmobs.client.renderer.layer.creature;

import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import net.minecraft.resources.Identifier;

public class LayerCreatureSaddle extends LayerCreatureBase {
    public LayerCreatureSaddle(CreatureRenderer renderer) {
        super(renderer);
    }

    @Override
    public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
        if(!super.canRenderLayer(entity, scale) || !(entity instanceof RideableCreatureEntity))
            return false;
        return ((RideableCreatureEntity)entity).hasSaddle();
    }

    @Override
    public Identifier getLayerTexture(BaseCreatureEntity entity) {
        return entity.getEquipmentTexture("saddle");
    }
}
