package io.lama06.zombies.system.zombie.pathfinding;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Ranged attack goal for ranged/throwing zombies.
 * Maintains distance and performs ranged attacks.
 */
public final class ZombieRangedAttackGoal extends Goal {
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

        if (distance < preferredRange * 0.8) {
            retreatFromTarget();
        } else if (distance > preferredRange * 1.2) {
            mob.getNavigation().moveTo(target, 1.0);
        } else {
            mob.getNavigation().stop();
        }

        if (--cooldownRemaining <= 0 && distance <= attackRange) {
            executor.attack(mob, target);
            cooldownRemaining = attackCooldown;
        }
    }

    private void retreatFromTarget() {
        final double dx = mob.getX() - target.getX();
        final double dz = mob.getZ() - target.getZ();
        final double length = Math.sqrt(dx * dx + dz * dz);
        if (length > 0) {
            final double normX = dx / length;
            final double normZ = dz / length;
            mob.getNavigation().moveTo(
                mob.getX() + normX * 3,
                mob.getY(),
                mob.getZ() + normZ * 3,
                1.0
            );
        }
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
