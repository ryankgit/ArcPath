package com.arcpath.trajectory;

import com.arcpath.trajectory.TrajectorySimulator.TrajectorySimulationResult;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TrajectoryCache {

    // How many degrees the look angle must change to trigger a recalculation
    private static final float LOOK_THRESHOLD = 0.5f;

    // How many blocks the player must move to trigger a recalculation
    private static final float MOVE_THRESHOLD = 0.1f;

    // How many ticks to wait before forcing a recalculation regardless
    private static final int MAX_CACHE_TICKS = 10;

    private Vec3 smoothedVelocity = Vec3.ZERO;

    private TrajectorySimulationResult cachedResult;
    private ProjectileType cachedProjectileType;

    private float cachedYaw;
    private float cachedPitch;
    private Vec3 cachedPos;
    private int ticksSinceCached = 0;

    public TrajectorySimulationResult get(LocalPlayer player, Level level, ProjectileType type) {
        ticksSinceCached++;

        if (shouldRecalculate(player, type)) {
            Vec3 currentVelocity = player.getDeltaMovement();
            double x = lerp(smoothedVelocity.x, currentVelocity.x);
            double y = lerp(smoothedVelocity.y, currentVelocity.y);
            double z = lerp(smoothedVelocity.z, currentVelocity.z);
            smoothedVelocity = new Vec3(x, y, z);

            cachedResult = TrajectorySimulator.simulate(player, level, smoothedVelocity).orElse(null);
            cachedProjectileType = type;
            cachedYaw = player.getYRot();
            cachedPitch = player.getXRot();
            cachedPos = player.position();
            ticksSinceCached = 0;
        }

        return cachedResult;
    }

    public void invalidate() {
        cachedResult = null;
        cachedProjectileType = null;
        cachedPos = null;
        ticksSinceCached = MAX_CACHE_TICKS;
    }

    private boolean shouldRecalculate(LocalPlayer player, ProjectileType type) {
        if (cachedResult == null)
            return true;
        if (cachedPos == null)
            return true;
        if (type != cachedProjectileType)
            return true;
        if (ticksSinceCached >= MAX_CACHE_TICKS)
            return true;

        float yawDelta = Math.abs(player.getYRot() - cachedYaw);
        float pitchDelta = Math.abs(player.getXRot() - cachedPitch);
        double moveDelta = player.position().distanceTo(cachedPos);

        return yawDelta > LOOK_THRESHOLD || pitchDelta > LOOK_THRESHOLD || moveDelta > MOVE_THRESHOLD;
    }

    // Linear Interpolation
    private static double lerp(double a, double b) {
        return a + (b - a) * 0.2;
    }
}