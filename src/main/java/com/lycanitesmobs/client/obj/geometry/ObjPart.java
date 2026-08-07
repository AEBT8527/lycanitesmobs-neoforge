package com.lycanitesmobs.client.obj.geometry;


import com.lycanitesmobs.client.obj.material.Material;
import org.joml.Vector3f;

public class ObjPart {

    private final String name;
    private final String lowerName;
    private boolean cullBackfaces;
    public Mesh mesh;
    public Material material;
    public Vector3f center;

    public ObjPart(String name) {
        this.name = name;
        this.lowerName = name == null ? "" : name.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public String getLowerName() {
        return this.lowerName;
    }

    public boolean shouldCullBackfaces() {
        return this.cullBackfaces;
    }

    public void setCullBackfaces(boolean cullBackfaces) {
        this.cullBackfaces = cullBackfaces;
    }
}
