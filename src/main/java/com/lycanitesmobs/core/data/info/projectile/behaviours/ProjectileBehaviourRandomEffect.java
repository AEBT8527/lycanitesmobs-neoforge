package com.lycanitesmobs.core.data.info.projectile.behaviours;

import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.core.registries.BuiltInRegistries;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ProjectileBehaviourRandomEffect extends ProjectileBehaviour {
    /**
     * A list of beneficial potion effects that this element can grant.
     **/
    protected List<String> effects = new ArrayList<>();

    /**
     * The duration (in ticks) of the random effect.
     **/
    protected int duration = 100;

    /**
     * The random effect amplifier.
     **/
    protected int amplifier = 0;

    @Override
    public void loadFromJSON(JsonObject json) {
        if (json.has("effects"))
            this.effects = JSONHelper.getJsonStrings(json.get("effects").getAsJsonArray());
    }

    @Override
    public void onProjectileDamage(BaseProjectileEntity projectile, Level world, LivingEntity target, float damage) {
        if (projectile.getOwner() == null || damage <= 0) {
            return;
        }

        if (this.duration <= 0 || this.amplifier < 0) {
            return;
        }

        int randomIndex = world.getRandom().nextInt(this.effects.size());
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.getValue(Identifier.parse(this.effects.get(randomIndex)));
        if (effect != null) {
            target.addEffect(new MobEffectInstance(ObjectManager.holder(effect), this.duration, this.amplifier));
        }
    }
}
