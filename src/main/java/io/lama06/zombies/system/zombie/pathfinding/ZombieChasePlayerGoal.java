package io.lama06.zombies.system.zombie.pathfinding;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.zombie.Zombie;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Chase player using flow field pathfinding for complex terrain navigation.
 * Works at any distance - flow field handles long-range pathfinding.
 */
public final class ZombieChasePlayerGoal extends Goal {
    private static final double DIRECT_CHASE_DISTANCE_SQ = 25.0; // 5 blocks squared

    private final Mob mob;
    private final Zombie zombie;
    private final double speed;
    private int stuckTicks;
    private double lastX, lastY, lastZ;

    public ZombieChasePlayerGoal(final Mob mob, final Zombie zombie, final double speed) {
        this.mob = mob;
        this.zombie = zombie;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Always active if there are alive players - flow field handles distance
        return !zombie.getWorld().getAlivePlayers().isEmpty();
    }

    @Override
    public void start() {
        stuckTicks = 0;
        recordPosition();
    }

    @Override
    public void tick() {
        final ZombiesWorld world = zombie.getWorld();
        final Location zombieLoc = new Location(
            world.getBukkit(),
            mob.getX(), mob.getY(), mob.getZ()
        );

        // Find nearest player for looking direction
        final ZombiesPlayer nearestPlayer = world.getAlivePlayers().stream()
            .min(Comparator.comparingDouble(p ->
                p.getBukkit().getLocation().distanceSquared(zombieLoc)))
            .orElse(null);

        if (nearestPlayer != null) {
            final Location playerLoc = nearestPlayer.getBukkit().getLocation();
            mob.getLookControl().setLookAt(playerLoc.getX(), playerLoc.getY() + 1, playerLoc.getZ());

            final double distanceSq = zombieLoc.distanceSquared(playerLoc);

            // When close enough, use direct navigation
            if (distanceSq < DIRECT_CHASE_DISTANCE_SQ) {
                mob.getNavigation().moveTo(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(), speed);
            } else {
                // Use flow field for long-distance navigation
                navigateWithFlowField(zombieLoc, world);
            }
        } else {
            // No players, just use flow field
            navigateWithFlowField(zombieLoc, world);
        }

        checkStuck();
    }

    private void navigateWithFlowField(final Location zombieLoc, final ZombiesWorld world) {
        final Vector direction = FlowFieldManager.INSTANCE.getDirectionToNearestPlayer(zombieLoc, world);

        if (direction != null) {
            // Move in the flow field direction
            final double targetX = mob.getX() + direction.getX() * 2;
            final double targetY = mob.getY() + direction.getY() * 2;
            final double targetZ = mob.getZ() + direction.getZ() * 2;

            mob.getNavigation().moveTo(targetX, targetY, targetZ, speed);
        }
        // If no flow field data, navigation will be idle until field is calculated
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
        // Continue as long as there are alive players
        return !zombie.getWorld().getAlivePlayers().isEmpty();
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
