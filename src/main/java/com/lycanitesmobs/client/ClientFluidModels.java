package com.lycanitesmobs.client;

import com.lycanitesmobs.core.block.fluid.type.BaseFluidType;
import com.lycanitesmobs.core.manager.FluidManager;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

/**
 * 26.x: fluid still/flowing textures are no longer supplied through
 * IClientFluidTypeExtensions; they are fluid models registered on the mod bus.
 */
@OnlyIn(Dist.CLIENT)
public class ClientFluidModels {

    public static void onRegisterItemTintSources(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("lycanitesmobs", "spawn_egg"),
                com.lycanitesmobs.client.item.SpawnEggTintSource.MAP_CODEC);
    }

    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("lycanitesmobs", "equipment"),
                com.lycanitesmobs.client.renderer.item.EquipmentSpecialRenderer.Unbaked.EQUIPMENT_CODEC);
        event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("lycanitesmobs", "equipment_part"),
                com.lycanitesmobs.client.renderer.item.EquipmentSpecialRenderer.Unbaked.PART_CODEC);
    }

    public static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
        FluidManager.getInstance().forEachFluidBuilder((fluidName, builder) -> {
            if (!(builder.getFluidType() instanceof BaseFluidType fluidType)) {
                return;
            }
            FluidModel.Unbaked model = new FluidModel.Unbaked(
                    new Material(fluidType.getStillTexture()),
                    new Material(fluidType.getFlowingTexture()),
                    fluidType.getOverlayTexture() == null ? null : new Material(fluidType.getOverlayTexture()),
                    FluidTintSources.constant(0xFF000000 | fluidType.getTintColor())
            );
            event.register(model, builder::getStillFluid, builder::getFlowingFluid);
        });
    }
}
