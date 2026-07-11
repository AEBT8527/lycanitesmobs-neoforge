package com.lycanitesmobs.core.util.helpers;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.resources.ResourceLocation;


public class AssetHelper {
    public static ResourceLocation resource(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation modResource(String path) {
        return resource(LycanitesMobs.MODID, path);
    }

    public static ResourceLocation texture(String path) {
        return modResource(path);
    }

    public static ResourceLocation entityTexture(String textureName) {
        return texture("textures/entity/" + textureName.toLowerCase() + ".png");
    }

    public static ResourceLocation creatureIcon(String creatureName) {
        return texture("textures/guis/creatures/" + creatureName + "_icon.png");
    }
}
