package com.lycanitesmobs.client.extensions;

import com.lycanitesmobs.client.renderer.item.EquipmentPartRenderer;
import com.lycanitesmobs.client.renderer.item.EquipmentRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class LycanitesClientExtensions {
    private LycanitesClientExtensions() {
    }

    public static IClientItemExtensions createEquipmentItemExtensions() {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new EquipmentRenderer();
            }
        };
    }

    public static IClientItemExtensions createEquipmentPartItemExtensions() {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new EquipmentPartRenderer();
            }
        };
    }
}
