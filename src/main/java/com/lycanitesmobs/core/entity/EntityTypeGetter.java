package com.lycanitesmobs.core.entity;

import net.minecraft.resources.Identifier;

public interface EntityTypeGetter {
    Identifier getRegistryName();
    void setRegistryName(Identifier registryName);
}
