package io.lama06.zombies.system.player.revive;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class SpawnPlayerCorpseSystem implements Listener {
    @EventHandler
    private void onPlayerDeath(final PlayerDeathEvent event) {
        final ZombiesPlayer player = new ZombiesPlayer(event.getPlayer());
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }

        // Send notification message
        world.sendMessage(Component.text((PlayerCorpse.TIME / 20) + " seconds left to revive ")
                .append(player.getBukkit().displayName())
                .color(NamedTextColor.RED));

        // Create the corpse NPC with invisible hitbox
        final CorpseData corpse = PlayerCorpseNPC.createCorpse(
                player.getBukkit(),
                player.getBukkit().getLocation()
        );

        // Register the corpse in the plugin tracker
        ZombiesPlugin.INSTANCE.addCorpse(player.getBukkit().getUniqueId(), corpse);
    }
}
