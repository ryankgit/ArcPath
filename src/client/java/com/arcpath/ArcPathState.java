package com.arcpath;

public class ArcPathState {
    private ArcPathState() { }

    private static boolean enabled = true;
    private static boolean debugMode = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void toggleEnabled() {
        enabled = !enabled;
    }

    public static void toggleDebugMode() {
        debugMode = !debugMode;
    }
}