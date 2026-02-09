package io.lama06.zombies.system.zombie;

import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OverrideMeleeDamageSystem implements Listener {
    private static final int SLIME_ATTACK_INTERVAL_TICKS = 10;
    private final Map<AttackKey, Long> slimeNextAttackTick = new HashMap<>();

    @EventHandler
    private void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }
        if (ZombiesPlugin.INSTANCE.getCorpse(player.getUniqueId()) != null) {
            event.setCancelled(true);
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(event.getEntity().getWorld());
        if (!world.isGameRunning()) {
            return;
        }
        final Zombie zombie = new Zombie(event.getDamager());
        if (!zombie.isZombie()) {
            return;
        }

        if (event.getDamager() instanceof Slime) {
            final long nowTick = player.getWorld().getGameTime();
            final AttackKey attackKey = new AttackKey(event.getDamager().getUniqueId(), player.getUniqueId());
            final Long nextAllowedTick = slimeNextAttackTick.get(attackKey);
            if (nextAllowedTick != null && nowTick < nextAllowedTick) {
                event.setCancelled(true);
                return;
            }
            slimeNextAttackTick.put(attackKey, nowTick + SLIME_ATTACK_INTERVAL_TICKS);
        }

        final ZombieData data = zombie.getData();
        if (data == null || data.meleeDamage == null) {
            return;
        }
        event.setDamage(data.meleeDamage);
    }

    private record AttackKey(UUID attacker, UUID victim) { }
}
