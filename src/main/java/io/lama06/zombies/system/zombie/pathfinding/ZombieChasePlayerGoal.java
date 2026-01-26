package io.lama06.zombies.system.zombie.pathfinding;

import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.zombie.Zombie;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.List;

/**
 * Chase player using flow field pathfinding for complex terrain navigation.
 * Falls back to vanilla navigation when close to target.
 */
public final class ZombieChasePlayerGoal extends Goal {
    private static final double DIRECT_CHASE_DISTANCE = 5.0; // Use vanilla nav when this close

    private final Mob mob;
    private final Zombie zombie;
    private final double speed;
    private final double chaseRange;
    private Player target;
    private int stuckTicks;
    private double lastX, lastY, lastZ;

    public ZombieChasePlayerGoal(final Mob mob, final Zombie zombie, final double speed, final double range) {
        this.mob = mob;
        this.zombie = zombie;
        this.speed = speed;
        this.chaseRange = range;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        final List<Player> players = mob.level().getEntitiesOfClass(
            Player.class,
            mob.getBoundingBox().inflate(chaseRange),
            p -> !p.isSpectator() && p.isAlive()
        );
        if (!players.isEmpty()) {
            target = players.stream()
                .min((a, b) -> Double.compare(
                    mob.distanceToSqr(a), mob.distanceToSqr(b)))
                .orElse(null);
            return target != null;
        }
        return false;
    }

    @Override
    public void start() {
        stuckTicks = 0;
        recordPosition();
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        final double distanceToTarget = mob.distanceTo(target);

        // When close enough, use direct navigation
        if (distanceToTarget < DIRECT_CHASE_DISTANCE) {
            mob.getNavigation().moveTo(target, speed);
        } else {
            // Use flow field for long-distance navigation
            navigateWithFlowField();
        }

        checkStuck();
    }

    private void navigateWithFlowField() {
        final Location zombieLoc = new Location(
            zombie.getWorld().getBukkit(),
            mob.getX(), mob.getY(), mob.getZ()
        );

        final ZombiesWorld world = zombie.getWorld();
        final Vector direction = FlowFieldManager.INSTANCE.getDirectionToNearestPlayer(zombieLoc, world);

        if (direction != null) {
            // Move in the flow field direction
            final double targetX = mob.getX() + direction.getX() * 2;
            final double targetY = mob.getY() + direction.getY() * 2;
            final double targetZ = mob.getZ() + direction.getZ() * 2;

            mob.getNavigation().moveTo(targetX, targetY, targetZ, speed);
        } else if (target != null) {
            // Fallback to vanilla navigation if no flow field data
            mob.getNavigation().moveTo(target, speed);
        }
    }

    private void checkStuck() {
        final double dx = mob.getX() - lastX;
        final double dy = mob.getY() - lastY;
        final double dz = mob.getZ() - lastZ;
        final double moved = dx * dx + dy * dy + dz * dz;

        if (moved < 0.01) {
            stuckTicks++;
            if (stuckTicks > 60) { // 3 seconds stuck
                unstuck();
                stuckTicks = 0;
            }
        } else {
            stuckTicks = 0;
        }
        recordPosition();
    }

    private void unstuck() {
        // Try jumping and moving in a random direction
        mob.getNavigation().stop();
        mob.setDeltaMovement(
            (Math.random() - 0.5) * 0.6,
            0.3,
            (Math.random() - 0.5) * 0.6
        );
    }

    private void recordPosition() {
        lastX = mob.getX();
        lastY = mob.getY();
        lastZ = mob.getZ();
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && !target.isSpectator()
            && mob.distanceToSqr(target) < chaseRange * chaseRange;
    }

    @Override
    public void stop() {
        target = null;
        mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
