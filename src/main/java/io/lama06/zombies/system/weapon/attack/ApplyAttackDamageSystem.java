package io.lama06.zombies.system.weapon.attack;

import io.lama06.zombies.event.player.PlayerAttackZombieEvent;
import io.lama06.zombies.weapon.AttackData;
import io.lama06.zombies.weapon.Weapon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class ApplyAttackDamageSystem implements Listener {
    @EventHandler
    private void onPlayerAttackZombie(final PlayerAttackZombieEvent event) {
        final Weapon weapon = event.getWeapon();
        final AttackData attackData = weapon.getData().attack;
        if (attackData == null) {
            return;
        }
        double damage = attackData.damage();
        if (event.isHeadshot()) {
            damage += attackData.headshotBonusDamage();
        }
        event.setBaseDamage(damage);
        event.setKnockback(attackData.knockback());
        if (attackData.fire()) {
            event.setFire(true);
        }
    }
}
