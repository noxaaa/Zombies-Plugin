package io.lama06.zombies.system.weapon.shoot;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.event.weapon.WeaponShootEvent;
import io.lama06.zombies.weapon.ShootSoundData;
import io.lama06.zombies.weapon.Weapon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PlaySoundAfterShotSystem implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onWeaponShoot(final WeaponShootEvent event) {
        final ZombiesPlayer player = event.getPlayer();
        final Weapon weapon = event.getWeapon();
        final ShootSoundData shootSoundData = weapon.getData().shootSound;
        if (shootSoundData == null) {
            return;
        }
        player.getBukkit().getWorld().playSound(
                player.getBukkit().getLocation(),
                shootSoundData.sound(),
                shootSoundData.volume(),
                shootSoundData.pitch()
        );
    }
}
