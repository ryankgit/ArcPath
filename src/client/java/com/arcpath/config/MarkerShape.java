package com.arcpath.config;

public enum MarkerShape {
    CIRCLE,
    DIAMOND,
    SQUARE;

    @Override
    public String toString() {
        return switch (this) {
            case CIRCLE  -> "Circle";
            case DIAMOND -> "Diamond";
            case SQUARE  -> "Square";
        };
    }
}
