package io.lama06.zombies.system.zombie.pathfinding;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Ranged attack goal for ranged/throwing zombies.
 * Maintains distance and performs ranged attacks.
 */
public final class ZombieRangedAttackGoal extends Goal {
    private static final int LOS_POSITION_SAMPLES = 12;

    private final Mob mob;
    private final double attackRange;
    private final double preferredRange;
    private final int attackCooldown;
    private final RangedAttackExecutor executor;

    private Player target;
    private int cooldownRemaining;

    @FunctionalInterface
    public interface RangedAttackExecutor {
        void attack(Mob mob, Player target);
    }

    public ZombieRangedAttackGoal(final Mob mob, final double attackRange, final double preferredRange,
                                   final int cooldown, final RangedAttackExecutor executor) {
        this.mob = mob;
        this.attackRange = attackRange;
        this.preferredRange = preferredRange;
        this.attackCooldown = cooldown;
        this.executor = executor;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        final List<Player> players = mob.level().getEntitiesOfClass(
            Player.class,
            mob.getBoundingBox().inflate(attackRange),
            p -> !p.isSpectator() && p.isAlive()
        );
        target = players.stream()
            .min(Comparator.comparingDouble(mob::distanceToSqr))
            .orElse(null);
        return target != null;
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        final double distance = mob.distanceTo(target);
        final boolean hasLineOfSight = mob.getSensing().hasLineOfSight(target);

        if (!hasLineOfSight) {
            moveToVisiblePosition();
        } else if (distance > attackRange) {
            mob.getNavigation().moveTo(target, 1.0);
        } else {
            mob.getNavigation().stop();
        }

        if (--cooldownRemaining <= 0 && distance <= attackRange && hasLineOfSight) {
            executor.attack(mob, target);
            cooldownRemaining = attackCooldown;
        }
    }

    private void moveToVisiblePosition() {
        final double radius = Math.max(2.0, preferredRange);
        final double angleOffset = (mob.tickCount % LOS_POSITION_SAMPLES) * (Math.PI * 2.0 / LOS_POSITION_SAMPLES);

        double bestX = Double.NaN;
        double bestY = 0;
        double bestZ = 0;
        double bestDistanceSq = Double.MAX_VALUE;

        for (int i = 0; i < LOS_POSITION_SAMPLES; i++) {
            final double angle = angleOffset + (Math.PI * 2.0 * i / LOS_POSITION_SAMPLES);
            final double candidateX = target.getX() + Math.cos(angle) * radius;
            final double candidateY = target.getY();
            final double candidateZ = target.getZ() + Math.sin(angle) * radius;

            if (!canSeeTargetFrom(candidateX, candidateY, candidateZ)) {
                continue;
            }

            final double distanceSq = mob.distanceToSqr(candidateX, candidateY, candidateZ);
            if (distanceSq >= bestDistanceSq) {
                continue;
            }

            bestDistanceSq = distanceSq;
            bestX = candidateX;
            bestY = candidateY;
            bestZ = candidateZ;
        }

        if (Double.isNaN(bestX)) {
            // Fallback: keep chasing target until line of sight opens.
            mob.getNavigation().moveTo(target, 1.0);
            return;
        }

        mob.getNavigation().moveTo(bestX, bestY, bestZ, 1.0);
    }

    private boolean canSeeTargetFrom(final double x, final double y, final double z) {
        final Vec3 from = new Vec3(x, y + mob.getEyeHeight(), z);
        final Vec3 to = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        final HitResult hitResult = mob.level().clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mob
        ));
        return hitResult.getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && !target.isSpectator()
            && mob.distanceToSqr(target) < attackRange * attackRange;
    }

    @Override
    public void stop() {
        target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
