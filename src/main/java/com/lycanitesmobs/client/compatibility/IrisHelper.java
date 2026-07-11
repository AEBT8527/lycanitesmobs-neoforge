package com.lycanitesmobs.client.compatibility;

import com.lycanitesmobs.client.renderer.util.CustomRenderStates;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Isolated Iris API bridge — this class is <b>only ever loaded</b> when
 * Oculus is present (guarded by {@link OculusCompat#isOculusLoaded()}).
 *
 * <p>Keeping all {@code net.irisshaders.*} imports in this single class
 * means the rest of the codebase compiles and runs cleanly without Oculus
 * on the classpath.</p>
 */
public final class IrisHelper {

    private IrisHelper() {}

    /** Reflectively fetch the Iris API instance (Oculus/Iris ships {@code net.irisshaders.iris.api.v0.IrisApi}). */
    private static Object irisApi() throws ReflectiveOperationException {
        Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
        return apiClass.getMethod("getInstance").invoke(null);
    }

    /**
     * Whether a shader pack is currently active. Reflection keeps the mod compiling and running
     * without Iris/Oculus on the classpath (this class is only touched when Oculus is present).
     */
    public static boolean isShaderPackInUse() {
        try {
            Object api = irisApi();
            return (boolean) api.getClass().getMethod("isShaderPackInUse").invoke(api);
        } catch (Throwable e) {
            return true;
        }
    }

    /**
     * Maps our internal blend-mode + glow flags to the best vanilla entity
     * {@link RenderType} that Iris knows how to intercept.
     *
     * <p>Iris wraps these types with {@code EntityRenderStateShard}, routes
     * them to the shader-pack's {@code gbuffers_entities} program, and
     * extends the vertex format to {@code IrisVertexFormats.ENTITY}
     * automatically.</p>
     *
     * @param texture  The entity texture.
     * @param blending One of {@link CustomRenderStates.BLEND} ordinals.
     * @param glow     Whether this layer should be fullbright / emissive.
     */
    public static RenderType getIrisEntityRenderType(Identifier texture, int blending, boolean glow) {
        if (glow || blending == CustomRenderStates.BLEND.ADD.getValue()) {
            return RenderTypes.entityTranslucentEmissive(texture);
        }
        if (blending == CustomRenderStates.BLEND.SUB.getValue()) {
            return RenderTypes.entityTranslucent(texture);
        }
        return RenderTypes.entityCutout(texture);
    }

    /** Whether we are currently inside a shadow-rendering pass. */
    public static boolean isRenderingShadowPass() {
        try {
            Object api = irisApi();
            return (boolean) api.getClass().getMethod("isRenderingShadowPass").invoke(api);
        } catch (Throwable e) {
            return false;
        }
    }
}
