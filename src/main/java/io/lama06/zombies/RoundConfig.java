package io.lama06.zombies;

import io.lama06.zombies.menu.*;
import io.lama06.zombies.perk.GlobalPerk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RoundConfig {
    public static final double DEFAULT_PERK_DROP_PROBABILITY = 0.20;

    public List<Wave> waves = new ArrayList<>();
    public Map<GlobalPerk, Double> perkDropProbabilities = new HashMap<>();
    public boolean clearBuffsOnRoundStart = false;

    public int getTotalZombies() {
        return waves.stream().mapToInt(Wave::getTotalZombies).sum();
    }

    public double getPerkDropProbability(final GlobalPerk perk) {
        return perkDropProbabilities.getOrDefault(perk, DEFAULT_PERK_DROP_PROBABILITY);
    }

    public void openMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openMenu(player, callback);

        SelectionMenu.open(
                player,
                Component.text("Edit Round"),
                callback,
                new SelectionEntry(
                        Component.text("Waves (" + waves.size() + ")"),
                        Material.CLOCK,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Waves"),
                                waves,
                                Material.CLOCK,
                                wave -> Component.text("Wave: " + wave.getTotalZombies() + " zombies, delay " + wave.delayTicks + " ticks"),
                                Wave::new,
                                wave -> wave.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Perk Drop Probabilities"),
                        Material.NETHER_STAR,
                        () -> openPerkProbabilitiesMenu(player, reopen)
                ),
                new SelectionEntry(
                        Component.text("Clear Buffs on Round Start: " + (clearBuffsOnRoundStart ? "Yes" : "No"))
                                .color(clearBuffsOnRoundStart ? NamedTextColor.GREEN : NamedTextColor.RED),
                        Material.BUCKET,
                        () -> {
                            clearBuffsOnRoundStart = !clearBuffsOnRoundStart;
                            reopen.run();
                        }
                )
        );
    }

    private void openPerkProbabilitiesMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openPerkProbabilitiesMenu(player, callback);

        final List<SelectionEntry> entries = new ArrayList<>();

        for (final GlobalPerk perk : GlobalPerk.values()) {
            final double probability = getPerkDropProbability(perk);
            final int percentDisplay = (int) (probability * 100);

            entries.add(new SelectionEntry(
                    perk.getDisplayName().append(Component.text(": " + percentDisplay + "%").color(NamedTextColor.GRAY)),
                    perk.getMaterial(),
                    () -> InputMenu.open(
                            player,
                            Component.text("Drop % for ").append(perk.getDisplayName()),
                            percentDisplay,
                            new IntegerInputType(0, 100),
                            newPercent -> {
                                if (newPercent == 20) {
                                    perkDropProbabilities.remove(perk);
                                } else {
                                    perkDropProbabilities.put(perk, newPercent / 100.0);
                                }
                                reopen.run();
                            },
                            reopen
                    ),
                    Component.text("Reset to 20%").color(NamedTextColor.YELLOW),
                    () -> {
                        perkDropProbabilities.remove(perk);
                        reopen.run();
                    }
            ));
        }

        SelectionMenu.open(
                player,
                Component.text("Perk Drop Probabilities"),
                callback,
                entries.toArray(SelectionEntry[]::new)
        );
    }
}
