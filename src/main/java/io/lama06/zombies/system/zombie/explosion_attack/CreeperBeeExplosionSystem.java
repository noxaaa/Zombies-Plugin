package io.lama06.zombies.system.zombie.explosion_attack;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.system.zombie.creeper_bee.CreeperBeeKeys;
import io.lama06.zombies.util.pdc.UuidDataType;
import io.lama06.zombies.zombie.ExplosionAttackData;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieType;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public final class CreeperBeeExplosionSystem implements Listener {
    private static final double PROXIMITY_RANGE = 3.0;
    private static final double PROXIMITY_RANGE_SQ = PROXIMITY_RANGE * PROXIMITY_RANGE;

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final Zombie zombie : ZombiesPlugin.INSTANCE.getZombies()) {
            if (zombie.getType() != ZombieType.SUICIDER) {
                continue;
            }
            final Entity entity = zombie.getEntity();
            if (!entity.isValid() || isExploded(entity)) {
                continue;
            }
            if (!isPartnerAlive(entity)) {
                entity.remove();
                continue;
            }
            final ZombiesWorld world = zombie.getWorld();
            final boolean nearPlayer = world.getAlivePlayers().stream()
                    .map(ZombiesPlayer::getBukkit)
                    .anyMatch(player -> player.getLocation().distanceSquared(entity.getLocation()) <= PROXIMITY_RANGE_SQ);
            if (nearPlayer) {
                triggerExplosion(zombie);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onDamage(final EntityDamageByEntityEvent event) {
        final Zombie zombie = new Zombie(event.getEntity());
        if (!zombie.isZombie() || zombie.getType() != ZombieType.SUICIDER) {
            return;
        }
        if (isExploded(event.getEntity())) {
            return;
        }
        event.setCancelled(true);
        triggerExplosion(zombie);
    }

    @EventHandler
    private void onEntityDeath(final EntityDeathEvent event) {
        removePartner(event.getEntity());
    }

    private void triggerExplosion(final Zombie zombie) {
        final Entity entity = zombie.getEntity();
        markExploded(entity);
        final ExplosionAttackData explosionAttackData = zombie.getData().explosionAttack;
        if (explosionAttackData != null) {
            ZombieExplosion.explode(zombie, explosionAttackData);
        }
        removePartner(entity);
        entity.remove();
    }

    private boolean isExploded(final Entity entity) {
        final PersistentDataContainer pdc = entity.getPersistentDataContainer();
        return pdc.getOrDefault(CreeperBeeKeys.explodedKey(), PersistentDataType.BOOLEAN, false);
    }

    private void markExploded(final Entity entity) {
        entity.getPersistentDataContainer().set(
                CreeperBeeKeys.explodedKey(),
                PersistentDataType.BOOLEAN,
                true
        );
    }

    private void removePartner(final Entity entity) {
        final PersistentDataContainer pdc = entity.getPersistentDataContainer();
        final UUID partnerId = pdc.get(CreeperBeeKeys.partnerKey(), UuidDataType.INSTANCE);
        if (partnerId == null) {
            return;
        }
        pdc.remove(CreeperBeeKeys.partnerKey());
        final Entity partner = entity.getWorld().getEntity(partnerId);
        if (partner != null) {
            partner.getPersistentDataContainer().remove(CreeperBeeKeys.partnerKey());
            partner.remove();
        }
    }

    private boolean isPartnerAlive(final Entity entity) {
        final PersistentDataContainer pdc = entity.getPersistentDataContainer();
        final UUID partnerId = pdc.get(CreeperBeeKeys.partnerKey(), UuidDataType.INSTANCE);
        if (partnerId == null) {
            return false;
        }
        final Entity partner = entity.getWorld().getEntity(partnerId);
        return partner != null && partner.isValid();
    }
}
