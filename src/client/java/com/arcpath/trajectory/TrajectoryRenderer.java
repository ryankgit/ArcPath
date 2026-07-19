package com.arcpath.trajectory;

import com.arcpath.ArcPathState;
import com.arcpath.trajectory.rendering.ArcRenderer;
import com.arcpath.trajectory.rendering.MarkerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

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
            ArcRenderer.draw(sim.points(), poseStack, lines, throwableSettings.arc);

        if (throwableSettings.target.enabled) {
            Direction landingFaceDirection = sim.landingFace().orElse(Direction.UP);
            sim.landing().ifPresent(landing -> {
                DebugRenderer.recordLanding(landing);
                MarkerRenderer.draw(landing, landingFaceDirection, poseStack, lines,throwableSettings.target);
            });
        }

        // END_MAIN requires explicitly flushing the batch
        bufferSource.endBatch(RenderTypes.LINES);
        poseStack.popPose();
    }
}
