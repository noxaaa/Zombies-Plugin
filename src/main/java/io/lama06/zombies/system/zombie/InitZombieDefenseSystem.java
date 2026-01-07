package io.lama06.zombies.system.zombie;

import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class InitZombieDefenseSystem implements Listener {
    @EventHandler
    private void onSpawn(final ZombieSpawnEvent event) {
        final Zombie zombie = event.getZombie();
        final double defense = event.getEffectiveDefense();
        zombie.set(Zombie.DEFENSE, defense);
    }
}
