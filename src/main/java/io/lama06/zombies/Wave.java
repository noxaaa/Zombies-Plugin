package io.lama06.zombies;

import io.lama06.zombies.menu.*;
import io.lama06.zombies.zombie.ZombieType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Wave {
    public int delayTicks;
    public Map<ZombieType, Integer> zombies = new HashMap<>();
    public Map<ZombieType, Integer> zombieHealth = new HashMap<>();  // 每种僵尸的血量覆盖, 0或不存在 = 使用僵尸类型默认血量
    public Map<ZombieType, Double> zombieSpeed = new HashMap<>();    // 每种僵尸的速度覆盖, 0或不存在 = 使用默认速度
    public Map<ZombieType, Double> zombieDamage = new HashMap<>();   // 每种僵尸的攻击力覆盖, 0或不存在 = 使用默认攻击力

    public int getTotalZombies() {
        return zombies.values().stream().mapToInt(i -> i).sum();
    }

    public int getHealthForType(final ZombieType type) {
        return zombieHealth.getOrDefault(type, 0);
    }

    public double getSpeedForType(final ZombieType type) {
        return zombieSpeed.getOrDefault(type, 0.0);
    }

    public double getDamageForType(final ZombieType type) {
        return zombieDamage.getOrDefault(type, 0.0);
    }

    public void openMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openMenu(player, callback);

        SelectionMenu.open(
                player,
                Component.text("Edit Wave"),
                callback,
                new SelectionEntry(
                        Component.text("Delay: " + delayTicks + " ticks (" + (delayTicks / 20.0) + "s)"),
                        Material.CLOCK,
                        () -> InputMenu.open(
                                player,
                                Component.text("Delay (ticks)"),
                                delayTicks,
                                new IntegerInputType(),
                                delay -> {
                                    this.delayTicks = delay;
                                    reopen.run();
                                },
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Zombies (" + getTotalZombies() + " total)"),
                        Material.ZOMBIE_HEAD,
                        () -> openZombiesMenu(player, reopen)
                ),
                new SelectionEntry(
                        Component.text("Zombie Health Overrides"),
                        Material.RED_DYE,
                        () -> openHealthMenu(player, reopen)
                ),
                new SelectionEntry(
                        Component.text("Zombie Speed Overrides"),
                        Material.FEATHER,
                        () -> openSpeedMenu(player, reopen)
                ),
                new SelectionEntry(
                        Component.text("Zombie Damage Overrides"),
                        Material.IRON_SWORD,
                        () -> openDamageMenu(player, reopen)
                )
        );
    }

    private void openZombiesMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openZombiesMenu(player, callback);

        final List<SelectionEntry> entries = new ArrayList<>();
        entries.add(new SelectionEntry(
                Component.text("Add Zombie Type").color(NamedTextColor.GREEN),
                Material.GREEN_STAINED_GLASS_PANE,
                () -> EnumSelectionMenu.open(
                        ZombieType.class,
                        player,
                        Component.text("Select Zombie Type"),
                        reopen,
                        type -> {
                            zombies.put(type, zombies.getOrDefault(type, 0) + 1);
                            reopen.run();
                        }
                )
        ));

        for (final var entry : zombies.entrySet()) {
            final ZombieType type = entry.getKey();
            final int count = entry.getValue();
            entries.add(new SelectionEntry(
                    Component.text(type.name() + ": " + count),
                    type.getDisplayMaterial(),
                    () -> InputMenu.open(
                            player,
                            Component.text(type.name() + " Count"),
                            count,
                            new IntegerInputType(),
                            newCount -> {
                                if (newCount <= 0) {
                                    zombies.remove(type);
                                } else {
                                    zombies.put(type, newCount);
                                }
                                reopen.run();
                            },
                            reopen
                    ),
                    Component.text("Delete").color(NamedTextColor.RED),
                    () -> {
                        zombies.remove(type);
                        reopen.run();
                    }
            ));
        }

        SelectionMenu.open(player, Component.text("Edit Zombies"), callback, entries.toArray(SelectionEntry[]::new));
    }

    private void openHealthMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openHealthMenu(player, callback);

        final List<SelectionEntry> entries = new ArrayList<>();
        entries.add(new SelectionEntry(
                Component.text("Add Health Override").color(NamedTextColor.GREEN),
                Material.GREEN_STAINED_GLASS_PANE,
                () -> EnumSelectionMenu.open(
                        ZombieType.class,
                        player,
                        Component.text("Select Zombie Type"),
                        reopen,
                        type -> InputMenu.open(
                                player,
                                Component.text(type.name() + " Health"),
                                type.data.health,
                                new IntegerInputType(1, null),
                                health -> {
                                    zombieHealth.put(type, health);
                                    reopen.run();
                                },
                                reopen
                        )
                )
        ));

        for (final var entry : zombieHealth.entrySet()) {
            final ZombieType type = entry.getKey();
            final int health = entry.getValue();
            entries.add(new SelectionEntry(
                    Component.text(type.name() + ": " + health + " HP"),
                    Material.RED_DYE,
                    () -> InputMenu.open(
                            player,
                            Component.text(type.name() + " Health"),
                            health,
                            new IntegerInputType(1, null),
                            newHealth -> {
                                zombieHealth.put(type, newHealth);
                                reopen.run();
                            },
                            reopen
                    ),
                    Component.text("Delete").color(NamedTextColor.RED),
                    () -> {
                        zombieHealth.remove(type);
                        reopen.run();
                    }
            ));
        }

        SelectionMenu.open(player, Component.text("Health Overrides"), callback, entries.toArray(SelectionEntry[]::new));
    }

    private void openSpeedMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openSpeedMenu(player, callback);

        final List<SelectionEntry> entries = new ArrayList<>();
        entries.add(new SelectionEntry(
                Component.text("Add Speed Override").color(NamedTextColor.GREEN),
                Material.GREEN_STAINED_GLASS_PANE,
                () -> EnumSelectionMenu.open(
                        ZombieType.class,
                        player,
                        Component.text("Select Zombie Type"),
                        reopen,
                        type -> InputMenu.open(
                                player,
                                Component.text(type.name() + " Speed"),
                                0.23,
                                new DoubleInputType(0.0, 10.0),
                                speed -> {
                                    zombieSpeed.put(type, speed);
                                    reopen.run();
                                },
                                reopen
                        )
                )
        ));

        for (final var entry : zombieSpeed.entrySet()) {
            final ZombieType type = entry.getKey();
            final double speed = entry.getValue();
            entries.add(new SelectionEntry(
                    Component.text(type.name() + ": " + speed),
                    Material.FEATHER,
                    () -> InputMenu.open(
                            player,
                            Component.text(type.name() + " Speed"),
                            speed,
                            new DoubleInputType(0.0, 10.0),
                            newSpeed -> {
                                zombieSpeed.put(type, newSpeed);
                                reopen.run();
                            },
                            reopen
                    ),
                    Component.text("Delete").color(NamedTextColor.RED),
                    () -> {
                        zombieSpeed.remove(type);
                        reopen.run();
                    }
            ));
        }

        SelectionMenu.open(player, Component.text("Speed Overrides"), callback, entries.toArray(SelectionEntry[]::new));
    }

    private void openDamageMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openDamageMenu(player, callback);

        final List<SelectionEntry> entries = new ArrayList<>();
        entries.add(new SelectionEntry(
                Component.text("Add Damage Override").color(NamedTextColor.GREEN),
                Material.GREEN_STAINED_GLASS_PANE,
                () -> EnumSelectionMenu.open(
                        ZombieType.class,
                        player,
                        Component.text("Select Zombie Type"),
                        reopen,
                        type -> InputMenu.open(
                                player,
                                Component.text(type.name() + " Damage"),
                                2.0,
                                new DoubleInputType(0.0, 100.0),
                                damage -> {
                                    zombieDamage.put(type, damage);
                                    reopen.run();
                                },
                                reopen
                        )
                )
        ));

        for (final var entry : zombieDamage.entrySet()) {
            final ZombieType type = entry.getKey();
            final double damage = entry.getValue();
            entries.add(new SelectionEntry(
                    Component.text(type.name() + ": " + damage + " dmg"),
                    Material.IRON_SWORD,
                    () -> InputMenu.open(
                            player,
                            Component.text(type.name() + " Damage"),
                            damage,
                            new DoubleInputType(0.0, 100.0),
                            newDamage -> {
                                zombieDamage.put(type, newDamage);
                                reopen.run();
                            },
                            reopen
                    ),
                    Component.text("Delete").color(NamedTextColor.RED),
                    () -> {
                        zombieDamage.remove(type);
                        reopen.run();
                    }
            ));
        }

        SelectionMenu.open(player, Component.text("Damage Overrides"), callback, entries.toArray(SelectionEntry[]::new));
    }
}
