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
    private static final double SLIME_HORIZONTAL_SPEED = 0.22;
    private static final double SLIME_JUMP_VELOCITY = 0.42;
    private static final int SLIME_JUMP_COOLDOWN_TICKS = 5 * 20;

    private final Mob mob;
    private final Zombie zombie;
    private final double speed;
    private int stuckTicks;
    private double lastX, lastY, lastZ;
    private int nextSlimeJumpTick;

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
        nextSlimeJumpTick = 0;
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
            Location chaseTarget = playerLoc;
            mob.getLookControl().setLookAt(playerLoc.getX(), playerLoc.getY() + 1, playerLoc.getZ());

            final double distanceSq = zombieLoc.distanceSquared(playerLoc);

            // When close enough, use direct navigation
            if (distanceSq < DIRECT_CHASE_DISTANCE_SQ) {
                mob.getNavigation().moveTo(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(), speed);
            } else {
                // Use flow field for long-distance navigation
                chaseTarget = navigateWithFlowField(zombieLoc, world, playerLoc);
            }

            // Slime/MagmaCube navigation can become idle after replacing vanilla goals.
            // Apply a directional jump/velocity fallback to guarantee active chasing.
            if (mob.getBukkitEntity() instanceof org.bukkit.entity.Slime) {
                moveSlimeTowards(zombieLoc, chaseTarget);
            }
        }

        checkStuck();
    }

    private Location navigateWithFlowField(final Location zombieLoc, final ZombiesWorld world, final Location playerLoc) {
        final Vector direction = FlowFieldManager.INSTANCE.getDirectionToNearestPlayer(zombieLoc, world);

        if (direction != null) {
            // Move in the flow field direction
            final double targetX = mob.getX() + direction.getX() * 2;
            final double targetY = mob.getY() + direction.getY() * 2;
            final double targetZ = mob.getZ() + direction.getZ() * 2;

            mob.getNavigation().moveTo(targetX, targetY, targetZ, speed);
            return new Location(world.getBukkit(), targetX, targetY, targetZ);
        } else {
            // Fallback: move towards player direction (won't avoid obstacles, but stuck detection will handle it)
            final Vector toPlayer = playerLoc.toVector().subtract(zombieLoc.toVector()).normalize();
            final double targetX = mob.getX() + toPlayer.getX() * 3;
            final double targetY = mob.getY();
            final double targetZ = mob.getZ() + toPlayer.getZ() * 3;

            mob.getNavigation().moveTo(targetX, targetY, targetZ, speed);
            return new Location(world.getBukkit(), targetX, targetY, targetZ);
        }
    }

    private void moveSlimeTowards(final Location from, final Location to) {
        final Vector horizontal = to.toVector().subtract(from.toVector());
        horizontal.setY(0);
        if (horizontal.lengthSquared() < 0.0001) {
            return;
        }

        horizontal.normalize().multiply(SLIME_HORIZONTAL_SPEED * speed);
        if (mob.onGround()) {
            if (mob.tickCount < nextSlimeJumpTick) {
                return;
            }
            nextSlimeJumpTick = mob.tickCount + SLIME_JUMP_COOLDOWN_TICKS;
            mob.setDeltaMovement(horizontal.getX(), SLIME_JUMP_VELOCITY, horizontal.getZ());
            return;
        }

        mob.setDeltaMovement(horizontal.getX(), mob.getDeltaMovement().y, horizontal.getZ());
    }

    private void checkStuck() {
        final double dx = mob.getX() - lastX;
        final double dy = mob.getY() - lastY;
        final double dz = mob.getZ() - lastZ;
        final double moved = dx * dx + dy * dy + dz * dz;

        if (moved < 0.01) {
            stuckTicks++;
            final int stuckThreshold = mob.getBukkitEntity() instanceof org.bukkit.entity.Slime
                    ? SLIME_JUMP_COOLDOWN_TICKS + 20
                    : 60;
            if (stuckTicks > stuckThreshold) { // 3s for normal mobs, longer for slime-like jump cooldown
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
