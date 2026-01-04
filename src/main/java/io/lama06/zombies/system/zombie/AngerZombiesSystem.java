package io.lama06.zombies.system.zombie;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.DyeColor;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Comparator;

public final class AngerZombiesSystem implements Listener {
    @EventHandler
    private void onZombieSpawn(final ZombieSpawnEvent event) {
        final Zombie zombie = event.getZombie();
        final ZombiesPlayer nearestPlayer = getNearestPlayer(zombie);
        if (nearestPlayer == null) {
            return;
        }
        // 为所有 Mob 类型设置目标
        if (zombie.getEntity() instanceof final Mob mob) {
            mob.setTarget(nearestPlayer.getBukkit());
        }
        // 特殊处理：设置愤怒状态
        if (zombie.getEntity() instanceof final PigZombie pigZombie) {
            pigZombie.setAngry(true);
        } else if (zombie.getEntity() instanceof final Wolf wolf) {
            wolf.setAngry(true);
            wolf.setCollarColor(DyeColor.RED);
        }
    }

    private ZombiesPlayer getNearestPlayer(final Zombie zombie) {
        return zombie.getWorld().getAlivePlayers().stream()
                .min(Comparator.comparingDouble(player -> player.getBukkit().getLocation().distanceSquared(zombie.getEntity().getLocation())))
                .orElse(null);
    }
}
