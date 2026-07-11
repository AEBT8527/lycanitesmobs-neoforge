package com.lycanitesmobs.core.util.helpers;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.resources.Identifier;


public class AssetHelper {
    public static Identifier resource(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static Identifier modResource(String path) {
        return resource(LycanitesMobs.MODID, path);
    }

    public static Identifier texture(String path) {
        return modResource(path);
    }

    public static Identifier entityTexture(String textureName) {
        return texture("textures/entity/" + textureName.toLowerCase() + ".png");
    }

    public static Identifier creatureIcon(String creatureName) {
        return texture("textures/guis/creatures/" + creatureName + "_icon.png");
    }
}
