package io.lama06.zombies.system.zombie;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Comparator;

public final class RetargetZombiesSystem implements Listener {
    private static final int RETARGET_INTERVAL = 40; // 2秒
    private int tickCounter = 0;

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        tickCounter++;
        if (tickCounter < RETARGET_INTERVAL) {
            return;
        }
        tickCounter = 0;

        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            for (final Zombie zombie : world.getZombies()) {
                retargetZombie(zombie);
            }
        }
    }

    private void retargetZombie(final Zombie zombie) {
        if (!(zombie.getEntity() instanceof final Mob mob)) {
            return;
        }
        final ZombiesPlayer nearestPlayer = zombie.getWorld().getAlivePlayers().stream()
                .min(Comparator.comparingDouble(p ->
                    p.getBukkit().getLocation().distanceSquared(zombie.getEntity().getLocation())))
                .orElse(null);
        if (nearestPlayer != null) {
            mob.setTarget(nearestPlayer.getBukkit());
        }
    }
}
