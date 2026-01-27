package io.lama06.zombies.system.zombie.break_window;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.Window;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.data.Component;
import io.lama06.zombies.zombie.BreakWindowData;
import io.lama06.zombies.zombie.Zombie;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public final class StartWindowBreakingSystem implements Listener {
    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final Zombie zombie : ZombiesPlugin.INSTANCE.getZombies()) {
            startZombie(zombie);
        }
    }

    private void startZombie(final Zombie zombie) {
        final Component breakWindowComponent = zombie.getComponent(Zombie.BREAK_WINDOW);
        if (breakWindowComponent == null) {
            return;
        }
        final BreakWindowData breakWindowData = zombie.getData().breakWindow;
        final int time = breakWindowData.time();
        final double maxDistance = breakWindowData.maxDistance();
        final BlockPosition block = breakWindowComponent.get(BreakWindowData.BLOCK);
        if (block != null) {
            return;
        }

        // Find the spawn window for this zombie
        final Window spawnWindow = findSpawnWindow(zombie);
        if (spawnWindow == null) {
            return;
        }

        // If the zombie can already reach the repair area, don't need to break more
        if (canReachRepairArea(zombie, spawnWindow)) {
            return;
        }

        final NearestWindowBlockResult nearestWindowBlock = getNearestWindowBlock(zombie, spawnWindow, maxDistance);
        if (nearestWindowBlock == null) {
            return;
        }
        breakWindowComponent.set(BreakWindowData.BLOCK, nearestWindowBlock.position());
        breakWindowComponent.set(BreakWindowData.REMAINING_TIME, time);
    }

    /**
     * Check if the zombie can pathfind to the repair area using Paper's Pathfinder API.
     * If findPath returns a valid result, the window is passable.
     */
    private boolean canReachRepairArea(final Zombie zombie, final Window spawnWindow) {
        if (spawnWindow.repairArea == null) {
            return false;
        }

        // Use Paper's Bukkit Pathfinder API
        if (!(zombie.getEntity() instanceof final Mob bukkitMob)) {
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
    private Window findSpawnWindow(final Zombie zombie) {
        final BlockPosition spawnLoc = zombie.get(Zombie.SPAWN_LOCATION);
        if (spawnLoc == null) {
            return null;
        }

        final ZombiesWorld world = zombie.getWorld();
        final Vector spawnVector = new Vector(
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

    private record NearestWindowBlockResult(BlockPosition position, double distance) { }

    /**
     * Find the nearest unbroken block in the spawn window only.
     */
    private NearestWindowBlockResult getNearestWindowBlock(final Zombie zombie, final Window spawnWindow, final double maxDistance) {
        if (spawnWindow.blocks == null) {
            return null;
        }

        final ZombiesWorld world = zombie.getWorld();
        final Location zombieLocation = zombie.getEntity() instanceof final LivingEntity living
                ? living.getEyeLocation()
                : zombie.getEntity().getLocation();

        BlockPosition nearestWindowBlock = null;
        double smallestDistance = Double.POSITIVE_INFINITY;

        for (final BlockPosition windowBlockPos : spawnWindow.blocks.getBlocks()) {
            final Block block = windowBlockPos.toLocation(world.getBukkit()).getBlock();
            if (block.getType() == Material.AIR) {
                continue;
            }
            final double distance = windowBlockPos.toCenter().toVector().distance(zombieLocation.toVector());
            if (distance < smallestDistance && distance <= maxDistance) {
                nearestWindowBlock = windowBlockPos;
                smallestDistance = distance;
            }
        }

        if (nearestWindowBlock == null) {
            return null;
        }
        return new NearestWindowBlockResult(nearestWindowBlock, smallestDistance);
    }
}
