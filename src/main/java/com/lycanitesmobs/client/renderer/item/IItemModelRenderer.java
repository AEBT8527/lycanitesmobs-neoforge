package com.lycanitesmobs.client.renderer.item;

import com.lycanitesmobs.client.renderer.layer.item.LayerItem;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface IItemModelRenderer {
    void bindItemTexture(Identifier location);

    List<LayerItem> addLayer(LayerItem renderLayer);
}
