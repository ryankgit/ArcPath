package com.arcpath;

public class ArcPathState {
    private ArcPathState() { }

    private static boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }
}