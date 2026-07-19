package com.arcpath.trajectory.rendering;

import com.arcpath.config.ArcPathConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class ArcRenderer {

    private static final int SKIP_POINTS = 5;

    private ArcRenderer() {
    }

    public static void draw(List<Vec3> points, PoseStack poseStack,
            VertexConsumer buffer, ArcPathConfig.ArcSettings arc) {
        switch (arc.style) {
            case DASHED -> drawDashed(points, poseStack, buffer, arc);
            case SOLID -> drawSolid(points, poseStack, buffer, arc);
        }
    }

    private static void drawSolid(List<Vec3> points, PoseStack poseStack,
            VertexConsumer buffer, ArcPathConfig.ArcSettings arc) {

        Matrix4f mat = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();
        int totalPoints = points.size();

        for (int i = SKIP_POINTS; i < totalPoints - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);
            Vec3 dir = p2.subtract(p1).normalize();
            float[] rgba = gradientColor(arc, i - SKIP_POINTS, totalPoints - SKIP_POINTS);

            RenderUtils.addLine(buffer, mat, pose,
                    (float) p1.x, (float) p1.y, (float) p1.z,
                    (float) p2.x, (float) p2.y, (float) p2.z,
                    (float) dir.x, (float) dir.y, (float) dir.z,
                    rgba[0], rgba[1], rgba[2], rgba[3], arc.lineWidth);
        }
    }

    private static void drawDashed(List<Vec3> points, PoseStack poseStack,
            VertexConsumer buffer, ArcPathConfig.ArcSettings arc) {

        Matrix4f mat = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();
        int segmentPhase = 0;
        int totalPoints = points.size();

        for (int i = SKIP_POINTS; i < totalPoints - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);

            if (segmentPhase < arc.dashLength) {
                Vec3 dir = p2.subtract(p1).normalize();
                float[] rgba = gradientColor(arc, i - SKIP_POINTS, totalPoints - SKIP_POINTS);

                RenderUtils.addLine(buffer, mat, pose,
                        (float) p1.x, (float) p1.y, (float) p1.z,
                        (float) p2.x, (float) p2.y, (float) p2.z,
                        (float) dir.x, (float) dir.y, (float) dir.z,
                        rgba[0], rgba[1], rgba[2], rgba[3], arc.lineWidth);
            }

            segmentPhase++;
            if (segmentPhase >= arc.dashLength + arc.gapLength)
                segmentPhase = 0;
        }
    }

    /**
     * Returns the interpolated RGBA color for a given point along the arc.
     * When gradient is disabled, returns the base arc color.
     * When gradient is enabled, lerps from arc.color to arc.gradientColor
     * based on the point's position along the arc (0=start, 1=end).
     */
    private static float[] gradientColor(ArcPathConfig.ArcSettings arc, int index, int total) {
        float alpha = 1.0f - (arc.transparency / 100.0f);

        int baseColor = arc.color.getColor();
        float r = ((baseColor >> 16) & 0xFF) / 255f;
        float g = ((baseColor >> 8) & 0xFF) / 255f;
        float b = ((baseColor) & 0xFF) / 255f;

        if (!arc.gradientEnabled || total <= 0) {
            return new float[] { r, g, b, alpha };
        }

        int gradColor = arc.gradientColor.getColor();
        float gr = ((gradColor >> 16) & 0xFF) / 255f;
        float gg = ((gradColor >> 8) & 0xFF) / 255f;
        float gb = ((gradColor) & 0xFF) / 255f;

        float t = (float) index / total;

        return new float[] {
                RenderUtils.lerp(r, gr, t),
                RenderUtils.lerp(g, gg, t),
                RenderUtils.lerp(b, gb, t),
                alpha
        };
    }
}