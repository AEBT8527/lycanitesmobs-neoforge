package com.lycanitesmobs.core.entity.effect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class EffectBase extends MobEffect {
	private final String name;
	private ResourceLocation registryName;
	
	// ==================================================
	//                    Constructor
	// ==================================================
	public EffectBase(String name, boolean badEffect, int color) {
		super(badEffect ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, color);
		this.name = name;
		this.setRegistryName(LycanitesMobs.MODID, name);
	}

	public ResourceLocation getRegistryName() {
		return registryName;
	}
	public ResourceLocation setRegistryName(String modID, String blockName) {
		return registryName = ResourceLocation.fromNamespaceAndPath(modID, blockName);
	}


	// ==================================================
	//                    Effects
	// ==================================================
	@Override
	public boolean isInstantenous() {
        return false;
    }

	public ResourceLocation getTexture() {
		return AssetHelper.texture("textures/mob_effect/" + this.name + ".png");
	}
	

	// ==================================================
	//                    Visuals
	// ==================================================
	/**
	 * IForgeMobEffect no longer has these methods, im assuming they're set elsewhere
	 */
	/*@OnlyIn(Dist.CLIENT)
	@Override
	public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack mStack, int x, int y, float z) {
		ResourceLocation texture = TextureManager.getTexture("effect." + this.name);
		if(texture == null) {
			return;
		}

		Minecraft.getInstance().getTextureManager().bindForSetup(texture);
		gui.blit(mStack, x + 6, y + 7, 0, 0, 18, 18, 18, 18);
		super.renderInventoryEffect(effect, gui, mStack, x, y, z);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack mStack, int x, int y, float z, float alpha) {
		ResourceLocation texture = TextureManager.getTexture("effect." + this.name);
		if(texture == null) {
			return;
		}

		Minecraft.getInstance().getTextureManager().bind(texture);
		gui.blit(mStack, x + 3, y + 3, 0, 0, 18, 18, 18, 18);
	}*/
}
