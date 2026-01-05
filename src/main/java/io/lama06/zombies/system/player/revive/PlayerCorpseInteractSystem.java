package io.lama06.zombies.system.player.revive;

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import io.lama06.zombies.ZombiesWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class PlayerCorpseInteractSystem implements Listener {
    @EventHandler
    private void onUseUnknownEntity(final PlayerUseUnknownEntityEvent event) {
        final int entityId = event.getEntityId();
        final CorpseData corpse = PlayerCorpseNPC.getCorpseByEntityId(entityId);
        if (corpse == null) {
            return;
        }

        final Player reviver = event.getPlayer();

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
