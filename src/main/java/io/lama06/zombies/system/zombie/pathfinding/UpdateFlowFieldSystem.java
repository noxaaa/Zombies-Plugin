package io.lama06.zombies.system.zombie.pathfinding;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.StartRoundEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Updates flow fields for all players in active zombie games.
 */
public final class UpdateFlowFieldSystem implements Listener {
    private int tickCounter = 0;

    /**
     * Pre-calculate flow fields at round start (synchronously).
     * This ensures zombies have valid pathfinding data immediately when they spawn.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onRoundStart(final StartRoundEvent event) {
        FlowFieldManager.INSTANCE.preCalculateForAllPlayers(event.getWorld());
    }

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        // Update every 20 ticks (1 second)
        if (++tickCounter < 20) {
            return;
        }
        tickCounter = 0;

        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            for (final ZombiesPlayer player : world.getAlivePlayers()) {
                FlowFieldManager.INSTANCE.updateForPlayer(player, ZombiesPlugin.INSTANCE);
            }
        }
    }
}
