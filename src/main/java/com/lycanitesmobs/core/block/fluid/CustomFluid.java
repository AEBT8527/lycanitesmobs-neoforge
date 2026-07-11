package com.lycanitesmobs.core.block.fluid;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

abstract public class CustomFluid extends BaseFlowingFluid {

    public CustomFluid(Properties properties, String name) {
        super(properties);
        this.setRegistryName(LycanitesMobs.MODID, name);
    }

    private ResourceLocation registryName;

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public ResourceLocation setRegistryName(String modID, String blockName) {
        return registryName = ResourceLocation.fromNamespaceAndPath(modID, blockName);
    }

    public static class Flowing extends CustomFluid {
        public Flowing(Properties properties, String name) {
            super(properties, name);
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> fluidStateDefinitionBuilder) {
            super.createFluidStateDefinition(fluidStateDefinitionBuilder);
            fluidStateDefinitionBuilder.add(LEVEL);
        }

        public int getAmount(FluidState fluidState) {
            return fluidState.getValue(LEVEL);
        }

        public boolean isSource(FluidState fluidState) {
            return false;
        }
    }

    public static class Still extends CustomFluid {
        public Still(Properties properties, String name) {
            super(properties, name);
        }

        public int getAmount(FluidState fluidState) {
            return 8;
        }

        public boolean isSource(FluidState fluidState) {
            return true;
        }
    }
}
