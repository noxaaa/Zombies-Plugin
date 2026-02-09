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
import java.util.Objects;

public final class Wave {
    public int delayTicks;
    public Map<ZombieType, Integer> zombies = new HashMap<>();
    public Map<ZombieType, Integer> zombieHealth = new HashMap<>();  // 每种僵尸的血量覆盖, 0或不存在 = 使用僵尸类型默认血量
    public Map<ZombieType, Double> zombieSpeed = new HashMap<>();    // 每种僵尸的速度覆盖, 0或不存在 = 使用默认速度
    public Map<ZombieType, Double> zombieDamage = new HashMap<>();   // 每种僵尸的攻击力覆盖, 0或不存在 = 使用默认攻击力
    public Map<ZombieType, Double> zombieDefense = new HashMap<>(); // 每种僵尸的防御力覆盖, 0或不存在 = 使用默认防御力

    public int getTotalZombies() {
        return zombies.values().stream()
                .mapToInt(count -> Objects.requireNonNullElse(count, 0))
                .sum();
    }

    public int getHealthForType(final ZombieType type) {
        return Objects.requireNonNullElse(zombieHealth.get(type), 0);
    }

    public double getSpeedForType(final ZombieType type) {
        return Objects.requireNonNullElse(zombieSpeed.get(type), 0.0);
    }

    public double getDamageForType(final ZombieType type) {
        return Objects.requireNonNullElse(zombieDamage.get(type), 0.0);
    }

    public double getDefenseForType(final ZombieType type) {
        return Objects.requireNonNullElse(zombieDefense.get(type), 0.0);
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
                ),
                new SelectionEntry(
                        Component.text("Zombie Defense Overrides"),
                        Material.SHIELD,
                        () -> openDefenseMenu(player, reopen)
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
            final int count = Objects.requireNonNullElse(entry.getValue(), 0);
            final String typeName = getTypeName(type);
            entries.add(new SelectionEntry(
                    formatTypeValue(type, count),
                    getTypeMaterial(type),
                    () -> InputMenu.open(
                            player,
                            Component.text(typeName + " Count"),
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
            final int health = Objects.requireNonNullElse(entry.getValue(), 0);
            final String typeName = getTypeName(type);
            entries.add(new SelectionEntry(
                    formatTypeValue(type, health + " HP"),
                    getTypeMaterial(type),
                    () -> InputMenu.open(
                            player,
                            Component.text(typeName + " Health"),
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
            final double speed = Objects.requireNonNullElse(entry.getValue(), 0.0);
            final String typeName = getTypeName(type);
            entries.add(new SelectionEntry(
                    formatTypeValue(type, speed),
                    getTypeMaterial(type),
                    () -> InputMenu.open(
                            player,
                            Component.text(typeName + " Speed"),
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
            final double damage = Objects.requireNonNullElse(entry.getValue(), 0.0);
            final String typeName = getTypeName(type);
            entries.add(new SelectionEntry(
                    formatTypeValue(type, damage + " dmg"),
                    getTypeMaterial(type),
                    () -> InputMenu.open(
                            player,
                            Component.text(typeName + " Damage"),
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

    private void openDefenseMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openDefenseMenu(player, callback);

        final List<SelectionEntry> entries = new ArrayList<>();
        entries.add(new SelectionEntry(
                Component.text("Add Defense Override").color(NamedTextColor.GREEN),
                Material.GREEN_STAINED_GLASS_PANE,
                () -> EnumSelectionMenu.open(
                        ZombieType.class,
                        player,
                        Component.text("Select Zombie Type"),
                        reopen,
                        type -> InputMenu.open(
                                player,
                                Component.text(type.name() + " Defense"),
                                type.data.defense,
                                new DoubleInputType(0.0, 20.0),
                                defense -> {
                                    zombieDefense.put(type, defense);
                                    reopen.run();
                                },
                                reopen
                        )
                )
        ));

        for (final var entry : zombieDefense.entrySet()) {
            final ZombieType type = entry.getKey();
            final double defense = Objects.requireNonNullElse(entry.getValue(), 0.0);
            final String typeName = getTypeName(type);
            entries.add(new SelectionEntry(
                    formatTypeValue(type, defense + " def"),
                    getTypeMaterial(type),
                    () -> InputMenu.open(
                            player,
                            Component.text(typeName + " Defense"),
                            defense,
                            new DoubleInputType(0.0, 20.0),
                            newDefense -> {
                                zombieDefense.put(type, newDefense);
                                reopen.run();
                            },
                            reopen
                    ),
                    Component.text("Delete").color(NamedTextColor.RED),
                    () -> {
                        zombieDefense.remove(type);
                        reopen.run();
                    }
            ));
        }

        SelectionMenu.open(player, Component.text("Defense Overrides"), callback, entries.toArray(SelectionEntry[]::new));
    }

    private static String getTypeName(final ZombieType type) {
        return type == null ? "INVALID_ZOMBIE_TYPE" : type.name();
    }

    private static Material getTypeMaterial(final ZombieType type) {
        return type == null ? Material.BARRIER : type.getDisplayMaterial();
    }

    private static Component formatTypeValue(final ZombieType type, final Object value) {
        final Component text = Component.text(getTypeName(type) + ": " + value);
        return type == null ? text.color(NamedTextColor.RED) : text;
    }
}
