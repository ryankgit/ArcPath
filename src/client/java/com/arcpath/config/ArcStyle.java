package com.arcpath.config;

public enum ArcStyle {
    DASHED,
    SOLID;

    @Override
    public String toString() {
        return switch (this) {
            case DASHED -> "Dashed";
            case SOLID  -> "Solid";
        };
    }
}
