package com.lycanitesmobs.core.block;

import net.minecraft.resources.Identifier;

public interface BlockTypeGetter {
    Identifier getRegistryName();

    void setRegistryName(Identifier registryName);
}
