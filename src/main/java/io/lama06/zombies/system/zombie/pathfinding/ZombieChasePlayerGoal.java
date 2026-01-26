package io.lama06.zombies.system.zombie.pathfinding;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;

public final class ZombieChasePlayerGoal extends Goal {
    private final Mob mob;
    private final double speed;
    private final double chaseRange;
    private Player target;
    private int retargetCooldown;
    private int stuckTicks;
    private double lastX, lastY, lastZ;

    public ZombieChasePlayerGoal(final Mob mob, final double speed, final double range) {
        this.mob = mob;
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
        retargetCooldown = 0;
        stuckTicks = 0;
        recordPosition();
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (--retargetCooldown <= 0) {
            retargetCooldown = 10;
            mob.getNavigation().moveTo(target, speed);
        }

        checkStuck();
    }

    private void checkStuck() {
        final double dx = mob.getX() - lastX;
        final double dy = mob.getY() - lastY;
        final double dz = mob.getZ() - lastZ;
        final double moved = dx * dx + dy * dy + dz * dz;

        if (moved < 0.01) {
            stuckTicks++;
            if (stuckTicks > 40) {
                unstuck();
                stuckTicks = 0;
            }
        } else {
            stuckTicks = 0;
        }
        recordPosition();
    }

    private void unstuck() {
        mob.getNavigation().stop();
        mob.setDeltaMovement(
            (Math.random() - 0.5) * 0.5,
            0.2,
            (Math.random() - 0.5) * 0.5
        );
        mob.getNavigation().recomputePath();
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
