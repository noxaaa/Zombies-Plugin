package io.lama06.zombies.system.player.revive;

import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public final class PlayerCorpseInteractSystem implements Listener {
    @EventHandler
    private void onRightClick(final PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand armorStand)) {
            return;
        }

        final CorpseData corpse = ZombiesPlugin.INSTANCE.getCorpseByArmorStand(armorStand);
        if (corpse == null) {
            return;
        }

        handleReviveInteraction(event.getPlayer(), corpse);
        event.setCancelled(true);
    }

    @EventHandler
    private void onLeftClick(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof ArmorStand armorStand)) {
            return;
        }

        final CorpseData corpse = ZombiesPlugin.INSTANCE.getCorpseByArmorStand(armorStand);
        if (corpse == null) {
            return;
        }

        handleReviveInteraction(player, corpse);
        event.setCancelled(true);
    }

    private void handleReviveInteraction(final Player reviver, final CorpseData corpse) {
        // Check if game is running
        final ZombiesWorld world = new ZombiesWorld(reviver.getWorld());
        if (!world.isZombiesWorld() || !world.isGameRunning()) {
            return;
        }

        // Can't revive yourself
        if (reviver.getUniqueId().equals(corpse.getDeadPlayerUUID())) {
            return;
        }

        // Only alive players can revive
        if (reviver.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        // Check if someone else is already reviving (via click)
        if (corpse.hasReviver() && corpse.isReviveByClick() &&
                !corpse.getReviverUUID().equals(reviver.getUniqueId())) {
            reviver.sendActionBar(Component.text("Someone is already reviving this player").color(NamedTextColor.RED));
            return;
        }

        // Start or continue click revive (takes priority over sneak revive)
        corpse.setReviverUUID(reviver.getUniqueId());
        corpse.setReviveByClick(true);
    }
}
