package io.lama06.zombies.system.zombie;

import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class OverrideMeleeDamageSystem implements Listener {
    @EventHandler
    private void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(event.getEntity().getWorld());
        if (!world.isGameRunning()) {
            return;
        }
        final Zombie zombie = new Zombie(event.getDamager());
        if (!zombie.isZombie()) {
            return;
        }
        final ZombieData data = zombie.getData();
        if (data == null || data.meleeDamage == null) {
            return;
        }
        event.setDamage(data.meleeDamage);
    }
}
