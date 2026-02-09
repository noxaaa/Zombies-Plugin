package io.lama06.zombies.system.zombie;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class GrowSlimeBlobSystem implements Listener {
    private static final int GROWTH_INTERVAL_TICKS = 3 * 20;
    private static final int MAX_SIZE = 10;

    @EventHandler
    private void onSpawn(final ZombieSpawnEvent event) {
        final Zombie zombie = event.getZombie();
        if (zombie.getType() != ZombieType.SLIME_BLOB) {
            return;
        }
        if (!(zombie.getEntity() instanceof final Slime slime)) {
            return;
        }

        final double baseHealth = event.getEffectiveHealth();
        if (baseHealth <= 0) {
            return;
        }

        zombie.set(Zombie.SLIME_BLOB_BASE_HEALTH, baseHealth);
        setSizeAndHealth(slime, 1, baseHealth, baseHealth);
    }

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final Zombie zombie : ZombiesPlugin.INSTANCE.getZombies()) {
            if (zombie.getType() != ZombieType.SLIME_BLOB) {
                continue;
            }
            if (!(zombie.getEntity() instanceof final Slime slime)) {
                continue;
            }
            if (slime.getSize() >= MAX_SIZE) {
                continue;
            }
            if (slime.getTicksLived() <= 0 || slime.getTicksLived() % GROWTH_INTERVAL_TICKS != 0) {
                continue;
            }

            final Double baseGrowthHealth = zombie.get(Zombie.SLIME_BLOB_BASE_HEALTH);
            if (baseGrowthHealth == null || baseGrowthHealth <= 0) {
                continue;
            }

            final AttributeInstance maxHealth = slime.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth == null) {
                continue;
            }

            final double newMaxHealth = maxHealth.getBaseValue() + baseGrowthHealth;
            final double newHealth = Math.min(newMaxHealth, slime.getHealth() + baseGrowthHealth);
            final int nextSize = Math.min(MAX_SIZE, slime.getSize() + 1);
            setSizeAndHealth(slime, nextSize, newMaxHealth, newHealth);
        }
    }

    private static void setSizeAndHealth(final Slime slime, final int size, final double maxHealth, final double health) {
        slime.setSize(size);

        final AttributeInstance maxHealthAttribute = slime.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            return;
        }
        maxHealthAttribute.setBaseValue(maxHealth);
        slime.setHealth(Math.min(maxHealth, health));
    }
}
