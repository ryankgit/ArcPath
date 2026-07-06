package com.arcpath.trajectory;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TrajectorySimulator {

    public record TrajectorySimulationResult(List<Vec3> points, Optional<Vec3> landing, Optional<Direction> landingFace) {
    }

    private static final int MAX_TICKS = 240;

    public static Optional<TrajectorySimulationResult> simulate(LocalPlayer player, Level level) {
        var heldItem = player.getMainHandItem();
        var type = ProjectileType.fromItem(heldItem.getItem());
        if (type.isEmpty())
            return Optional.empty();
        if (isChargeWeapon(heldItem) && !player.isUsingItem())
            return Optional.empty();

        ProjectileType projectile = type.get();
        Vec3 pos = player.getEyePosition(1.0f);
        Vec3 velocity = player.getLookAngle().scale(projectile.speed);

        List<Vec3> points = new ArrayList<>();
        points.add(pos);

        Vec3 landing = null;
        Direction landingFace = null;

        for (int tick = 0; tick < MAX_TICKS; tick++) {
            Vec3 nextPos = computeNextPos(pos, velocity, projectile);

            Optional<Entity> hitEntity = checkEntityCollision(player, level, pos, nextPos);
            if (hitEntity.isPresent()) {
                landing = entityLandingPoint(hitEntity.get());
                landingFace = travelDirection(pos, nextPos);
                points.add(landing);
                break;
            }

            var clip = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, player));
            if (clip.getType() != HitResult.Type.MISS) {
                landing = clip.getLocation();
                if (clip instanceof BlockHitResult blockHit)
                    landingFace = blockHit.getDirection();
                points.add(landing);
                break;
            }

            velocity = applyPhysics(velocity, projectile);
            pos = nextPos;
            points.add(pos);
        }

        return Optional.of(new TrajectorySimulationResult(points, Optional.ofNullable(landing), Optional.ofNullable(landingFace)));
    }

    private static boolean isChargeWeapon(ItemStack item) {
        return item.getItem() == Items.BOW || item.getItem() == Items.CROSSBOW || item.getItem() == Items.TRIDENT;
    }

    private static Vec3 applyPhysics(Vec3 velocity, ProjectileType projectile) {
        return new Vec3((velocity.x) * projectile.drag, (velocity.y - projectile.gravity) * projectile.drag, (velocity.z) * projectile.drag);
    }

    private static Vec3 computeNextPos(Vec3 pos, Vec3 velocity, ProjectileType projectile) {
        Vec3 nextPos = applyPhysics(velocity, projectile);
        return pos.add(nextPos);
    }

    private static Vec3 entityLandingPoint(Entity entity) {
        AABB box = entity.getBoundingBox();
        return new Vec3(box.getCenter().x, box.maxY, box.getCenter().z);
    }

    private static Direction travelDirection(Vec3 pos, Vec3 nextPos) {
        Vec3 travelDir = nextPos.subtract(pos).normalize();
        return Direction.stream()
                .max(Comparator.comparingDouble(
                        d -> travelDir.dot(new Vec3(d.getStepX(), d.getStepY(), d.getStepZ()))))
                .orElse(Direction.UP);
    }

    private static Optional<Entity> checkEntityCollision(LocalPlayer player, Level level, Vec3 pos, Vec3 nextPos) {
        AABB segmentBox = new AABB(pos, nextPos).inflate(0.5);
        Optional<Entity> foundEntity = level.getEntities(player, segmentBox, entity -> entity.isAlive()
                && entity != player
                && entity.getBoundingBox().inflate(0.3).clip(pos, nextPos).isPresent())
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.getBoundingBox().inflate(0.3)
                        .clip(pos, nextPos)
                        .map(v -> v.distanceTo(pos))
                        .orElse(Double.MAX_VALUE)));

        // An entity that cannot be hit by projectiles should not have their collision considered when rendering target marker
        if (foundEntity.isPresent() && foundEntity.get().canBeHitByProjectile())
            return foundEntity;

        return Optional.empty();
    }
}