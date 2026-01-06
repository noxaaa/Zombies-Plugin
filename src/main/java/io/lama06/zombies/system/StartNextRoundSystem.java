package io.lama06.zombies.system;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.*;
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
            checkRoundComplete(world);
        }
    }

    private void checkRoundComplete(final ZombiesWorld world) {
        final WorldConfig config = world.getConfig();
        if (config == null || config.rounds.isEmpty()) {
            return;
        }

        final List<Zombie> zombies = world.getZombies();
        if (!zombies.isEmpty()) {
            return;
        }

        final Integer currentRound = world.get(ZombiesWorld.ROUND);
        final Integer triggeredWaves = world.get(ZombiesWorld.TRIGGERED_WAVES);

        if (currentRound == null || currentRound < 1 || currentRound > config.rounds.size()) {
            return;
        }

        final RoundConfig roundConfig = config.rounds.get(currentRound - 1);
        final int totalWaves = roundConfig.waves.size();

        if (triggeredWaves == null || triggeredWaves < totalWaves) {
            return;
        }

        final int nextRound = currentRound + 1;
        if (nextRound > config.rounds.size()) {
            world.endGame();
            return;
        }

        startRound(world, nextRound);
    }

    private void startRound(final ZombiesWorld world, final int round) {
        world.set(ZombiesWorld.ROUND, round);
        world.set(ZombiesWorld.ROUND_START_TIME, world.getBukkit().getGameTime());
        world.set(ZombiesWorld.TRIGGERED_WAVES, 0);

        final Title title = Title.title(
                Component.text("Round " + round).color(NamedTextColor.RED),
                Component.empty(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500))
        );
        for (final ZombiesPlayer player : world.getPlayers()) {
            player.getBukkit().showTitle(title);
            player.getBukkit().playSound(player.getBukkit().getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        }

        Bukkit.getPluginManager().callEvent(new StartRoundEvent(world, round));
    }
}
