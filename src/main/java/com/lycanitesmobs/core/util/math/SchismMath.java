package com.lycanitesmobs.core.util.math;

/**
 * Pure math helpers for Schism-owned rules that do not need Minecraft utility classes.
 */
public final class SchismMath {
    private SchismMath() {
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double lerp(double scale, double min, double max) {
        return min + scale * (max - min);
    }

    public static double horizontalDistance(double x, double z) {
        return Math.sqrt(x * x + z * z);
    }

    public static double horizontalDistanceAtLeast(double x, double z, double minimum) {
        return Math.max(horizontalDistance(x, z), minimum);
    }
}
