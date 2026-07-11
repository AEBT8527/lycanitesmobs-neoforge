package com.lycanitesmobs.core.mixin;

import com.lycanitesmobs.core.entity.EntityTypeGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = EntityType.class, remap = false)
public class EntityTypeMixin implements EntityTypeGetter {
    @Unique
    public Identifier registryName;

    public void setRegistryName(Identifier registryName) {
        this.registryName = registryName;
    }

    public Identifier getRegistryName() {
        return registryName;
    }
}
