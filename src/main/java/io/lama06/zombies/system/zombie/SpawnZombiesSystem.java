package io.lama06.zombies.system.zombie;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.*;
import io.lama06.zombies.zombie.ZombieType;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;

public final class SpawnZombiesSystem implements Listener {
    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            spawnWaves(world);
        }
    }

    private void spawnWaves(final ZombiesWorld world) {
        final WorldConfig config = world.getConfig();
        if (config == null || config.rounds.isEmpty()) {
            return;
        }

        final Integer round = world.get(ZombiesWorld.ROUND);
        final Long roundStartTime = world.get(ZombiesWorld.ROUND_START_TIME);
        final Integer triggeredWaves = world.get(ZombiesWorld.TRIGGERED_WAVES);

        if (round == null || roundStartTime == null || triggeredWaves == null) {
            return;
        }

        if (round < 1 || round > config.rounds.size()) {
            return;
        }

        final RoundConfig roundConfig = config.rounds.get(round - 1);
        final List<Wave> waves = roundConfig.waves;

        if (triggeredWaves >= waves.size()) {
            return;
        }

        final long currentTime = world.getBukkit().getGameTime();
        final long elapsedTicks = currentTime - roundStartTime;

        for (int i = triggeredWaves; i < waves.size(); i++) {
            final Wave wave = waves.get(i);
            if (elapsedTicks >= wave.delayTicks) {
                spawnWave(world, wave);
                world.set(ZombiesWorld.TRIGGERED_WAVES, i + 1);
            } else {
                break;
            }
        }
    }

    private void spawnWave(final ZombiesWorld world, final Wave wave) {
        for (final Map.Entry<ZombieType, Integer> entry : wave.zombies.entrySet()) {
            final ZombieType type = entry.getKey();
            final int count = entry.getValue();
            final int health = wave.getHealthForType(type);
            final double speed = wave.getSpeedForType(type);
            final double damage = wave.getDamageForType(type);
            final double defense = wave.getDefenseForType(type);
            for (int i = 0; i < count; i++) {
                final Location spawnPoint = world.getNextZombieSpawnPoint();
                if (spawnPoint == null) {
                    continue;
                }
                world.spawnZombie(spawnPoint, type, health, speed, damage, defense);
            }
        }
    }
}
