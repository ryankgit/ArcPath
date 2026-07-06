package com.arcpath.trajectory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.Optional;

import com.arcpath.config.ArcPathConfig;
import com.arcpath.config.ArcPathConfig.ThrowableSettings;

public enum ProjectileType {
    ENDER_PEARL(1.5f, 0.03f, 0.99f),
    SNOWBALL(1.5f, 0.03f, 0.99f),
    EGG(1.5f, 0.03f, 0.99f),
    TRIDENT(2.5f, 0.05f, 0.99f),
    ARROW(3.0f, 0.05f, 0.99f);

    public final float speed;
    public final float gravity;
    public final float drag;

    ProjectileType(float speed, float gravity, float drag) {
        this.speed = speed;
        this.gravity = gravity;
        this.drag = drag;
    }

    public ThrowableSettings settings() {
        ArcPathConfig cfg = ArcPathConfig.get();
        return switch (this) {
            case ENDER_PEARL -> cfg.enderPearl;
            case SNOWBALL -> cfg.snowball;
            case EGG -> cfg.egg;
            case TRIDENT -> cfg.trident;
            case ARROW -> cfg.arrow;
        };
    }

    public static Optional<ProjectileType> fromItem(Item item) {
        if (item == null)
            return Optional.empty();

        String id = BuiltInRegistries.ITEM.getKey(item).getPath();

        return switch (id) {
            case "ender_pearl" -> Optional.of(ENDER_PEARL);
            case "snowball" -> Optional.of(SNOWBALL);
            case "egg" -> Optional.of(EGG);
            case "trident" -> Optional.of(TRIDENT);
            case "bow" -> Optional.of(ARROW);
            case "crossbow" -> Optional.of(ARROW);
            default -> Optional.empty();
        };
    }
}