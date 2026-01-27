package io.lama06.zombies.system.weapon.attack;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.data.Component;
import io.lama06.zombies.event.player.PlayerAttackZombieEvent;
import io.lama06.zombies.perk.GlobalPerk;
import io.lama06.zombies.weapon.AttackData;
import io.lama06.zombies.weapon.SpreadDamageData;
import io.lama06.zombies.weapon.Weapon;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Comparator;
import java.util.List;

public final class ApplySpreadDamageSystem implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerAttackZombie(final PlayerAttackZombieEvent event) {
        final Weapon weapon = event.getWeapon();
        final SpreadDamageData spreadData = weapon.getData().spreadDamage;
        if (spreadData == null) {
            return;
        }

        final AttackData attackData = weapon.getData().attack;
        if (attackData == null) {
            return;
        }

        final Zombie primaryTarget = event.getZombie();
        final Entity targetEntity = primaryTarget.getEntity();
        final ZombiesWorld world = primaryTarget.getWorld();

        // Get nearby zombies within spread radius, excluding primary target
        final List<Zombie> nearbyZombies = world.getZombies().stream()
                .filter(z -> !z.getEntity().getUniqueId().equals(targetEntity.getUniqueId()))
                .filter(z -> z.getEntity().getLocation().distance(targetEntity.getLocation()) <= spreadData.radius())
                .sorted(Comparator.comparingDouble(z ->
                        z.getEntity().getLocation().distanceSquared(targetEntity.getLocation())))
                .limit(spreadData.maxTargets())
                .toList();

        // Check if instant kill is active
        final Component perksComponent = world.getComponent(ZombiesWorld.PERKS_COMPONENT);
        final boolean instantKill = perksComponent != null
                && perksComponent.get(GlobalPerk.INSTANT_KILL.getRemainingTimeAttribute()) > 0;

        // Apply non-crit damage and gold to nearby zombies
        final double spreadDamage = attackData.damage();  // Always non-crit
        final int spreadGold = attackData.gold();  // Always non-crit gold
        final ZombiesPlayer player = event.getPlayer();

        for (final Zombie zombie : nearbyZombies) {
            final Entity entity = zombie.getEntity();
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            // Deal damage or instant kill
            if (instantKill) {
                livingEntity.setKiller(player.getBukkit());
                livingEntity.setHealth(0);
            } else {
                livingEntity.damage(spreadDamage, player.getBukkit());
            }

            // Give gold to player
            final int currentGold = player.get(ZombiesPlayer.GOLD);
            player.set(ZombiesPlayer.GOLD, currentGold + spreadGold);
        }
    }
}
