package io.lama06.zombies.system.player.revive;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class SpawnPlayerCorpseSystem implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player bukkit)) {
            return;
        }

        final ZombiesPlayer player = new ZombiesPlayer(bukkit);
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }

        // Only handle ADVENTURE mode players (alive players)
        if (bukkit.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        // Check if player is already downed
        if (ZombiesPlugin.INSTANCE.getCorpse(bukkit.getUniqueId()) != null) {
            event.setCancelled(true); // Downed players can't take more damage
            return;
        }

        // Check if this damage would kill the player
        final double healthAfterDamage = bukkit.getHealth() - event.getFinalDamage();
        if (healthAfterDamage > 0) {
            return; // Not fatal, let normal damage happen
        }

        // Cancel the fatal damage - player enters "downed" state instead of dying
        event.setCancelled(true);

        final Location deathLocation = bukkit.getLocation().clone();

        // Send notification message
        world.sendMessage(bukkit.displayName()
                .append(Component.text(" is downed! ").color(NamedTextColor.RED))
                .append(Component.text((PlayerCorpse.TIME / 20) + " seconds to revive").color(NamedTextColor.YELLOW)));

        // Create the corpse NPC
        final CorpseData corpse = PlayerCorpseNPC.createCorpse(bukkit, deathLocation);

        // Save inventory before clearing
        corpse.setSavedInventory(bukkit.getInventory().getContents().clone());

        // Clear inventory while downed
        bukkit.getInventory().clear();

        // Register the corpse in the plugin tracker
        ZombiesPlugin.INSTANCE.addCorpse(bukkit.getUniqueId(), corpse);

        // Set player to full health but locked in place
        bukkit.setHealth(20.0);

        // Hide the real player from other players, only show the corpse NPC
        PlayerCorpseNPC.hideRealPlayer(bukkit);
    }
}
