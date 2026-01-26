package io.lama06.zombies.system.zombie.pathfinding;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Updates flow fields for all players in active zombie games.
 */
public final class UpdateFlowFieldSystem implements Listener {
    private int tickCounter = 0;

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        // Update every 10 ticks (0.5 seconds)
        if (++tickCounter < 10) {
            return;
        }
        tickCounter = 0;

        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            for (final ZombiesPlayer player : world.getAlivePlayers()) {
                FlowFieldManager.INSTANCE.updateForPlayer(player);
            }
        }
    }
}
