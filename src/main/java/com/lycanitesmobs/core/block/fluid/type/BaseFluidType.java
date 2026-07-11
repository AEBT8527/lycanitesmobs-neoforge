package com.lycanitesmobs.core.block.fluid.type;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * 26.x: FluidType.initializeClient was removed (client fluid extensions now register through
 * RegisterClientExtensionsEvent) and the FogRenderer fog hooks are gone with the fog rework.
 * This type now just carries the fluid's client data; textures/tint should be wired up via the
 * client extensions event when the fluid visual pass is restored.
 */
public class BaseFluidType extends FluidType {
    private final Identifier stillTexture;
    private final Identifier flowingTexture;
    private final Identifier overlayTexture;
    private final int tintColor;
    private final Vector3f fogColor;
    private final Identifier registryName;

    public static BaseFluidType create(Identifier fluidRegistryName, Identifier stillTexture, Identifier flowingTexture, Identifier overlayTexture,
                                       int tintColor, Vector3f fogColor, Properties properties) {
        return new BaseFluidType(fluidRegistryName, stillTexture, flowingTexture, overlayTexture, tintColor, fogColor, properties);
    }

    private BaseFluidType(Identifier fluidRegistryName, Identifier stillTexture, Identifier flowingTexture, Identifier overlayTexture,
                          int tintColor, Vector3f fogColor, Properties properties) {
        super(properties);
        this.stillTexture = Objects.requireNonNull(stillTexture, "stillTexture");
        this.flowingTexture = Objects.requireNonNull(flowingTexture, "flowingTexture");
        this.overlayTexture = overlayTexture;
        this.tintColor = tintColor;
        this.fogColor = new Vector3f(Objects.requireNonNull(fogColor, "fogColor"));
        this.registryName = fluidRegistryName;
    }

    public Identifier getRegistryName() {
        return registryName;
    }

    public Identifier getStillTexture() {
        return stillTexture;
    }

    public Identifier getFlowingTexture() {
        return flowingTexture;
    }

    public int getTintColor() {
        return tintColor;
    }

    public Identifier getOverlayTexture() {
        return overlayTexture;
    }

    public Vector3f getFogColor() {
        return new Vector3f(fogColor);
    }
}
