package io.lama06.zombies.system.zombie;

import io.lama06.zombies.zombie.Zombie;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * 应用自定义防御力系统，忽略原版护甲防护效果。
 * 使用公式: finalDamage = damage * (1 - min(20, defense) / 25)
 */
public final class ApplyCustomDefenseSystem implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        final Zombie zombie = new Zombie(entity);

        if (!zombie.isZombie()) {
            return;
        }

        final Double defense = zombie.get(Zombie.DEFENSE);
        if (defense == null) {
            return;
        }

        // 获取原始伤害（在护甲减免之前）
        final double originalDamage = event.getDamage();

        // 应用自定义防御力公式: damage * (1 - min(20, defense) / 25)
        final double effectiveDefense = Math.min(20.0, defense);
        final double damageMultiplier = 1.0 - (effectiveDefense / 25.0);
        final double finalDamage = originalDamage * damageMultiplier;

        // 设置最终伤害
        event.setDamage(finalDamage);
    }

    /**
     * 在僵尸生成时禁用原版护甲属性，确保护甲不会提供额外防护。
     */
    @EventHandler
    private void onSpawn(final io.lama06.zombies.event.zombie.ZombieSpawnEvent event) {
        final Entity entity = event.getZombie().getEntity();
        if (!(entity instanceof final LivingEntity living)) {
            return;
        }

        // 将护甲属性设为0，禁用原版护甲防护
        final AttributeInstance armorAttribute = living.getAttribute(Attribute.ARMOR);
        if (armorAttribute != null) {
            armorAttribute.setBaseValue(0);
        }
    }
}
