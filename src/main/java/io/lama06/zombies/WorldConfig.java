package io.lama06.zombies;

import io.lama06.zombies.menu.*;
import io.lama06.zombies.perk.PerkMachine;
import io.lama06.zombies.skill.SkillType;
import io.lama06.zombies.util.PositionUtil;
import io.lama06.zombies.weapon.WeaponType;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class WorldConfig implements CheckableConfig {
    public String startArea = "";
    public final List<Area> areas = new ArrayList<>();
    public final List<Door> doors = new ArrayList<>();
    public final List<Window> windows = new ArrayList<>();
    public final List<WeaponShop> weaponShops = new ArrayList<>();
    public final List<ArmorShop> armorShops = new ArrayList<>();
    public final List<AmmoShop> ammoShops = new ArrayList<>();
    public final List<LuckyChest> luckyChests = new ArrayList<>();
    public int luckyChestMoveAfterUses = 0;  // 0 = 不移动
    public final List<LuckyChestItemEntry> luckyChestItems = new ArrayList<>();  // 空 = 使用默认
    public final List<PerkMachine> perkMachines = new ArrayList<>();
    public PowerSwitch powerSwitch;
    public BlockPosition teamMachine;
    public BlockPosition ultimateMachine;
    public int ultimateMachinePrice = 5000;
    public boolean autoStartStop;
    public boolean preventBuilding;
    public double spawnRange = 0; // 0 = unlimited
    public final List<RoundConfig> rounds = new ArrayList<>();

    @Override
    public void check() throws InvalidConfigException {
        InvalidConfigException.mustBeSet(startArea, "start area");
        InvalidConfigException.checkList(areas, true, "areas");
        InvalidConfigException.checkList(doors, true, "doors");
        InvalidConfigException.checkList(windows, false, "windows");
        InvalidConfigException.checkList(weaponShops, true, "weapon shops");
        InvalidConfigException.checkList(armorShops, true, "armor shops");
        InvalidConfigException.checkList(ammoShops, true, "ammo shops");
        InvalidConfigException.checkList(luckyChests, true, "lucky chests");
        InvalidConfigException.checkList(perkMachines, true, "perk machines");
        if (powerSwitch != null) {
            powerSwitch.check();
        }
    }

    public void openMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openMenu(player, callback);

        SelectionMenu.open(
                player,
                Component.text("Zombies Configuration").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
                callback,
                new SelectionEntry(
                        Component.text("Start Area: " + (startArea.isEmpty() ? "_" : startArea)),
                        Material.OAK_FENCE,
                        () -> InputMenu.open(
                                player,
                                Component.text("Start Area"),
                                startArea,
                                new TextInputType(),
                                startArea -> {
                                    this.startArea = startArea;
                                    reopen.run();
                                },
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Areas (" + areas.size() + ")"),
                        Material.MAP,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Areas"),
                                areas,
                                Material.MAP,
                                area -> Component.text("Area: " + (area.name.isEmpty() ? "_" : area.name)),
                                Area::new,
                                area -> area.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Doors"),
                        Material.ACACIA_DOOR,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Doors"),
                                doors,
                                Material.ACACIA_DOOR,
                                door -> Component.text("Door from %s to %s".formatted(door.area1.isEmpty() ? "_" : door.area1, door.area2.isEmpty() ? "_" : door.area2)),
                                Door::new,
                                door -> door.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Windows"),
                        Material.GLASS,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Windows"),
                                windows,
                                Material.ACACIA_DOOR,
                                window -> Component.text("Window in: " + (window.area.isEmpty() ? "_" : window.area)),
                                Window::new,
                                window -> window.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Weapon Shops"),
                        Material.WOODEN_HOE,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Weapon Shops"),
                                weaponShops,
                                Material.WOODEN_HOE,
                                shop -> Component.text("Weapon Shop: ").append(shop.weaponType.getDisplayName()),
                                WeaponShop::new,
                                shop -> shop.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Armor Shops"),
                        Material.IRON_CHESTPLATE,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Armor Shops"),
                                armorShops,
                                Material.IRON_CHESTPLATE,
                                shop -> Component.text("Armor Shop: ").append(shop.quality.getDisplayName())
                                        .appendSpace().append(shop.part.getDisplayName()),
                                ArmorShop::new,
                                shop -> shop.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Ammo Shops"),
                        Material.GUNPOWDER,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Ammo Shops"),
                                ammoShops,
                                Material.GUNPOWDER,
                                shop -> Component.text("Ammo Shop at " + PositionUtil.format(shop.position)),
                                AmmoShop::new,
                                shop -> shop.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Lucky Chests"),
                        Material.CHEST,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Lucky Chests"),
                                luckyChests,
                                Material.CHEST,
                                luckyChest -> Component.text("Lucky Chest at " + PositionUtil.format(luckyChest.position)),
                                LuckyChest::new,
                                luckChest -> luckChest.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Lucky Chest Move After Uses: " + luckyChestMoveAfterUses),
                        Material.ENDER_PEARL,
                        () -> InputMenu.open(
                                player,
                                Component.text("Lucky Chest Move After Uses (0 = never)"),
                                luckyChestMoveAfterUses,
                                new IntegerInputType(),
                                uses -> {
                                    this.luckyChestMoveAfterUses = uses;
                                    reopen.run();
                                },
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Lucky Chest Items (" + luckyChestItems.size() + ")"),
                        Material.NETHER_STAR,
                        () -> openLuckyChestItemsMenu(player, reopen)
                ),
                new SelectionEntry(
                        Component.text("Perk Machines"),
                        Material.COMMAND_BLOCK,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Perk Machines"),
                                perkMachines,
                                Material.COMMAND_BLOCK,
                                machine -> Component.text("Perk Machine: ").append(machine.perk.getDisplayName()),
                                PerkMachine::new,
                                machine -> machine.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Power Switch" + (powerSwitch == null ? ": null" : "")),
                        Material.LEVER,
                        () -> (powerSwitch != null ? powerSwitch : (powerSwitch = new PowerSwitch())).openMenu(player, reopen),
                        Component.text("Remove").color(NamedTextColor.RED),
                        () -> {
                            powerSwitch = null;
                            reopen.run();
                        }
                ),
                new SelectionEntry(
                        Component.text("Team Machine: " + PositionUtil.format(teamMachine)),
                        Material.IRON_BLOCK,
                        () -> BlockPositionSelection.open(
                                player,
                                Component.text("Team Machine Position"),
                                reopen,
                                teamMachine -> {
                                    this.teamMachine = teamMachine;
                                    reopen.run();
                                }
                        ),
                        Component.text("Remove").color(NamedTextColor.RED),
                        () -> {
                            teamMachine = null;
                            reopen.run();
                        }
                ),
                new SelectionEntry(
                        Component.text("Ultimate Machine: " + PositionUtil.format(ultimateMachine)),
                        Material.DIAMOND_BLOCK,
                        () -> BlockPositionSelection.open(
                                player,
                                Component.text("Ultimate Machine Position"),
                                reopen,
                                ultimateMachine -> {
                                    this.ultimateMachine = ultimateMachine;
                                    reopen.run();
                                }
                        ),
                        Component.text("Remove").color(NamedTextColor.RED),
                        () -> {
                            ultimateMachine = null;
                            reopen.run();
                        }
                ),
                new SelectionEntry(
                        Component.text("Ultimate Machine Price: " + ultimateMachinePrice).color(NamedTextColor.GOLD),
                        Material.GOLD_NUGGET,
                        () -> InputMenu.open(
                                player,
                                Component.text("Ultimate Machine Price").color(NamedTextColor.GOLD),
                                ultimateMachinePrice,
                                new IntegerInputType(),
                                price -> {
                                    this.ultimateMachinePrice = price;
                                    reopen.run();
                                },
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Auto Start / Stop: " + autoStartStop),
                        Material.CLOCK,
                        () -> {
                            autoStartStop = !autoStartStop;
                            reopen.run();
                        }
                ),
                new SelectionEntry(
                        Component.text("Prevent Building by Operators: " + preventBuilding),
                        Material.BARRIER,
                        () -> {
                            preventBuilding = !preventBuilding;
                            reopen.run();
                        }
                ),
                new SelectionEntry(
                        Component.text("Rounds (" + rounds.size() + ")"),
                        Material.ZOMBIE_HEAD,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Rounds"),
                                rounds,
                                Material.ZOMBIE_HEAD,
                                round -> Component.text("Round: " + round.waves.size() + " waves, " + round.getTotalZombies() + " zombies"),
                                RoundConfig::new,
                                round -> round.openMenu(player, reopen),
                                reopen
                        )
                )
        );
    }

    private void openLuckyChestItemsMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openLuckyChestItemsMenu(player, callback);

        final java.util.List<SelectionEntry> entries = new java.util.ArrayList<>();

        // 添加武器按钮
        entries.add(new SelectionEntry(
                Component.text("Add Weapon").color(NamedTextColor.GREEN),
                Material.DIAMOND_SWORD,
                () -> EnumSelectionMenu.open(
                        WeaponType.class,
                        player,
                        Component.text("Select Weapon"),
                        reopen,
                        weaponType -> {
                            luckyChestItems.add(new LuckyChestItemEntry(weaponType));
                            reopen.run();
                        }
                )
        ));

        // 添加技能按钮
        entries.add(new SelectionEntry(
                Component.text("Add Skill").color(NamedTextColor.GOLD),
                Material.GOLDEN_APPLE,
                () -> EnumSelectionMenu.open(
                        SkillType.class,
                        player,
                        Component.text("Select Skill"),
                        reopen,
                        skillType -> {
                            luckyChestItems.add(new LuckyChestItemEntry(skillType));
                            reopen.run();
                        }
                )
        ));

        // 显示已选物品
        for (int i = 0; i < luckyChestItems.size(); i++) {
            final int index = i;
            final LuckyChestItemEntry item = luckyChestItems.get(i);
            final Component prefix = item.isWeapon()
                    ? Component.text("[Weapon] ").color(NamedTextColor.AQUA)
                    : Component.text("[Skill] ").color(NamedTextColor.GOLD);
            entries.add(new SelectionEntry(
                    prefix.append(item.getDisplayName()),
                    item.getDisplayMaterial(),
                    () -> {},  // 点击无操作
                    Component.text("Remove").color(NamedTextColor.RED),
                    () -> {
                        luckyChestItems.remove(index);
                        reopen.run();
                    }
            ));
        }

        // 提示信息
        if (luckyChestItems.isEmpty()) {
            entries.add(new SelectionEntry(
                    Component.text("Empty = use default (inLuckyChest)").color(NamedTextColor.GRAY),
                    Material.PAPER,
                    () -> {}
            ));
        }

        SelectionMenu.open(
                player,
                Component.text("Lucky Chest Items").color(NamedTextColor.GOLD),
                callback,
                entries.toArray(SelectionEntry[]::new)
        );
    }
}
