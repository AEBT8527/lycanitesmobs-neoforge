package com.lycanitesmobs.client.renderer.entity.projectile;

import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.misc.LaserEndProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.LaserProjectileEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class ProjectileSpriteRenderer extends EntityRenderer<BaseProjectileEntity, ProjectileRenderState> {
    private Class projectileClass;

    public ProjectileSpriteRenderer(EntityRendererProvider.Context renderManager, Class projectileClass) {
        super(renderManager);
        this.projectileClass = projectileClass;
    }

    @Override
    public ProjectileRenderState createRenderState() {
        return new ProjectileRenderState();
    }

    @Override
    public void extractRenderState(BaseProjectileEntity lycEntity, ProjectileRenderState lycState, float lycPt) {
        super.extractRenderState(lycEntity, lycState, lycPt);
        lycState.entity = lycEntity;
        lycState.lycPartialTicks = lycPt;
    }

    @Override
    public void submit(ProjectileRenderState lycState, PoseStack matrixStack, net.minecraft.client.renderer.SubmitNodeCollector lycCollector, net.minecraft.client.renderer.state.level.CameraRenderState lycCamera) {
        super.submit(lycState, matrixStack, lycCollector, lycCamera);
        BaseProjectileEntity entity = lycState.entity;
        if (entity == null) return;
        float partialTicks = lycState.lycPartialTicks;
        float yaw = partialTicks;
        int brightness = lycState.lightCoords;
        com.lycanitesmobs.client.renderer.util.SubmitBufferSource renderTypeBuffer = new com.lycanitesmobs.client.renderer.util.SubmitBufferSource();
        this.renderLegacy(entity, partialTicks, yaw, matrixStack, renderTypeBuffer, brightness);
        renderTypeBuffer.submitAll(matrixStack, lycCollector);
    }

    protected void renderLegacy(BaseProjectileEntity entity, float partialTicks, float yaw, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int brightness) {
        if (entity instanceof CustomProjectileEntity && !((CustomProjectileEntity) entity).hasProjectileInfo())
            return;
        if (entity.getClass() == LaserEndProjectileEntity.class) return;

        float loop = (float) entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : Math.min(1, partialTicks));
        float scale = entity.getProjectileScale();

        if (entity instanceof CustomProjectileEntity && ((CustomProjectileEntity) entity).getLaserEnd() != null) {
            matrixStack.pushPose();
            this.renderLaser((CustomProjectileEntity) entity, matrixStack, renderTypeBuffer, ((CustomProjectileEntity) entity).getLaserWidth() / 4, loop);
            matrixStack.popPose();
            return;
        }

        matrixStack.pushPose();
        matrixStack.mulPose(this.entityRenderDispatcher.camera.rotation());
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        matrixStack.translate(0, entity.getTextureOffsetY(), 0);
        matrixStack.scale(scale, scale, scale);
        Identifier texture = this.getTextureLocation(entity);
        RenderType rendertype = CustomRenderStates.getSpriteRenderType(texture);
        this.renderSprite(entity, matrixStack, renderTypeBuffer, rendertype, entity.getTextureScale());
        matrixStack.popPose();
    }


    public void renderSprite(BaseProjectileEntity entity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, RenderType rendertype, float scale) {
        float textureWidth = 0.25F;
        float textureHeight = 0.25F;
        float minU = 0;
        float maxU = 1;
        float minV = 0;
        float maxV = 1;
        if (entity.getAnimationFrameMax() > 0) {
            minV = (float) entity.getAnimationFrame() / (float) entity.getAnimationFrameMax();
            maxV = minV + (1F / (float) entity.getAnimationFrameMax());
            textureWidth *= scale;
            textureHeight *= scale;
        }

        Matrix4f matrix4f = matrixStack.last().pose();
        VertexConsumer vertexBuilder = renderTypeBuffer.getBuffer(rendertype);

        vertexBuilder
                .addVertex(matrix4f, -textureWidth, -textureHeight + (textureHeight / 2), 0.0F) // pos
                .setColor(255, 255, 255, 255) // color
                .setUv(minU, maxV) // texture
                .setNormal(0.0F, 1.0F, 0.0F) // normal
                ;
        vertexBuilder
                .addVertex(matrix4f, textureWidth, -textureHeight + (textureHeight / 2), 0.0F)
                .setColor(255, 255, 255, 255) // color
                .setUv(maxU, maxV)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
        vertexBuilder
                .addVertex(matrix4f, textureWidth, textureHeight + (textureHeight / 2), 0.0F)
                .setColor(255, 255, 255, 255) // color
                .setUv(maxU, minV)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
        vertexBuilder
                .addVertex(matrix4f, -textureWidth, textureHeight + (textureHeight / 2), 0.0F)
                .setColor(255, 255, 255, 255) // color
                .setUv(minU, minV)
                .setNormal(0.0F, 1.0F, 0.0F)
                ;
    }

    public void renderLaser(CustomProjectileEntity entity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float scale, float loop) {
        double laserSize = entity.position().distanceTo(entity.getLaserEnd());
        float spacing = 1;
        double factor = spacing / laserSize;
        if (laserSize <= 0) return;

        Identifier texture = this.getTextureLocation(entity);
        RenderType rendertype = CustomRenderStates.getSpriteRenderType(texture);
        Vec3 direction = entity.getLaserEnd().subtract(entity.position()).normalize();

        for (float segment = 0; segment <= laserSize; segment += factor) {
            matrixStack.pushPose();
            matrixStack.translate(segment * direction.x() * spacing, segment * direction.y() * spacing, segment * direction.z() * spacing);
            matrixStack.translate(0, entity.getTextureOffsetY(), 0);
            matrixStack.mulPose(this.entityRenderDispatcher.camera.rotation());
            matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            matrixStack.scale(scale, scale, scale);
            this.renderSprite(entity, matrixStack, renderTypeBuffer, rendertype, scale);
            matrixStack.popPose();
        }
    }


        public Identifier getTextureLocation(BaseProjectileEntity entity) {
        Identifier tex = entity.getTexture();
        return tex != null ? tex : MissingTextureAtlasSprite.getLocation();
    }

    protected Identifier getLaserTexture(LaserProjectileEntity entity) {
        return entity.getBeamTexture();
    }

    public void bindTexture(Identifier texture) {
        // TODO Remove
    }
}
