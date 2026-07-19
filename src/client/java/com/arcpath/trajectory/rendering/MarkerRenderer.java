package com.arcpath.trajectory.rendering;

import com.arcpath.config.ArcPathConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class MarkerRenderer {

    private MarkerRenderer() {
    }

    public static void draw(Vec3 pos, Direction face, PoseStack poseStack, VertexConsumer buffer, ArcPathConfig.TargetSettings target) {

        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();

        float[] rgba = RenderUtils.extractMarkerColor(target);
        float[] normal = RenderUtils.faceNormal(face);
        float[][] axes = RenderUtils.faceAxes(face);

        float centerX = (float) pos.x + normal[0];
        float centerY = (float) pos.y + normal[1];
        float centerZ = (float) pos.z + normal[2];

        // multiplying configured targetSize by 2 to scale to real size range while allowing config slider to be 1 - 100
        int size = target.targetSize * 2;
        float radius = size / 100.0f;
        int segments = size > 100 ? 64 : 32;

        switch (target.shape) {
            case CIRCLE -> {
                drawCircle(buffer, matrix, pose, centerX, centerY, centerZ, axes, radius, segments, normal, rgba,
                        target.lineWidth);
                drawCross(buffer, matrix, pose, centerX, centerY, centerZ, axes, radius, normal, rgba,
                        target.lineWidth);
            }
            case DIAMOND -> drawDiamond(buffer, matrix, pose, centerX, centerY, centerZ, axes, radius, normal, rgba,
                    target.lineWidth);
            case SQUARE -> drawSquare(buffer, matrix, pose, centerX, centerY, centerZ, axes, radius, normal, rgba,
                    target.lineWidth);
        }
    }

    private static void drawCircle(VertexConsumer buffer, Matrix4f matrix, PoseStack.Pose pose,
            float centerX, float centerY, float centerZ, float[][] axes, float radius, int segments,
            float[] normal, float[] rgba, float lineWidth) {

        float[] uAxis = axes[0];
        float[] vAxis = axes[1];

        for (int segment = 0; segment < segments; segment++) {
            float angle1 = (float) (2 * Math.PI * segment / segments);
            float angle2 = (float) (2 * Math.PI * (segment + 1) / segments);

            float cosAngle1 = (float) Math.cos(angle1) * radius;
            float sinAngle1 = (float) Math.sin(angle1) * radius;
            float cosAngle2 = (float) Math.cos(angle2) * radius;
            float sinAngle2 = (float) Math.sin(angle2) * radius;

            float startX = centerX + uAxis[0] * cosAngle1 + vAxis[0] * sinAngle1;
            float startY = centerY + uAxis[1] * cosAngle1 + vAxis[1] * sinAngle1;
            float startZ = centerZ + uAxis[2] * cosAngle1 + vAxis[2] * sinAngle1;

            float endX = centerX + uAxis[0] * cosAngle2 + vAxis[0] * sinAngle2;
            float endY = centerY + uAxis[1] * cosAngle2 + vAxis[1] * sinAngle2;
            float endZ = centerZ + uAxis[2] * cosAngle2 + vAxis[2] * sinAngle2;

            RenderUtils.addLine(buffer, matrix, pose,
                    startX, startY, startZ, endX, endY, endZ,
                    normal[0], normal[1], normal[2],
                    rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);
        }
    }

    private static void drawDiamond(VertexConsumer buffer, Matrix4f matrix, PoseStack.Pose pose,
            float centerX, float centerY, float centerZ, float[][] axes, float radius,
            float[] normal, float[] rgba, float lineWidth) {

        float[] u = axes[0];
        float[] v = axes[1];

        float topX = centerX + v[0] * radius, topY = centerY + v[1] * radius, topZ = centerZ + v[2] * radius;
        float bottomX = centerX - v[0] * radius, bottomY = centerY - v[1] * radius, bottomZ = centerZ - v[2] * radius;
        float leftX = centerX - u[0] * radius, leftY = centerY - u[1] * radius, leftZ = centerZ - u[2] * radius;
        float rightX = centerX + u[0] * radius, rightY = centerY + u[1] * radius, rightZ = centerZ + u[2] * radius;

        RenderUtils.addLine(buffer, matrix, pose, topX, topY, topZ, rightX, rightY, rightZ, normal[0], normal[1],
                normal[2], rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);
        RenderUtils.addLine(buffer, matrix, pose, rightX, rightY, rightZ, bottomX, bottomY, bottomZ, normal[0],
                normal[1], normal[2], rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);
        RenderUtils.addLine(buffer, matrix, pose, bottomX, bottomY, bottomZ, leftX, leftY, leftZ, normal[0], normal[1],
                normal[2], rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);
        RenderUtils.addLine(buffer, matrix, pose, leftX, leftY, leftZ, topX, topY, topZ, normal[0], normal[1],
                normal[2], rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);

        drawCross(buffer, matrix, pose, centerX, centerY, centerZ, axes, radius, normal, rgba, lineWidth);
    }

    private static void drawSquare(VertexConsumer buffer, Matrix4f matrix, PoseStack.Pose pose,
            float centerX, float centerY, float centerZ, float[][] axes, float radius,
            float[] normal, float[] rgba, float lineWidth) {

        float[] u = axes[0];
        float[] v = axes[1];

        float x1 = centerX + u[0] * radius + v[0] * radius;
        float y1 = centerY + u[1] * radius + v[1] * radius;
        float z1 = centerZ + u[2] * radius + v[2] * radius;

        float x2 = centerX - u[0] * radius + v[0] * radius;
        float y2 = centerY - u[1] * radius + v[1] * radius;
        float z2 = centerZ - u[2] * radius + v[2] * radius;

        float x3 = centerX - u[0] * radius - v[0] * radius;
        float y3 = centerY - u[1] * radius - v[1] * radius;
        float z3 = centerZ - u[2] * radius - v[2] * radius;

        float x4 = centerX + u[0] * radius - v[0] * radius;
        float y4 = centerY + u[1] * radius - v[1] * radius;
        float z4 = centerZ + u[2] * radius - v[2] * radius;

        RenderUtils.addLine(buffer, matrix, pose, x1, y1, z1, x2, y2, z2, normal[0], normal[1], normal[2], rgba[0],
                rgba[1], rgba[2], rgba[3], lineWidth);
        RenderUtils.addLine(buffer, matrix, pose, x2, y2, z2, x3, y3, z3, normal[0], normal[1], normal[2], rgba[0],
                rgba[1], rgba[2], rgba[3], lineWidth);
        RenderUtils.addLine(buffer, matrix, pose, x3, y3, z3, x4, y4, z4, normal[0], normal[1], normal[2], rgba[0],
                rgba[1], rgba[2], rgba[3], lineWidth);
        RenderUtils.addLine(buffer, matrix, pose, x4, y4, z4, x1, y1, z1, normal[0], normal[1], normal[2], rgba[0],
                rgba[1], rgba[2], rgba[3], lineWidth);

        drawCross(buffer, matrix, pose, centerX, centerY, centerZ, axes, radius, normal, rgba, lineWidth);
    }

    private static void drawCross(VertexConsumer buffer, Matrix4f matrix, PoseStack.Pose pose,
            float centerX, float centerY, float centerZ, float[][] axes, float radius,
            float[] normal, float[] rgba, float lineWidth) {

        float[] uAxis = axes[0];
        float[] vAxis = axes[1];

        RenderUtils.addLine(buffer, matrix, pose,
                centerX - uAxis[0] * radius, centerY - uAxis[1] * radius, centerZ - uAxis[2] * radius,
                centerX + uAxis[0] * radius, centerY + uAxis[1] * radius, centerZ + uAxis[2] * radius,
                normal[0], normal[1], normal[2],
                rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);

        RenderUtils.addLine(buffer, matrix, pose,
                centerX - vAxis[0] * radius, centerY - vAxis[1] * radius, centerZ - vAxis[2] * radius,
                centerX + vAxis[0] * radius, centerY + vAxis[1] * radius, centerZ + vAxis[2] * radius,
                normal[0], normal[1], normal[2],
                rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);
    }
}