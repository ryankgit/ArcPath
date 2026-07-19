package com.arcpath.trajectory.rendering;

import com.arcpath.config.ArcPathConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public class RenderUtils {

    private RenderUtils() {
    }

    public static void addLine(
            VertexConsumer buffer, Matrix4f matrix, PoseStack.Pose pose,
            float startX, float startY, float startZ,
            float endX, float endY, float endZ,
            float normalX, float normalY, float normalZ,
            float r, float g, float b, float a, float lineWidth) {

        buffer.addVertex(matrix, startX, startY, startZ).setColor(r, g, b, a).setNormal(pose, normalX, normalY, normalZ)
                .setLineWidth(lineWidth);
        buffer.addVertex(matrix, endX, endY, endZ).setColor(r, g, b, a).setNormal(pose, normalX, normalY, normalZ)
                .setLineWidth(lineWidth);
    }

    public static float[] faceNormal(Direction face) {
        return new float[] {
                face.getStepX() * 0.1f,
                face.getStepY() * 0.1f,
                face.getStepZ() * 0.1f
        };
    }

    public static float[][] faceAxes(Direction face) {
        return switch (face.getAxis()) {
            case Y -> new float[][] { { 1, 0, 0 }, { 0, 0, 1 } };
            case X -> new float[][] { { 0, 1, 0 }, { 0, 0, 1 } };
            default -> new float[][] { { 1, 0, 0 }, { 0, 1, 0 } };
        };
    }

    public static float[] extractArcColor(ArcPathConfig.ArcSettings arc) {
        int color = arc.color.getColor();
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = ((color) & 0xFF) / 255f;
        float alpha = 1.0f - (arc.transparency / 100.0f);
        return new float[] { red, green, blue, alpha };
    }

    public static float[] extractMarkerColor(ArcPathConfig.TargetSettings target) {
        int color = target.color.getColor();
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = ((color) & 0xFF) / 255f;
        float alpha = 1.0f - (target.transparency / 100.0f);
        return new float[] { red, green, blue, alpha };
    }

    // Linearly interpolates between two values, used to smooth rapid changes over time
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}