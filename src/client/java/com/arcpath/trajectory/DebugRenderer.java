package com.arcpath.trajectory;

import com.arcpath.ArcPathState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class DebugRenderer {
    private DebugRenderer() { }

    private static final int MAX_HISTORY = 50;
    private static final int LINE_COLOR = 0xFFFF0000;

    private static final Deque<Vec3> landingHistory = new ArrayDeque<>();

    public static void register() {
        LevelRenderEvents.END_MAIN.register(DebugRenderer::onWorldRender);
    }

    public static void recordLanding(Vec3 landing) {
        if (landing == null)
            return;
        landingHistory.addLast(landing);
        if (landingHistory.size() > MAX_HISTORY)
            landingHistory.removeFirst();
    }

    public static void clear() {
        landingHistory.clear();
    }

    private static void onWorldRender(LevelRenderContext ctx) {
        if (!ArcPathState.isDebugMode())
            return;

        if (landingHistory.size() < 2)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        Vec3 cam = ctx.gameRenderer().getMainCamera().position();

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();

        int color = LINE_COLOR;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = ((color) & 0xFF) / 255f;

        MultiBufferSource.BufferSource bufferSource = ctx.bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(RenderTypes.LINES);

        List<Vec3> points = new ArrayList<>(landingHistory);
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);

            // Draw slightly above ground to avoid z-fighting
            float y1 = (float) p1.y + 0.02f;
            float y2 = (float) p2.y + 0.02f;

            Vec3 dir = p2.subtract(p1).normalize();
            float nx = (float) dir.x;
            float ny = (float) dir.y;
            float nz = (float) dir.z;

            lines.addVertex(matrix, (float) p1.x, y1, (float) p1.z)
                    .setColor(r, g, b, 1f).setNormal(pose, nx, ny, nz).setLineWidth(3.0f);
            lines.addVertex(matrix, (float) p2.x, y2, (float) p2.z)
                    .setColor(r, g, b, 1f).setNormal(pose, nx, ny, nz).setLineWidth(3.0f);
        }

        // Draw a dot at each landing point
         for (Vec3 point : points)
            drawDot(lines, matrix, pose, (float) point.x, (float) point.y + 0.02f, (float) point.z, r, g, b);

        bufferSource.endBatch(RenderTypes.LINES);
        poseStack.popPose();
    }

    private static void drawDot(VertexConsumer lines, Matrix4f matrix, PoseStack.Pose pose, float x, float y, float z, float r, float g, float b) {
        float s = 0.1f;
        // Small cross at each point
        lines.addVertex(matrix, x - s, y, z).setColor(r, g, b, 1f).setNormal(pose, 1, 0, 0).setLineWidth(3.0f);
        lines.addVertex(matrix, x + s, y, z).setColor(r, g, b, 1f).setNormal(pose, 1, 0, 0).setLineWidth(3.0f);
        lines.addVertex(matrix, x, y, z - s).setColor(r, g, b, 1f).setNormal(pose, 0, 0, 1).setLineWidth(3.0f);
        lines.addVertex(matrix, x, y, z + s).setColor(r, g, b, 1f).setNormal(pose, 0, 0, 1).setLineWidth(3.0f);
    }
}