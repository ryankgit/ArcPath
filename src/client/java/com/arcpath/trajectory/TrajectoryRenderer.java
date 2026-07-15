package com.arcpath.trajectory;

import com.arcpath.ArcPathState;
import com.arcpath.config.ArcPathConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import java.util.List;

public class TrajectoryRenderer {

    private static final TrajectoryCache CachedTrajectory = new TrajectoryCache();

    private TrajectoryRenderer() { }

    public static void register() {
        LevelRenderEvents.END_MAIN.register(TrajectoryRenderer::onWorldRender);
    }

    private static void onWorldRender(LevelRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        if (mc.options.hideGui)
            return;

        if (!ArcPathState.isEnabled()) 
            return;

        // Disable entirely when player is submerged
        // TODO: deal with water physics
        if (mc.player.isUnderWater())
            return;

        var heldItem = mc.player.getMainHandItem();
        var type = ProjectileType.fromItem(heldItem.getItem());
        if (type.isEmpty()) {
            CachedTrajectory.invalidate();
            return;
        }

        var throwableSettings = type.get().settings();
        if (!throwableSettings.enabled) {
            CachedTrajectory.invalidate();
            return;
        }

        var sim = CachedTrajectory.get(mc.player, mc.level, type.get());
        if (sim == null) 
            return;

        PoseStack poseStack = new PoseStack();
        Vec3 cam = ctx.gameRenderer().getMainCamera().position();

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        MultiBufferSource.BufferSource bufferSource = ctx.bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(RenderTypes.LINES);

        if (throwableSettings.arc.enabled)
            drawDashedLine(sim.points(), poseStack, lines, throwableSettings.arc);

        if (throwableSettings.target.enabled) {
            Direction landingFaceDirection = sim.landingFace().orElse(Direction.UP);
            sim.landing().ifPresent(
                    landing -> drawLandingMarker(landing, landingFaceDirection, poseStack, lines,
                            throwableSettings.target));
        }

        // END_MAIN requires explicitly flushing the batch
        bufferSource.endBatch(RenderTypes.LINES);
        poseStack.popPose();
    }

    private static void drawDashedLine(List<Vec3> points, PoseStack poseStack, VertexConsumer buffer, ArcPathConfig.ArcSettings arc) {

        int dashLength = arc.dashLength;
        int gapLength = arc.gapLength;
        float width = arc.lineWidth;
        float[] rgba = extractColor(arc);

        Matrix4f mat = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();
        int segmentPhase = 0;

        for (int i = 5; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);

            if (segmentPhase < dashLength) {
                Vec3 dir = p2.subtract(p1).normalize();
                addLine(buffer, mat, pose,
                        (float) p1.x, (float) p1.y, (float) p1.z,
                        (float) p2.x, (float) p2.y, (float) p2.z,
                        (float) dir.x, (float) dir.y, (float) dir.z,
                        rgba[0], rgba[1], rgba[2], rgba[3], width);
            }

            segmentPhase++;
            if (segmentPhase >= dashLength + gapLength)
                segmentPhase = 0;
        }
    }

    private static void drawLandingMarker(Vec3 pos, Direction face, PoseStack poseStack, VertexConsumer buffer,
            ArcPathConfig.TargetSettings target) {

        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();

        float[] rgba = extractMarkerColor(target);
        float[] normal = faceNormal(face);
        float[][] axes = faceAxes(face);

        float centerX = (float) pos.x + normal[0] * 0.02f;
        float centerY = (float) pos.y + normal[1] * 0.02f;
        float centerZ = (float) pos.z + normal[2] * 0.02f;

        // multiplying configured targetSize by 2 to scale to real size range while allowing config slider to be 1 - 100
        int size = target.targetSize * 2;
        float radius = size / 100.0f;
        int segments = size > 100 ? 64 : 32;

        drawCircle(buffer, matrix, pose, centerX, centerY, centerZ, axes, radius, segments, normal, rgba, target.lineWidth);
        drawCross(buffer, matrix, pose, centerX, centerY, centerZ, axes, radius, normal, rgba, target.lineWidth);
    }

    private static float[] extractColor(ArcPathConfig.ArcSettings arc) {
        int color = arc.color.getColor();
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = ((color) & 0xFF) / 255f;
        float alpha = 1.0f - (arc.transparency / 100.0f);
        return new float[] { red, green, blue, alpha };
    }

    private static float[] extractMarkerColor(ArcPathConfig.TargetSettings target) {
        int color = target.color.getColor();
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = ((color) & 0xFF) / 255f;
        float alpha = 1.0f - (target.transparency / 100.0f);
        return new float[] { red, green, blue, alpha };
    }

    private static float[] faceNormal(Direction face) {
        return new float[] {
                face.getStepX() * 0.1f,
                face.getStepY() * 0.1f,
                face.getStepZ() * 0.1f
        };
    }

    // Returns {ux, uy, uz, vx, vy, vz} — the two axes of the face plane
    private static float[][] faceAxes(Direction face) {
        return switch (face.getAxis()) {
            case Y -> new float[][] { { 1, 0, 0 }, { 0, 0, 1 } };
            case X -> new float[][] { { 0, 1, 0 }, { 0, 0, 1 } };
            default -> new float[][] { { 1, 0, 0 }, { 0, 1, 0 } };
        };
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

            addLine(buffer, matrix, pose, startX, startY, startZ, endX, endY, endZ,
                    normal[0], normal[1], normal[2],
                    rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);
        }
    }

    private static void drawCross(VertexConsumer buffer, Matrix4f matrix, PoseStack.Pose pose,
            float centerX, float centerY, float centerZ, float[][] axes, float radius,
            float[] normal, float[] rgba, float lineWidth) {

        float[] uAxis = axes[0];
        float[] vAxis = axes[1];

        addLine(buffer, matrix, pose,
                centerX - uAxis[0] * radius, centerY - uAxis[1] * radius, centerZ - uAxis[2] * radius,
                centerX + uAxis[0] * radius, centerY + uAxis[1] * radius, centerZ + uAxis[2] * radius,
                normal[0], normal[1], normal[2],
                rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);

        addLine(buffer, matrix, pose,
                centerX - vAxis[0] * radius, centerY - vAxis[1] * radius, centerZ - vAxis[2] * radius,
                centerX + vAxis[0] * radius, centerY + vAxis[1] * radius, centerZ + vAxis[2] * radius,
                normal[0], normal[1], normal[2],
                rgba[0], rgba[1], rgba[2], rgba[3], lineWidth);
    }

    private static void addLine(
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
}
