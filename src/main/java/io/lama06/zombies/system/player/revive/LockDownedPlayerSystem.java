package io.lama06.zombies.system.player.revive;

import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class LockDownedPlayerSystem implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isGameRunning()) {
            return;
        }

        final CorpseData corpse = ZombiesPlugin.INSTANCE.getCorpse(player.getUniqueId());
        if (corpse == null || corpse.getRemainingTime() <= 0) {
            return;
        }

        // Lock position only, allow looking around
        final Location from = event.getFrom();
        final Location to = event.getTo();
        final Location corpseLocation = corpse.getLocation();

        // Keep the player's current view direction, but lock position
        final Location lockedLocation = new Location(
                corpseLocation.getWorld(),
                corpseLocation.getX(),
                corpseLocation.getY(),
                corpseLocation.getZ(),
                to.getYaw(),   // Allow view rotation
                to.getPitch()  // Allow view rotation
        );
        event.setTo(lockedLocation);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isGameRunning()) {
            return;
        }

        final CorpseData corpse = ZombiesPlugin.INSTANCE.getCorpse(player.getUniqueId());
        if (corpse == null || corpse.getRemainingTime() <= 0) {
            return;
        }

        // Prevent downed players from using items
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerAttack(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isGameRunning()) {
            return;
        }

        final CorpseData corpse = ZombiesPlugin.INSTANCE.getCorpse(player.getUniqueId());
        if (corpse == null || corpse.getRemainingTime() <= 0) {
            return;
        }

        // Prevent downed players from attacking
        event.setCancelled(true);
    }
}
