package io.lama06.zombies.system.zombie;

import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class HalveSkeletonRangedDamageSystem implements Listener {
    @EventHandler(ignoreCancelled = true)
    private void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final ZombiesWorld world = new ZombiesWorld(event.getEntity().getWorld());
        if (!world.isGameRunning()) {
            return;
        }

        if (!(event.getDamager() instanceof final Projectile projectile)) {
            return;
        }

        final ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof final Entity shooterEntity)) {
            return;
        }

        final Zombie zombie = new Zombie(shooterEntity);
        if (!zombie.isZombie() || zombie.getType() != ZombieType.SKELETON) {
            return;
        }

        event.setDamage(event.getDamage() * 0.5);
    }
}
