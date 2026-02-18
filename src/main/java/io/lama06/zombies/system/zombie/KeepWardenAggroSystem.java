package io.lama06.zombies.system.zombie;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieType;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Comparator;

public final class KeepWardenAggroSystem implements Listener {
    // Warden becomes ANGRY at >=80, keep it above this to avoid digging/despawn behavior.
    private static final int MIN_ANGER = 100;
    private static final int REFRESH_INTERVAL_TICKS = 20;

    @EventHandler
    private void onSpawn(final ZombieSpawnEvent event) {
        keepAggro(event.getZombie());
    }

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        if (event.getTickNumber() % REFRESH_INTERVAL_TICKS != 0) {
            return;
        }
        for (final Zombie zombie : ZombiesPlugin.INSTANCE.getZombies()) {
            keepAggro(zombie);
        }
    }

    private static void keepAggro(final Zombie zombie) {
        if (zombie.getType() != ZombieType.WARDEN) {
            return;
        }
        if (!(zombie.getEntity() instanceof final Warden warden)) {
            return;
        }

        final ZombiesPlayer target = zombie.getWorld().getAlivePlayers().stream()
                .min(Comparator.comparingDouble(player ->
                        player.getBukkit().getLocation().distanceSquared(warden.getLocation())))
                .orElse(null);
        if (target == null) {
            return;
        }

        if (warden.getAnger(target.getBukkit()) < MIN_ANGER) {
            warden.setAnger(target.getBukkit(), MIN_ANGER);
        }
        warden.setTarget(target.getBukkit());
        warden.setDisturbanceLocation(target.getBukkit().getLocation());
    }
}
