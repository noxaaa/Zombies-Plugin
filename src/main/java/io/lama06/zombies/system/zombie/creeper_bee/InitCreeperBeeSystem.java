package io.lama06.zombies.system.zombie.creeper_bee;

import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.util.pdc.UuidDataType;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.UUID;

public final class InitCreeperBeeSystem implements Listener {
    private static final double SCALE = 0.5;
    private static final double BEE_SPEED = 1.2;
    private static final int HEALTH = 20;

    @EventHandler
    private void onZombieSpawn(final ZombieSpawnEvent event) {
        final Zombie zombie = event.getZombie();
        if (zombie.getType() != ZombieType.SUICIDER) {
            return;
        }
        if (!(zombie.getEntity() instanceof final Creeper creeper)) {
            return;
        }
        final PersistentDataContainer creeperPdc = creeper.getPersistentDataContainer();
        if (creeperPdc.has(CreeperBeeKeys.partnerKey(), UuidDataType.INSTANCE)) {
            return;
        }

        final ZombiesWorld world = zombie.getWorld();
        final Bee bee = world.getBukkit().spawn(creeper.getLocation(), Bee.class);

        creeper.setAI(false);
        creeper.setRemoveWhenFarAway(false);
        bee.setAI(true);
        bee.setAI(true);
        bee.setRemoveWhenFarAway(false);

        applyAttributes(creeper);
        applyAttributes(bee);

        bee.addPassenger(creeper);

        final UUID creeperId = creeper.getUniqueId();
        final UUID beeId = bee.getUniqueId();
        creeperPdc.set(CreeperBeeKeys.partnerKey(), UuidDataType.INSTANCE, beeId);
        bee.getPersistentDataContainer().set(CreeperBeeKeys.partnerKey(), UuidDataType.INSTANCE, creeperId);
    }

    private void applyAttributes(final org.bukkit.entity.LivingEntity entity) {
        final AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(HEALTH);
            entity.setHealth(HEALTH);
        }
        final AttributeInstance knockbackResist = entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (knockbackResist != null) {
            knockbackResist.setBaseValue(1.0);
        }
        final Attribute scaleAttribute = getScaleAttribute();
        if (scaleAttribute != null) {
            final AttributeInstance scale = entity.getAttribute(scaleAttribute);
            if (scale != null) {
                scale.setBaseValue(SCALE);
            }
        }
    }

    private Attribute getScaleAttribute() {
        try {
            return Attribute.valueOf("GENERIC_SCALE");
        } catch (final IllegalArgumentException ignored) {
            // Older API name
        }
        try {
            return Attribute.valueOf("SCALE");
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }
}
