package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.core.block.liquid.BaseLiquidBlock;

import java.util.HashMap;
import java.util.Map;


import com.lycanitesmobs.core.block.liquid.AcidLiquidBlock;
import com.lycanitesmobs.core.block.liquid.MoglavaLiquidBlock;
import com.lycanitesmobs.core.block.liquid.OozeLiquidBlock;
import com.lycanitesmobs.core.block.liquid.PoisonLiquidBlock;
import com.lycanitesmobs.core.block.liquid.VeshoneyLiquidBlock;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.block.fluid.CustomFluid;
import com.lycanitesmobs.core.block.fluid.type.BaseFluidType;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.joml.Vector3f;

import java.util.function.Supplier;
import java.util.function.BiConsumer;

public class FluidManager {

    private static FluidManager INSTANCE;

    public static FluidManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FluidManager();
        }
        return INSTANCE;
    }

    private final Map<String, BaseLiquidBlock> worldgenFluidBlocks = new HashMap<>();
    private final Map<String, FluidBuilder> fluidBuilders = new HashMap<>();
    private boolean fluidsDefined = false;

    public void defineFluids() {
        if (this.fluidsDefined) {
            return;
        }
        this.fluidsDefined = true;

        Block.Properties waterBlockProperties = Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable().replaceable();
        Block.Properties waterBrightBlockProperties = Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable().lightLevel(s -> 10).replaceable();
        Block.Properties lavaBlockProperties = Block.Properties.of().mapColor(MapColor.FIRE).noCollission().randomTicks().strength(100).noLootTable().lightLevel(s -> 15).replaceable();

        addFluid("ooze", 0x009F9F, 3000, 3000, 0, 10, false, OozeLiquidBlock::new, waterBrightBlockProperties, "frost", false, true);
        addFluid("rabbitooze", 0x00AFAF, 3000, 3000, 0, 10, true, OozeLiquidBlock::new, waterBrightBlockProperties, "frost", false, false);
        addFluid("moglava", 0xFF5722, 3000, 5000, 1100, 15, true, MoglavaLiquidBlock::new, lavaBlockProperties, "lava", false, true);
        addFluid("acid", 0x8BC34A, 1000, 10, 40, 10, false, AcidLiquidBlock::new, waterBrightBlockProperties, "acid", true, true);
        addFluid("sharacid", 0x8BB35A, 1000, 10, 40, 10, true, AcidLiquidBlock::new, waterBrightBlockProperties, "acid", false, false);
        addFluid("poison", 0x9C27B0, 1000, 8, 20, 0, false, PoisonLiquidBlock::new, waterBlockProperties, "poison", false, true);
        addFluid("vesspoison", 0xAC27A0, 1000, 8, 20, 0, true, PoisonLiquidBlock::new, waterBlockProperties, "poison", false, false);
        addFluid("veshoney", 0xCEBC39, 4000, 4000, 0, 0, false, VeshoneyLiquidBlock::new, waterBlockProperties, "fae", false, false);
    }

    public void addFluid(String fluidName, int fluidColor, int density, int viscosity, int temperature, int luminosity, boolean multiply, LiquidBlockFactory blockFactory, BlockBehaviour.Properties blockProperties, String elementName, boolean destroyItems, boolean worldgen) {
        ElementInfo element = ElementManager.getInstance().getElement(elementName);
        Vector3f fogColor = new Vector3f(((fluidColor >> 16) & 0xFF) / 255f, ((fluidColor >> 8) & 0xFF) / 255f, (fluidColor & 0xFF) / 255f);
        int tickRate = (viscosity >= 4000) ? 30 : 5;

        FluidBuilder builder = new FluidBuilder(fluidName, AssetHelper.modResource(fluidName));
        FluidType fluidType = builder.createFluidType(
                AssetHelper.modResource("block/" + fluidName + "_still"),
                AssetHelper.modResource("block/" + fluidName + "_flowing"),
                null,
                fluidColor, fogColor, density, viscosity, temperature, luminosity, multiply
        );
        BaseFlowingFluid.Properties fluidProps = builder.fluidProperties(fluidType, tickRate, () -> ObjectManager.getFluidBlock(fluidName));
        this.fluidBuilders.put(fluidName, builder);
        ObjectManager.addSound(fluidName, "block." + fluidName);

        Item.Properties bucketProps = new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);
        ObjectManager.addItem(fluidName + "_bucket", () -> new BucketItem(builder.getStillFluid(), bucketProps));

        ObjectManager.addBlock(fluidName, () -> {
            Supplier<BaseFlowingFluid> still = builder::getStillFluid;
            BaseLiquidBlock block = blockFactory.create(still, blockProperties, fluidName, element, destroyItems);
            if (worldgen) registerWorldgenFluidBlock(fluidName, block);
            return block;
        }, true);
    }

    @FunctionalInterface
    public interface LiquidBlockFactory {
        BaseLiquidBlock create(Supplier<BaseFlowingFluid> still, BlockBehaviour.Properties blockProperties, String fluidName, ElementInfo element, boolean destroyItems);
    }

    private void registerWorldgenFluidBlock(String fluidName, BaseLiquidBlock block) {
        this.worldgenFluidBlocks.put(fluidName, block);
    }

    public void forEachWorldgenFluidBlock(BiConsumer<String, BaseLiquidBlock> action) {
        this.worldgenFluidBlocks.forEach(action);
    }

    public void forEachFluidType(BiConsumer<ResourceLocation, BaseFluidType> action) {
        this.fluidBuilders.values().forEach(builder -> {
            if (builder.getFluidType() instanceof BaseFluidType baseFluidType) {
                action.accept(baseFluidType.getRegistryName(), baseFluidType);
            }
        });
    }

    public void registerFluidObjects(BiConsumer<ResourceLocation, BaseFlowingFluid> action) {
        this.fluidBuilders.forEach((fluidName, builder) -> {
            BaseFlowingFluid still = ObjectManager.getFluid(fluidName);
            if (still == null) {
                still = (BaseFlowingFluid) ObjectManager.addFluid(fluidName, new CustomFluid.Still(builder.getFluidProperties(), fluidName));
            }
            if (still instanceof CustomFluid customFluid) {
                action.accept(customFluid.getRegistryName(), still);
            }

            String flowingName = fluidName + "_flowing";
            BaseFlowingFluid flowing = ObjectManager.getFluid(flowingName);
            if (flowing == null) {
                flowing = (BaseFlowingFluid) ObjectManager.addFluid(flowingName, new CustomFluid.Flowing(builder.getFluidProperties(), flowingName));
            }
            if (flowing instanceof CustomFluid customFluid) {
                action.accept(customFluid.getRegistryName(), flowing);
            }
        });
    }

    public static class FluidBuilder {
        private final String baseName;
        private final ResourceLocation registryName;
        private FluidType fluidType;
        private BaseFlowingFluid.Properties fluidProperties;

        public FluidBuilder(String fluidName, ResourceLocation registryLocation) {
            this.baseName = fluidName;
            this.registryName = registryLocation;
        }

        public FluidType createFluidType(ResourceLocation stillTexture, ResourceLocation flowingTexture, ResourceLocation overlayTexture,
                                         int tintColor, Vector3f fogColor, int fluidDensity, int fluidViscosity, int fluidTemperature, int fluidLuminosity,
                                         boolean fluidMultiply) {
            FluidType.Properties props = FluidType.Properties.create();
            props.density(fluidDensity);
            props.viscosity(fluidViscosity);
            props.temperature(fluidTemperature);
            props.lightLevel(fluidLuminosity);
            if (fluidMultiply) props.canConvertToSource(true);
            fluidType = BaseFluidType.create(registryName, stillTexture, flowingTexture, overlayTexture, tintColor, fogColor, props);
            return fluidType;
        }

        public BaseFlowingFluid.Properties fluidProperties(FluidType fT, int fluidTickRate, Supplier<? extends LiquidBlock> blockSupplier) {
            BaseFlowingFluid.Properties p = new BaseFlowingFluid.Properties(() -> fT, this::getStillFluid, this::getFlowingFluid);
            p.tickRate(fluidTickRate);
            p.bucket(this::getBucketItem);
            p.block(blockSupplier);
            this.fluidProperties = p;
            return p;
        }

        public BaseFlowingFluid getStillFluid() {
            return ObjectManager.getFluid(baseName);
        }

        public BaseFlowingFluid getFlowingFluid() {
            return ObjectManager.getFluid(baseName + "_flowing");
        }

        public BucketItem getBucketItem() {
            return (BucketItem) ObjectManager.getItem(baseName + "_bucket");
        }

        public BaseFlowingFluid.Properties getFluidProperties() {
            return fluidProperties;
        }

        public FluidType getFluidType() {
            return fluidType;
        }
    }
}
