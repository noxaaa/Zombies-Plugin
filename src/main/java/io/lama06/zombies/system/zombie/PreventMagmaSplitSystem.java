package io.lama06.zombies.system.zombie;

import io.lama06.zombies.zombie.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SlimeSplitEvent;

public final class PreventMagmaSplitSystem implements Listener {
    @EventHandler
    private void onSlimeSplit(final SlimeSplitEvent event) {
        final Zombie zombie = new Zombie(event.getEntity());
        if (zombie.isZombie()) {
            event.setCancelled(true);
        }
    }
}
