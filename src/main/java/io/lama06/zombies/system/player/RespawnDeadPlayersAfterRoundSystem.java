package io.lama06.zombies.system.player;

import io.lama06.zombies.PlaceholderItem;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.StartRoundEvent;
import io.lama06.zombies.ZombiesPlayer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class RespawnDeadPlayersAfterRoundSystem implements Listener {
    @EventHandler
    private void onRoundStart(final StartRoundEvent event) {
        final ZombiesWorld world = event.getWorld();
        for (final ZombiesPlayer player : world.getPlayers()) {
            final Player bukkit = player.getBukkit();
            if (bukkit.getGameMode() != GameMode.SPECTATOR) {
                continue;
            }
            // Don't clear perks here - they were already cleared when the player timed out
            // Players keep their weapons but lose perks on true death (25s timeout)
            bukkit.setGameMode(GameMode.ADVENTURE);
            bukkit.teleport(world.getBukkit().getSpawnLocation());
            bukkit.setHealth(20);
            bukkit.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));

            // Restore empty weapon slot placeholders (slots 1-2, not slot 3 since EXTRA_WEAPON perk is lost)
            for (int slot = 1; slot <= 2; slot++) {
                final ItemStack item = bukkit.getInventory().getItem(slot);
                if (item == null || item.getType() == Material.AIR) {
                    bukkit.getInventory().setItem(slot, PlaceholderItem.createWeaponPlaceholder(slot));
                }
            }
        }
    }
}
