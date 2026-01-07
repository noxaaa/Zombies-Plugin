package io.lama06.zombies.system.zombie;

import io.lama06.zombies.event.player.PlayerAttackZombieEvent;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class DamageZombieAfterAttackSystem implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onAttack(final PlayerAttackZombieEvent event) {
        final Zombie zombie = event.getZombie();
        final Entity entity = zombie.getEntity();
        if (event.isFire()) {
            entity.setFireTicks(5 * 20);
        }
        if (!(entity instanceof final LivingEntity living)) {
            return;
        }
        if (event.isKill()) {
            living.setKiller(event.getPlayer().getBukkit());
            living.setHealth(0);
            applyKnockback(event, living);
            return;
        }
        living.setNoDamageTicks(0);
        if (event.isHeadshot()) {
            zombie.set(Zombie.IGNORE_DEFENSE, true);
        }
        living.damage(event.getDamage(), event.getPlayer().getBukkit());
        zombie.remove(Zombie.IGNORE_DEFENSE);
        applyKnockback(event, living);
        if (event.isFreeze()) {
            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 7 * 20, 2));
        }
    }

    private void applyKnockback(final PlayerAttackZombieEvent event, final LivingEntity target) {
        final double knockback = event.getKnockback();
        if (knockback <= 0) {
            return;
        }
        final Vector playerPos = event.getPlayer().getBukkit().getLocation().toVector();
        final Vector targetPos = target.getLocation().toVector();
        Vector direction = targetPos.subtract(playerPos);
        direction.setY(0);
        if (direction.lengthSquared() > 0) {
            direction = direction.normalize();
        } else {
            direction = event.getPlayer().getBukkit().getLocation().getDirection();
            direction.setY(0);
            if (direction.lengthSquared() > 0) {
                direction = direction.normalize();
            }
        }
        direction.multiply(knockback);
        direction.setY(knockback * 0.3);  // 给一些向上的力
        target.setVelocity(target.getVelocity().add(direction));
    }
}
