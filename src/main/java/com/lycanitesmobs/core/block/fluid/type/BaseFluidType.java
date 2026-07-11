package com.lycanitesmobs.core.block.fluid.type;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.function.Consumer;

public class BaseFluidType extends FluidType {
    private static final ThreadLocal<ClientExtensionProperties> CONSTRUCTING_CLIENT_PROPERTIES = new ThreadLocal<>();

    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private final ResourceLocation overlayTexture;
    private final int tintColor;
    private final Vector3f fogColor;
    private final ResourceLocation registryName;

    public static BaseFluidType create(ResourceLocation fluidRegistryName, ResourceLocation stillTexture, ResourceLocation flowingTexture, ResourceLocation overlayTexture,
                                       int tintColor, Vector3f fogColor, Properties properties) {
        ClientExtensionProperties clientProperties = new ClientExtensionProperties(stillTexture, flowingTexture, overlayTexture, tintColor, fogColor);
        CONSTRUCTING_CLIENT_PROPERTIES.set(clientProperties);
        try {
            return new BaseFluidType(fluidRegistryName, clientProperties, properties);
        } finally {
            CONSTRUCTING_CLIENT_PROPERTIES.remove();
        }
    }

    private BaseFluidType(ResourceLocation fluidRegistryName, ClientExtensionProperties clientProperties, Properties properties) {
        super(properties);
        this.stillTexture = clientProperties.stillTexture();
        this.flowingTexture = clientProperties.flowingTexture();
        this.overlayTexture = clientProperties.overlayTexture();
        this.tintColor = clientProperties.tintColor();
        this.fogColor = clientProperties.copyFogColor();
        this.registryName = fluidRegistryName;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public ResourceLocation getStillTexture() {
        return stillTexture;
    }

    public ResourceLocation getFlowingTexture() {
        return flowingTexture;
    }

    public int getTintColor() {
        return tintColor;
    }

    public ResourceLocation getOverlayTexture() {
        return overlayTexture;
    }

    public Vector3f getFogColor() {
        return new Vector3f(fogColor);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        ClientExtensionProperties clientProperties = CONSTRUCTING_CLIENT_PROPERTIES.get();
        if (clientProperties == null) {
            clientProperties = new ClientExtensionProperties(stillTexture, flowingTexture, overlayTexture, tintColor, fogColor);
        }
        consumer.accept(clientProperties.createExtensions());
    }

    private record ClientExtensionProperties(ResourceLocation stillTexture, ResourceLocation flowingTexture, @Nullable ResourceLocation overlayTexture,
                                             int tintColor, Vector3f fogColor) {
        private ClientExtensionProperties {
            Objects.requireNonNull(stillTexture, "stillTexture");
            Objects.requireNonNull(flowingTexture, "flowingTexture");
            fogColor = new Vector3f(Objects.requireNonNull(fogColor, "fogColor"));
        }

        private Vector3f copyFogColor() {
            return new Vector3f(this.fogColor);
        }

        private IClientFluidTypeExtensions createExtensions() {
            ResourceLocation stillTexture = this.stillTexture;
            ResourceLocation flowingTexture = this.flowingTexture;
            ResourceLocation overlayTexture = this.overlayTexture;
            int tintColor = this.tintColor;
            Vector3f fogColor = this.copyFogColor();
            return new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return stillTexture;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return flowingTexture;
                }

                @Override
                public @Nullable ResourceLocation getOverlayTexture() {
                    return overlayTexture;
                }

                @Override
                public int getTintColor() {
                    return tintColor;
                }

                @Override
                public @NotNull Vector3f modifyFogColor(net.minecraft.client.Camera camera, float partialTick, net.minecraft.client.multiplayer.ClientLevel level,
                                                        int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    return new Vector3f(fogColor);
                }

                @Override
                public void modifyFogRender(net.minecraft.client.Camera camera, net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance, float partialTick,
                                            float nearDistance, float farDistance, FogShape shape) {
                    RenderSystem.setShaderFogStart(1f);
                    RenderSystem.setShaderFogEnd(6f);
                }
            };
        }
    }
}
