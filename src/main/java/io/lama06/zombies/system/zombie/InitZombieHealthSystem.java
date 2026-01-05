package io.lama06.zombies.system.zombie;

import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.zombie.ZombieData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class InitZombieHealthSystem implements Listener {
    @EventHandler
    private void onSpawn(final ZombieSpawnEvent event) {
        final Entity entity = event.getZombie().getEntity();
        if (!(entity instanceof final LivingEntity living)) {
            return;
        }

        // 设置血量
        final int health = event.getEffectiveHealth();
        if (health > 0) {
            final AttributeInstance maxHealth = living.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(health);
                living.setHealth(health);
            }
        }

        // 设置击退抗性
        final ZombieData data = event.getData();
        if (data.knockbackResistance > 0) {
            final AttributeInstance knockbackResist = living.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
            if (knockbackResist != null) {
                knockbackResist.setBaseValue(data.knockbackResistance);
            }
        }
    }
}
