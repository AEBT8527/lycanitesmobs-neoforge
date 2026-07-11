package com.lycanitesmobs.client.renderer.layer.creature;

import com.lycanitesmobs.client.renderer.entity.creature.CreatureRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector2f;

@OnlyIn(Dist.CLIENT)
public class LayerCreatureScrolling extends LayerCreatureEffect {
	public LayerCreatureScrolling(CreatureRenderer renderer, String textureSuffix, boolean glow, int blending, boolean subspecies, Vector2f scrollSpeed) {
		super(renderer, textureSuffix, glow, blending, subspecies);
		this.scrollSpeed = scrollSpeed;
	}
}
