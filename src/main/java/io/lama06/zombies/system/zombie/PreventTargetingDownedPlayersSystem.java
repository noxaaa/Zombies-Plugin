package io.lama06.zombies.system.zombie;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.util.pdc.UuidDataType;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;

public final class PreventTargetingDownedPlayersSystem implements Listener {
    private static final String LASER_GUARDIAN_ZOMBIE_KEY = "guardian_owner_zombie";

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onEntityTarget(final EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof final Player player)) {
            return;
        }
        if (ZombiesPlugin.INSTANCE.getCorpse(player.getUniqueId()) == null) {
            return;
        }

        if (!isZombieOrLaserGuardian(event.getEntity())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            for (final Zombie zombie : world.getZombies()) {
                if (!(zombie.getEntity() instanceof final Mob mob)) {
                    continue;
                }
                final LivingEntity target = mob.getTarget();
                if (!(target instanceof final Player player)) {
                    continue;
                }
                if (ZombiesPlugin.INSTANCE.getCorpse(player.getUniqueId()) == null) {
                    continue;
                }
                mob.setTarget(null);
            }

            for (final Guardian guardian : world.getBukkit().getEntitiesByClass(Guardian.class)) {
                if (!isLaserGuardian(guardian)) {
                    continue;
                }
                final LivingEntity target = guardian.getTarget();
                if (!(target instanceof final Player player)) {
                    continue;
                }
                if (ZombiesPlugin.INSTANCE.getCorpse(player.getUniqueId()) == null) {
                    continue;
                }
                guardian.setTarget(null);
            }
        }
    }

    private boolean isZombieOrLaserGuardian(final Entity entity) {
        return new Zombie(entity).isZombie() || isLaserGuardian(entity);
    }

    private boolean isLaserGuardian(final Entity entity) {
        if (!(entity instanceof final Guardian guardian)) {
            return false;
        }
        final PersistentDataContainer pdc = guardian.getPersistentDataContainer();
        return pdc.has(
                new NamespacedKey(ZombiesPlugin.INSTANCE, LASER_GUARDIAN_ZOMBIE_KEY),
                UuidDataType.INSTANCE
        );
    }
}
