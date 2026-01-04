package io.lama06.zombies.system.zombie;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.RoundConfig;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class MakeZombiesGlowSystem implements Listener {
    @EventHandler
    private void onTick(final ServerTickEndEvent event) {
        for (final ZombiesWorld gameWorld : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            final WorldConfig config = gameWorld.getConfig();
            if (config == null || config.rounds.isEmpty()) {
                continue;
            }
            final Integer round = gameWorld.get(ZombiesWorld.ROUND);
            final Integer triggeredWaves = gameWorld.get(ZombiesWorld.TRIGGERED_WAVES);
            if (round == null || triggeredWaves == null || round < 1 || round > config.rounds.size()) {
                continue;
            }
            final RoundConfig roundConfig = config.rounds.get(round - 1);
            if (triggeredWaves < roundConfig.waves.size()) {
                continue;
            }
            for (final Zombie zombie : gameWorld.getZombies()) {
                final Entity entity = zombie.getEntity();
                if (entity.isGlowing()) {
                    continue;
                }
                entity.setGlowing(true);
            }
        }
    }
}
