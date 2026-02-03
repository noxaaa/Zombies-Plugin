package io.lama06.zombies.system.zombie.pathfinding;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Simple goal for Bee to rush towards the nearest alive player.
 */
public final class BeeRushPlayerGoal extends Goal {
    private final Mob mob;
    private final ZombiesWorld world;
    private final double speed;

    public BeeRushPlayerGoal(final Mob mob, final ZombiesWorld world, final double speed) {
        this.mob = mob;
        this.world = world;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return !world.getAlivePlayers().isEmpty();
    }

    @Override
    public void tick() {
        final ZombiesPlayer target = world.getAlivePlayers().stream()
                .min(Comparator.comparingDouble(p ->
                        p.getBukkit().getLocation().distanceSquared(mob.getBukkitEntity().getLocation())))
                .orElse(null);

        if (target == null) {
            mob.getNavigation().stop();
            return;
        }

        final Location targetLoc = target.getBukkit().getLocation();
        mob.getLookControl().setLookAt(targetLoc.getX(), targetLoc.getY() + 1, targetLoc.getZ());
        mob.getNavigation().moveTo(targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(), speed);
    }

    @Override
    public boolean canContinueToUse() {
        return !world.getAlivePlayers().isEmpty();
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
