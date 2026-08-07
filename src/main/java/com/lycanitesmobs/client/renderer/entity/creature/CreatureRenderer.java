package com.lycanitesmobs.client.renderer.entity.creature;

import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.model.creature.base.CreatureModel;
import com.lycanitesmobs.client.renderer.layer.creature.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.client.renderer.util.SubmitBufferSource;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Creature renderer, ported to 26.x's state/submit rendering.
 * <p>
 * The legacy per-frame animation pipeline is preserved: {@link CreatureRenderState} carries the
 * live entity, and all geometry the model emits is captured by {@link SubmitBufferSource} and
 * replayed through {@link SubmitNodeCollector#submitCustomGeometry}. The old VBO/Iris fast paths
 * are gone (see VBOObjModel); everything renders through vanilla entity render types.
 */
public class CreatureRenderer<T extends BaseCreatureEntity> extends MobRenderer<T, CreatureRenderState, CreatureModel<BaseCreatureEntity>> {
    /** Legacy creature layers (equipment, saddle, effects...) driven by our own render loop. */
    protected final List<LayerCreatureBase> customLayers = new ArrayList<>();

    public Identifier texture;

    public CreatureRenderer(String entityID, EntityRendererProvider.Context renderManager, float shadowSize) {
        super(renderManager, ModelManager.getInstance().getCreatureModel(CreatureManager.getInstance().getCreature(entityID), null), shadowSize);
    }

    @Override
    public CreatureRenderState createRenderState() {
        return new CreatureRenderState();
    }

    @Override
    public void extractRenderState(T entity, CreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.entity = entity;
        state.lycPartialTicks = partialTicks;
        state.lycYaw = partialTicks;
        state.lycPackedLight = state.lightCoords;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void submit(CreatureRenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // Vanilla handles shadow, fire, name tag and hitbox from the extracted state; our model
        // is empty for the vanilla path so this draws no geometry of its own.
        super.submit(state, matrixStack, collector, camera);

        T entity = (T) state.entity;
        if (entity == null) {
            return;
        }
        float partialTicks = state.lycPartialTicks;
        float yaw = partialTicks; // legacy code used the same interpolant for both
        int brightness = state.lycPackedLight;

        // Get Model and Layers:
        try {
            this.customLayers.clear();
            this.model = (CreatureModel<BaseCreatureEntity>) (CreatureModel<?>) ModelManager.getInstance().getCreatureModel(entity.getCreatureInfo(), entity.getSubspecies());
            if (this.model == null) {
                return;
            }
            this.model.addCustomLayers(this);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        SubmitBufferSource renderTypeBuffer = new SubmitBufferSource();

        // Entity states:
        float scale = 1;
        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        this.model.riding = shouldSit;
        this.model.young = entity.isBaby();
        float renderYaw = Mth.clamp(yaw, entity.yBodyRotO, entity.yBodyRot);
        float renderYawHead = Mth.clamp(yaw, entity.yHeadRotO, entity.yHeadRot);

        float lookYaw = renderYawHead - renderYaw;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity livingentity) {
            renderYaw = Mth.clamp(yaw, livingentity.yBodyRotO, livingentity.yBodyRot);
            lookYaw = renderYawHead - renderYaw;
            float renderYawMountOffset = Mth.wrapDegrees(lookYaw);
            if (renderYawMountOffset < -85.0F) renderYawMountOffset = -85.0F;
            if (renderYawMountOffset >= 85.0F) renderYawMountOffset = 85.0F;
            renderYaw = renderYawHead - renderYawMountOffset;
            if (renderYawMountOffset * renderYawMountOffset > 2500.0F) renderYaw += renderYawMountOffset * 0.2F;
            lookYaw = renderYawHead - renderYaw;
        }

        float time = 0.0F;
        int fade;
        boolean invisible;

        matrixStack.pushPose();
        try {
            float lookPitch = Mth.lerp(yaw, entity.xRotO, entity.xRot);

            float loop = this.getBobLegacy(entity, partialTicks % 1.0F);
            this.setupRotationsLegacy(entity, matrixStack, loop, renderYaw, yaw, 1.0F);
            matrixStack.scale(-1.0F, -1.0F, 1.0F);
            this.scaleLegacy(entity, matrixStack, yaw);
            matrixStack.translate(0.0D, (double) -1.501F, 0.0D);
            float distance = 0.0F;
            if (!shouldSit && entity.isAlive()) {
                distance = entity.walkAnimation.speed(yaw);
                time = entity.walkAnimation.position() - entity.walkAnimation.speed() * (1.0F - yaw);
                if (entity.isBaby()) time *= 3.0F;
                if (distance > 1.0F) distance = 1.0F;
            }

            fade = Math.max(entity.hurtTime, 0);
            invisible = entity.isInvisible();
            boolean allyInvisible = invisible && !entity.isInvisibleTo(Minecraft.getInstance().player);

            this.getMainModel().generateAnimationFrames(entity, time, distance, loop, lookYaw, lookPitch, 1, brightness);
            this.renderModel(entity, matrixStack, renderTypeBuffer, null, time, distance, loop, lookYaw, lookPitch, 1, brightness, fade, invisible, allyInvisible);
            for (LayerCreatureBase layerCreatureBase : this.customLayers) {
                if (!layerCreatureBase.canRenderLayer(entity, scale)) {
                    continue;
                }
                this.renderModel(entity, matrixStack, renderTypeBuffer, layerCreatureBase, time, distance, loop, lookYaw, lookPitch, scale, brightness, fade, invisible, allyInvisible);
            }
            this.getMainModel().clearAnimationFrames();
        } finally {
            matrixStack.popPose();
        }

        renderTypeBuffer.submitAll(matrixStack, collector);
    }

    protected void renderModel(BaseCreatureEntity entity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, LayerCreatureBase layer, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness, int fade, boolean invisible, boolean allyInvisible) {
        texture = this.getEntityTexture(entity, layer);
        if (invisible && !allyInvisible) {
            return;
        }

        int blending = this.getMainModel().getBlending(entity, layer);
        boolean glow = this.getMainModel().getGlow(entity, layer);
        VertexConsumer vertexConsumer = renderTypeBuffer.getBuffer(CustomRenderStates.getObjRenderType(texture, blending, glow));
        this.getMainModel().render(entity, matrixStack, vertexConsumer, layer, time, distance, loop, lookY, lookX, 1, brightness, fade);
    }

    @SuppressWarnings("unchecked")
    public CreatureModel<BaseCreatureEntity> getMainModel() {
        return this.model;
    }

    /** Registration point for the mod's own creature layers (called from model addCustomLayers). */
    public List<LayerCreatureBase> addLayer(LayerCreatureBase renderLayer) {
        if (!this.customLayers.contains(renderLayer)) {
            this.customLayers.add(renderLayer);
        }
        return this.customLayers;
    }

    public Identifier getEntityTexture(BaseCreatureEntity entity, LayerCreatureBase layer) {
        if (layer == null) {
            return entity.getTexture();
        }
        Identifier layerTexture = layer.getLayerTexture(entity);
        return layerTexture != null ? layerTexture : entity.getTexture();
    }

    @Override
    public Identifier getTextureLocation(CreatureRenderState state) {
        return state.entity != null ? state.entity.getTexture() : net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation();
    }

    protected void setupRotationsLegacy(BaseCreatureEntity entity, PoseStack matrixStack, float loop, float renderYaw, float partialTicks, float scale) {
        matrixStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - renderYaw));
    }

    /**
     * Stands in for vanilla's LivingEntityRenderer scale hook, which the legacy render loop calls
     * here and which is empty. It must stay empty: CreatureObjModel already multiplies the model
     * scale by {@code entity.getScale()}, so scaling the pose here too squares it (a 2.5x minion
     * rendered at 6.25x).
     */
    protected void scaleLegacy(BaseCreatureEntity entity, PoseStack matrixStack, float partialTick) {
    }

    protected float getBobLegacy(BaseCreatureEntity creatureEntity, float partialTicks) {
        if (!Minecraft.getInstance().isPaused()) {
            creatureEntity.advanceRenderTick(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
        }
        return creatureEntity.getRenderTick();
    }

    public float interpolateRotation(float par1, float par2, float par3) {
        float f3;
        for (f3 = par2 - par1; f3 < -180.0F; f3 += 360.0F) {
        }
        while (f3 >= 180.0F) {
            f3 -= 360.0F;
        }
        return par1 + par3 * f3;
    }
}
