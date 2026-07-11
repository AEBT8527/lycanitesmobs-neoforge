package com.lycanitesmobs.client.loader;

import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.renderer.util.RecolorTextureCache;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@OnlyIn(Dist.CLIENT)
public class ModelReloadListener implements PreparableReloadListener {
    public static final ModelReloadListener INSTANCE = new ModelReloadListener();

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState lycShared,
                                          Executor backgroundExecutor,
                                          PreparationBarrier barrier,
                                          Executor gameExecutor) {
        ResourceManager resourceManager = lycShared.resourceManager();
        LMHelperClass.logDebug("Resources", "ModelReloadListener.reload: start");

        return CompletableFuture
                .supplyAsync(() -> {
                    LMHelperClass.logDebug("Resources", "ModelReloadListener.reload: prepare");
                    return ModelManager.getInstance();
                }, backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(manager -> {
                    LMHelperClass.logDebug("Resources", "ModelReloadListener.reload: apply");
                    RecolorTextureCache.clear();
                    manager.reloadModels(resourceManager);
                    LMHelperClass.logDebug("Resources", "ModelReloadListener.reload: done");
                }, gameExecutor);
    }

}


