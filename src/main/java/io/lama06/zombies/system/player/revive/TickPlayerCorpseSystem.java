package io.lama06.zombies.system.player.revive;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.perk.PlayerPerk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TickPlayerCorpseSystem implements Listener {
    @EventHandler
    private void onTick(final ServerTickEndEvent event) {
        final List<UUID> toRemove = new ArrayList<>();

        for (final CorpseData corpse : ZombiesPlugin.INSTANCE.getCorpses()) {
            final Player deadPlayer = Bukkit.getPlayer(corpse.getDeadPlayerUUID());

            // Check if game is still running and player is still in world
            if (deadPlayer == null) {
                toRemove.add(corpse.getDeadPlayerUUID());
                continue;
            }

            final ZombiesWorld world = new ZombiesWorld(deadPlayer.getWorld());
            if (!world.isZombiesWorld() || !world.isGameRunning()) {
                toRemove.add(corpse.getDeadPlayerUUID());
                continue;
            }

            final Location corpseLocation = corpse.getLocation();

            // Show countdown to downed player
            final float remainingSeconds = corpse.getRemainingTime() / 20f;
            if (corpse.hasReviver()) {
                final float reviveSeconds = corpse.getReviveProgress() / 20f;
                deadPlayer.sendActionBar(Component.text("Being revived: %.1fs".formatted(reviveSeconds)).color(NamedTextColor.GREEN));
            } else {
                deadPlayer.sendActionBar(Component.text("Bleed out: %.1fs".formatted(remainingSeconds)).color(NamedTextColor.RED));
            }

            // Handle sneak revive interruption
            if (corpse.hasReviver() && !corpse.isReviveByClick()) {
                final Player reviver = Bukkit.getPlayer(corpse.getReviverUUID());
                if (reviver == null || !reviver.isSneaking() ||
                        reviver.getLocation().distance(corpseLocation) > PlayerCorpse.REVIVE_RADIUS) {
                    // Sneak revive interrupted - clear reviver, reset progress AND reset bleed out timer
                    corpse.clearReviver();
                    corpse.setReviveProgress(PlayerCorpse.REVIVE_TIME);
                    corpse.setRemainingTime(PlayerCorpse.TIME); // Reset 25s timer
                }
            }

            // Handle revive progress
            if (corpse.hasReviver()) {
                final Player reviver = Bukkit.getPlayer(corpse.getReviverUUID());
                if (reviver != null) {
                    final ZombiesPlayer zombiesReviver = new ZombiesPlayer(reviver);
                    final int reviveSpeed = zombiesReviver.hasPerk(PlayerPerk.FAST_REVIVE) ? 4 : 1;
                    corpse.decrementReviveProgress(reviveSpeed);

                    // Show progress to reviver
                    final float seconds = corpse.getReviveProgress() / 20f;
                    reviver.sendActionBar(Component.text("Reviving: %.1fs".formatted(seconds)).color(NamedTextColor.GREEN));

                    // Check if revive is complete
                    if (corpse.getReviveProgress() <= 0) {
                        completeRevive(corpse, deadPlayer, world, reviver);
                        toRemove.add(corpse.getDeadPlayerUUID());
                        continue;
                    }
                }
            } else {
                // No reviver - check for nearby sneaking players
                final var nearbyPlayers = corpseLocation.getNearbyPlayers(PlayerCorpse.REVIVE_RADIUS);
                Player foundReviver = null;

                for (final Player nearbyPlayer : nearbyPlayers) {
                    if (nearbyPlayer.getUniqueId().equals(corpse.getDeadPlayerUUID())) {
                        continue; // Can't revive yourself
                    }
                    if (nearbyPlayer.getGameMode() != GameMode.ADVENTURE) {
                        continue; // Only alive players can revive
                    }

                    // Show hint to nearby players
                    final float seconds = corpse.getReviveProgress() / 20f;
                    nearbyPlayer.sendActionBar(Component.text("Sneak to revive: %.1fs".formatted(seconds)).color(NamedTextColor.YELLOW));

                    if (nearbyPlayer.isSneaking() && foundReviver == null) {
                        foundReviver = nearbyPlayer;
                    }
                }

                if (foundReviver != null) {
                    // Start sneak revive
                    corpse.setReviverUUID(foundReviver.getUniqueId());
                    corpse.setReviveByClick(false);
                } else {
                    // No one reviving - countdown remaining time
                    corpse.decrementRemainingTime();
                }
            }

            // Check if corpse expired (25 seconds timeout = true death)
            if (corpse.getRemainingTime() <= 0) {
                // Restore inventory first
                if (corpse.getSavedInventory() != null) {
                    deadPlayer.getInventory().setContents(corpse.getSavedInventory());
                }

                // Clear perks but keep weapons
                final ZombiesPlayer zombiesDeadPlayer = new ZombiesPlayer(deadPlayer);
                zombiesDeadPlayer.clearPerks();

                // Set to spectator mode (true death)
                deadPlayer.setGameMode(GameMode.SPECTATOR);

                world.sendMessage(Component.text("Failed to revive ")
                        .append(deadPlayer.displayName())
                        .color(NamedTextColor.RED));
                toRemove.add(corpse.getDeadPlayerUUID());
            }
        }

        // Remove expired/completed corpses
        for (final UUID uuid : toRemove) {
            final CorpseData corpse = ZombiesPlugin.INSTANCE.getCorpse(uuid);
            if (corpse != null) {
                PlayerCorpseNPC.despawnCorpse(corpse);
                ZombiesPlugin.INSTANCE.removeCorpse(uuid);
            }
        }
    }

    private void completeRevive(final CorpseData corpse, final Player deadPlayer,
                                final ZombiesWorld world, final Player reviver) {
        // Click revive: respawn at reviver's current location
        // Sneak revive: respawn at corpse location
        final Location baseLocation = corpse.isReviveByClick()
                ? reviver.getLocation()
                : corpse.getLocation();

        // Restore the view direction from when player was downed
        final Location respawnLocation = new Location(
                baseLocation.getWorld(),
                baseLocation.getX(),
                baseLocation.getY(),
                baseLocation.getZ(),
                corpse.getLocation().getYaw(),
                corpse.getLocation().getPitch()
        );

        deadPlayer.setGameMode(GameMode.ADVENTURE);
        deadPlayer.teleport(respawnLocation);

        // Restore inventory
        if (corpse.getSavedInventory() != null) {
            deadPlayer.getInventory().setContents(corpse.getSavedInventory());
        }

        // Show the real player again to other players
        PlayerCorpseNPC.showRealPlayer(deadPlayer);

        // Give speed effect to the reviver
        reviver.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6 * 20, 2));

        world.sendMessage(reviver.displayName()
                .append(Component.text(" revived ").color(NamedTextColor.GREEN))
                .append(deadPlayer.displayName()));
    }
}
