package io.lama06.zombies.system;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.SpawnRate;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.StartRoundEvent;
import io.lama06.zombies.zombie.Zombie;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.List;

public final class StartNextRoundSystem implements Listener {
    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            final List<Zombie> zombies = world.getZombies();
            if (!zombies.isEmpty()) {
                continue;
            }
            final int currentRound = world.get(ZombiesWorld.ROUND);
            final SpawnRate currentSpawnRate = SpawnRate.SPAWN_RATES.get(currentRound - 1);
            final int remainingZombies = world.get(ZombiesWorld.REMAINING_ZOMBIES);
            final boolean bossSpawned = world.get(ZombiesWorld.BOSS_SPAWNED);
            if (remainingZombies > 0 || (currentSpawnRate.boss() != null && !bossSpawned)) {
                continue;
            }
            final int nextRound = currentRound + 1;
            if (nextRound > SpawnRate.SPAWN_RATES.size()) {
                world.endGame();
                continue;
            }
            final SpawnRate spawnRate = SpawnRate.SPAWN_RATES.get(nextRound - 1);
            world.set(ZombiesWorld.ROUND, nextRound);
            world.set(ZombiesWorld.NEXT_ZOMBIE_TIME, spawnRate.spawnDelay());
            world.set(ZombiesWorld.REMAINING_ZOMBIES, spawnRate.getNumberOfZombies());
            world.set(ZombiesWorld.BOSS_SPAWNED, false);

            // 显示红色标题和播放音效
            final Title title = Title.title(
                    Component.text("Round").color(NamedTextColor.RED),
                    Component.text(String.valueOf(nextRound)).color(NamedTextColor.RED),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500))
            );
            for (final ZombiesPlayer player : world.getPlayers()) {
                player.getBukkit().showTitle(title);
                player.getBukkit().playSound(player.getBukkit().getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
            }

            Bukkit.getPluginManager().callEvent(new StartRoundEvent(world, currentRound + 1));
        }
    }
}
