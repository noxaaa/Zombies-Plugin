package io.lama06.zombies.system;

import io.lama06.zombies.*;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.zombie.Zombie;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.List;

public final class CleanupAfterGameSystem implements Listener {
    private static final int TITLE_DURATION_SECONDS = 10;

    @EventHandler
    private void onGameEnd(final GameEndEvent event) {
        final ZombiesWorld world = event.getWorld();
        final WorldConfig config = world.getConfig();

        // Show title to all players
        showEndGameTitle(world, event.isVictory());

        // Kill all zombies immediately
        final List<Zombie> zombies = world.getZombies();
        for (final Zombie zombie : zombies) {
            zombie.getEntity().remove();
        }

        // Delay the cleanup by 10 seconds
        Bukkit.getScheduler().runTaskLater(ZombiesPlugin.INSTANCE, () -> {
            cleanupWorld(world, config);
        }, TITLE_DURATION_SECONDS * 20L);
    }

    private void showEndGameTitle(final ZombiesWorld world, final boolean victory) {
        final Component titleText;
        final Component subtitleText;

        if (victory) {
            titleText = Component.text("YOU WON").color(NamedTextColor.GREEN);
            subtitleText = Component.text("Congratulations!").color(NamedTextColor.GOLD);
        } else {
            titleText = Component.text("GAME OVER").color(NamedTextColor.RED);
            subtitleText = Component.text("Better luck next time!").color(NamedTextColor.GRAY);
        }

        final Title title = Title.title(
                titleText,
                subtitleText,
                Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofSeconds(TITLE_DURATION_SECONDS - 1),
                        Duration.ofMillis(500)
                )
        );

        for (final ZombiesPlayer player : world.getPlayers()) {
            player.getBukkit().showTitle(title);
        }
    }

    private void cleanupWorld(final ZombiesWorld world, final WorldConfig config) {

        for (final Door door : config.doors) {
            door.setOpen(world, false);
        }
        if (config.powerSwitch != null) {
            config.powerSwitch.setActive(world, false);
        }
        for (final Window window : config.windows) {
            window.close(world);
        }

        world.remove(ZombiesWorld.ROUND);
        world.remove(ZombiesWorld.ROUND_START_TIME);
        world.remove(ZombiesWorld.TRIGGERED_WAVES);
        world.remove(ZombiesWorld.REACHABLE_AREAS);
        world.remove(ZombiesWorld.OPEN_DOORS);
        world.remove(ZombiesWorld.POWER_SWITCH);
        world.remove(ZombiesWorld.DRAGONS_WRATH_USED);
        world.removeComponent(ZombiesWorld.PERKS_COMPONENT);

        for (final ZombiesPlayer player : world.getPlayers()) {
            player.remove(ZombiesPlayer.GAME_ID);
            player.remove(ZombiesPlayer.GOLD);
            player.remove(ZombiesPlayer.KILLS);
            player.clearPerks();
            final Player playerBukkit = player.getBukkit();
            playerBukkit.setGameMode(GameMode.ADVENTURE);
            playerBukkit.teleport(world.getBukkit().getSpawnLocation());
            playerBukkit.getInventory().clear();
            playerBukkit.setLevel(0);
            playerBukkit.setExp(0);
            playerBukkit.clearActivePotionEffects();
            playerBukkit.setHealth(20);
            playerBukkit.setFoodLevel(20);
            playerBukkit.setFireTicks(0);
        }
    }
}
