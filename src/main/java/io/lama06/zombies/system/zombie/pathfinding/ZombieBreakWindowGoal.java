package io.lama06.zombies.system.zombie.pathfinding;

import com.destroystokyo.paper.entity.Pathfinder;
import io.lama06.zombies.Window;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.data.Component;
import io.lama06.zombies.zombie.BreakWindowData;
import io.lama06.zombies.zombie.Zombie;
import io.papermc.paper.math.BlockPosition;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumSet;

public final class ZombieBreakWindowGoal extends Goal {
    private final Mob mob;
    private final Zombie zombie;
    private final double maxWindowDistance;
    private BlockPosition targetWindow;
    private boolean isBreaking;
    private Window spawnWindow; // The specific window this zombie spawned at

    public ZombieBreakWindowGoal(final Mob mob, final Zombie zombie, final double maxDistance) {
        this.mob = mob;
        this.zombie = zombie;
        this.maxWindowDistance = maxDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (zombie.getData().breakWindow == null) {
            return false;
        }

        // Find the spawn window once
        if (spawnWindow == null) {
            spawnWindow = findSpawnWindow();
            if (spawnWindow == null) {
                return false;
            }
        }
        if (!spawnWindow.needBreak) {
            return false;
        }

        // If the zombie can already reach the repair area, don't need to break more
        if (canReachRepairArea()) {
            return false;
        }

        final Component comp = zombie.getComponent(Zombie.BREAK_WINDOW);
        if (comp != null && comp.get(BreakWindowData.REMAINING_TIME) != null) {
            isBreaking = true;
            return true;
        }

        targetWindow = findNearestBlockInSpawnWindow();
        return targetWindow != null;
    }

    /**
     * Check if the zombie can pathfind to the repair area using Paper's Pathfinder API.
     * If findPath returns a valid result, the window is passable.
     */
    private boolean canReachRepairArea() {
        if (spawnWindow == null || spawnWindow.repairArea == null) {
            return false;
        }

        // Use Paper's Bukkit Pathfinder API
        if (!(zombie.getEntity() instanceof final org.bukkit.entity.Mob bukkitMob)) {
            return false;
        }

        final Pathfinder pathfinder = bukkitMob.getPathfinder();

        // Get center of repair area as target
        final BlockPosition lower = spawnWindow.repairArea.getLowerCorner();
        final BlockPosition upper = spawnWindow.repairArea.getUpperCorner();
        final double targetX = (lower.blockX() + upper.blockX()) / 2.0 + 0.5;
        final double targetY = lower.blockY();
        final double targetZ = (lower.blockZ() + upper.blockZ()) / 2.0 + 0.5;

        final Location targetLoc = new Location(zombie.getWorld().getBukkit(), targetX, targetY, targetZ);

        // findPath returns null if no path exists
        final Pathfinder.PathResult path = pathfinder.findPath(targetLoc);
        return path != null;
    }

    /**
     * Find the window that this zombie spawned at by finding the window
     * with blocks closest to the spawn location.
     */
    private Window findSpawnWindow() {
        final BlockPosition spawnLoc = zombie.get(Zombie.SPAWN_LOCATION);
        if (spawnLoc == null) {
            return null;
        }

        final ZombiesWorld world = zombie.getWorld();
        final org.bukkit.util.Vector spawnVector = new org.bukkit.util.Vector(
            spawnLoc.blockX() + 0.5, spawnLoc.blockY(), spawnLoc.blockZ() + 0.5
        );

        Window closest = null;
        double minDist = Double.MAX_VALUE;

        for (final Window window : world.getConfig().windows) {
            if (window.blocks == null) continue;

            // Find the minimum distance from spawn to any block in this window
            for (final BlockPosition blockPos : window.blocks.getBlocks()) {
                final double dist = blockPos.toCenter().toVector().distance(spawnVector);
                if (dist < minDist) {
                    minDist = dist;
                    closest = window;
                }
            }
        }

        return closest;
    }

    @Override
    public void start() {
        if (!isBreaking && targetWindow != null) {
            navigateToWindow();
        }
    }

    @Override
    public void tick() {
        final Component comp = zombie.getComponent(Zombie.BREAK_WINDOW);
        if (comp != null && comp.get(BreakWindowData.REMAINING_TIME) != null) {
            mob.getNavigation().stop();
            isBreaking = true;
            if (targetWindow != null) {
                mob.getLookControl().setLookAt(
                    targetWindow.blockX() + 0.5,
                    targetWindow.blockY() + 0.5,
                    targetWindow.blockZ() + 0.5
                );
            }
            return;
        }

        isBreaking = false;

        if (targetWindow != null && mob.getNavigation().isDone()) {
            navigateToWindow();
        }
    }

    private void navigateToWindow() {
        if (targetWindow == null) {
            return;
        }
        mob.getNavigation().moveTo(
            targetWindow.blockX() + 0.5,
            targetWindow.blockY(),
            targetWindow.blockZ() + 0.5,
            1.0
        );
    }

    /**
     * Find the nearest unbroken block in the spawn window only.
     */
    private BlockPosition findNearestBlockInSpawnWindow() {
        if (spawnWindow == null || spawnWindow.blocks == null) {
            return null;
        }

        final ZombiesWorld world = zombie.getWorld();
        final Location zombieLoc = zombie.getEntity().getLocation();

        BlockPosition nearest = null;
        double minDist = Double.MAX_VALUE;

        for (final BlockPosition pos : spawnWindow.blocks.getBlocks()) {
            final Block block = pos.toLocation(world.getBukkit()).getBlock();
            if (block.getType() == Material.AIR) {
                continue; // Already broken
            }

            final double dist = pos.toCenter().toVector().distance(zombieLoc.toVector());
            if (dist < minDist && dist <= maxWindowDistance) {
                minDist = dist;
                nearest = pos;
            }
        }
        return nearest;
    }

    @Override
    public boolean canContinueToUse() {
        if (spawnWindow != null && !spawnWindow.needBreak) {
            return false;
        }
        // If the zombie can now reach the repair area, stop breaking
        if (canReachRepairArea()) {
            return false;
        }

        if (isBreaking) {
            return true;
        }
        if (targetWindow == null) {
            return false;
        }

        final Block block = targetWindow.toLocation(zombie.getWorld().getBukkit()).getBlock();
        return block.getType() != Material.AIR;
    }

    @Override
    public void stop() {
        targetWindow = null;
        isBreaking = false;
    }
}
