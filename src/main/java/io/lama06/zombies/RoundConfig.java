package io.lama06.zombies;

import io.lama06.zombies.menu.ListConfigMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class RoundConfig {
    public List<Wave> waves = new ArrayList<>();

    public int getTotalZombies() {
        return waves.stream().mapToInt(Wave::getTotalZombies).sum();
    }

    public void openMenu(final Player player, final Runnable callback) {
        ListConfigMenu.open(
                player,
                Component.text("Edit Round - Waves (" + waves.size() + ")"),
                waves,
                Material.CLOCK,
                wave -> Component.text("Wave: " + wave.getTotalZombies() + " zombies, delay " + wave.delayTicks + " ticks"),
                Wave::new,
                wave -> wave.openMenu(player, () -> openMenu(player, callback)),
                callback
        );
    }
}
