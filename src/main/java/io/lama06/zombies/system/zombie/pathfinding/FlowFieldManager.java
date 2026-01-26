package io.lama06.zombies.system.zombie.pathfinding;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages flow fields for all players in zombies worlds.
 * Flow fields are recalculated periodically when players move.
 */
public final class FlowFieldManager {
    public static final FlowFieldManager INSTANCE = new FlowFieldManager();

    private static final long RECALCULATE_INTERVAL_MS = 1500; // Recalculate every 1.5 seconds
    private static final double MOVE_THRESHOLD = 3.0; // Recalculate if player moved more than 3 blocks

    private final Map<UUID, FlowField> flowFields = new ConcurrentHashMap<>();
    private final Map<UUID, Location> lastPlayerLocations = new ConcurrentHashMap<>();

    private FlowFieldManager() {}

    /**
     * Update flow field for a player if needed.
     * Should be called periodically (e.g., every tick or every few ticks).
     */
    public void updateForPlayer(final ZombiesPlayer player) {
        final UUID playerId = player.getBukkit().getUniqueId();
        final Location currentLoc = player.getBukkit().getLocation();

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
            flowFields.put(playerId, new FlowField(currentLoc));
            lastPlayerLocations.put(playerId, currentLoc.clone());
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
