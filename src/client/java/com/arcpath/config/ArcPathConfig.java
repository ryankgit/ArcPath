package com.arcpath.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.math.Color;

@Config(name = "arcpath")
public class ArcPathConfig implements ConfigData {

    public ThrowableSettings enderPearl = new ThrowableSettings();
    public ThrowableSettings snowball = new ThrowableSettings();
    public ThrowableSettings egg = new ThrowableSettings();
    public ThrowableSettings trident = new ThrowableSettings();
    public ThrowableSettings arrow = new ThrowableSettings();

    // Default ThrowableSettings
    public static class ThrowableSettings {
        public boolean enabled = true;
        public ArcSettings arc = new ArcSettings();
        public TargetSettings target = new TargetSettings();
    }

    // Default ArcSettings
    public static class ArcSettings {
        public boolean enabled = true;
        public Color color = Color.ofOpaque(0xFFFFFF);
        public int lineWidth = 6;
        public int dashLength = 3;
        public int gapLength = 3;
        public int transparency = 0;
    }

    // Default TargetSettings
    public static class TargetSettings {
        public boolean enabled = true;
        public Color color = Color.ofOpaque(0xFFFFFF);
        public int lineWidth = 6;
        public int targetSize = 20;
        public int transparency = 0;
    }

    public static ArcPathConfig get() {
        return AutoConfig.getConfigHolder(ArcPathConfig.class).getConfig();
    }

    public static void register() {
        AutoConfig.register(ArcPathConfig.class, GsonConfigSerializer::new);
    }

    public void save() {
        AutoConfig.getConfigHolder(ArcPathConfig.class).save();
    }
}