package io.lama06.zombies.system.zombie.pathfinding;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages flow fields for all players in zombies worlds.
 * Flow fields are recalculated asynchronously to avoid blocking the main thread.
 */
public final class FlowFieldManager {
    public static final FlowFieldManager INSTANCE = new FlowFieldManager();

    private static final long RECALCULATE_INTERVAL_MS = 2000; // Recalculate every 2 seconds
    private static final double MOVE_THRESHOLD = 3.0; // Recalculate if player moved more than 3 blocks

    private final Map<UUID, FlowField> flowFields = new ConcurrentHashMap<>();
    private final Map<UUID, Location> lastPlayerLocations = new ConcurrentHashMap<>();
    private final Set<UUID> calculating = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private FlowFieldManager() {}

    /**
     * Update flow field for a player if needed.
     * Calculation is done asynchronously.
     */
    public void updateForPlayer(final ZombiesPlayer player, final Plugin plugin) {
        final UUID playerId = player.getBukkit().getUniqueId();
        final Location currentLoc = player.getBukkit().getLocation();

        // Skip if already calculating for this player
        if (calculating.contains(playerId)) {
            return;
        }

        final FlowField existing = flowFields.get(playerId);
        final Location lastLoc = lastPlayerLocations.get(playerId);

        boolean needsRecalculate = false;

        if (existing == null) {
            needsRecalculate = true;
        } else if (existing.getAge() > RECALCULATE_INTERVAL_MS) {
            if (lastLoc == null || lastLoc.distance(currentLoc) > MOVE_THRESHOLD) {
                needsRecalculate = true;
            }
        }

        if (needsRecalculate) {
            calculating.add(playerId);
            lastPlayerLocations.put(playerId, currentLoc.clone());

            // Calculate async
            executor.submit(() -> {
                try {
                    final FlowField newField = new FlowField(currentLoc);
                    // Update on main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        flowFields.put(playerId, newField);
                        calculating.remove(playerId);
                    });
                } catch (final Exception e) {
                    calculating.remove(playerId);
                }
            });
        }
    }

    /**
     * Pre-calculate flow fields for all players synchronously.
     * Call this at round start to ensure zombies have valid data immediately.
     */
    public void preCalculateForAllPlayers(final ZombiesWorld world) {
        for (final ZombiesPlayer player : world.getAlivePlayers()) {
            final UUID playerId = player.getBukkit().getUniqueId();
            final Location currentLoc = player.getBukkit().getLocation();

            // Calculate synchronously
            final FlowField newField = new FlowField(currentLoc);
            flowFields.put(playerId, newField);
            lastPlayerLocations.put(playerId, currentLoc.clone());
            calculating.remove(playerId); // Clear any pending async calculation
        }
    }

    /**
     * Get the best direction for a zombie to move towards any player.
     * Returns the direction to the nearest reachable player.
     */
    public Vector getDirectionToNearestPlayer(final Location zombieLocation, final ZombiesWorld world) {
        Vector bestDirection = null;
        int bestDistance = Integer.MAX_VALUE;

        for (final ZombiesPlayer player : world.getAlivePlayers()) {
            final FlowField field = flowFields.get(player.getBukkit().getUniqueId());
            if (field == null) {
                continue;
            }

            final int distance = field.getDistance(zombieLocation);
            if (distance >= 0 && distance < bestDistance) {
                final Vector direction = field.getDirection(zombieLocation);
                if (direction != null) {
                    bestDistance = distance;
                    bestDirection = direction;
                }
            }
        }

        return bestDirection;
    }

    /**
     * Get the nearest player's flow field for a zombie.
     */
    public FlowField getNearestPlayerField(final Location zombieLocation, final ZombiesWorld world) {
        FlowField bestField = null;
        int bestDistance = Integer.MAX_VALUE;

        for (final ZombiesPlayer player : world.getAlivePlayers()) {
            final FlowField field = flowFields.get(player.getBukkit().getUniqueId());
            if (field == null) {
                continue;
            }

            final int distance = field.getDistance(zombieLocation);
            if (distance >= 0 && distance < bestDistance) {
                bestDistance = distance;
                bestField = field;
            }
        }

        return bestField;
    }

    /**
     * Remove flow field for a player (e.g., when they die or leave).
     */
    public void removePlayer(final UUID playerId) {
        flowFields.remove(playerId);
        lastPlayerLocations.remove(playerId);
    }

    /**
     * Clear all flow fields for a world (e.g., when game ends).
     */
    public void clearWorld(final ZombiesWorld world) {
        for (final ZombiesPlayer player : world.getPlayers()) {
            removePlayer(player.getBukkit().getUniqueId());
        }
    }
}
