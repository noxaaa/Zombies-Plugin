package io.lama06.zombies;

import io.lama06.zombies.menu.BlockPositionSelection;
import io.lama06.zombies.menu.SelectionEntry;
import io.lama06.zombies.menu.SelectionMenu;
import io.lama06.zombies.util.PositionUtil;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;

public final class UpgradeableArmorShop implements CheckableConfig {
    public BlockPosition position;

    @Override
    public void check() throws InvalidConfigException {
        InvalidConfigException.mustBeSet(position, "position");
    }

    public void openMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openMenu(player, callback);
        SelectionMenu.open(
                player,
                Component.text("Upgradeable Armor Shop"),
                callback,
                new SelectionEntry(
                        Component.text("Position: " + PositionUtil.format(position)),
                        Material.LEVER,
                        () -> BlockPositionSelection.open(
                                player,
                                Component.text("Upgradeable Armor Shop Position"),
                                reopen,
                                position -> {
                                    this.position = position;
                                    reopen.run();
                                }
                        )
                )
        );
    }

    public static int getMaxPurchases() {
        return Upgrade.values().length;
    }

    public static Upgrade getUpgradeByPurchaseCount(final int purchaseCount) {
        if (purchaseCount < 0 || purchaseCount >= Upgrade.values().length) {
            return null;
        }
        return Upgrade.values()[purchaseCount];
    }

    public static Map<EquipmentSlot, Material> getArmorPreviewForPurchaseCount(final int purchaseCount) {
        final Upgrade upgrade = getUpgradeByPurchaseCount(purchaseCount);
        if (upgrade != null) {
            return upgrade.armor;
        }
        return Upgrade.values()[Upgrade.values().length - 1].armor;
    }

    public enum Upgrade {
        LEATHER_SET(
                Component.text("Leather Set"),
                500,
                Map.of(
                        EquipmentSlot.HEAD, Material.LEATHER_HELMET,
                        EquipmentSlot.CHEST, Material.LEATHER_CHESTPLATE,
                        EquipmentSlot.LEGS, Material.LEATHER_LEGGINGS,
                        EquipmentSlot.FEET, Material.LEATHER_BOOTS
                )
        ),
        GOLD_SET(
                Component.text("Gold Set").color(NamedTextColor.GOLD),
                1000,
                Map.of(
                        EquipmentSlot.HEAD, Material.GOLDEN_HELMET,
                        EquipmentSlot.CHEST, Material.GOLDEN_CHESTPLATE,
                        EquipmentSlot.LEGS, Material.GOLDEN_LEGGINGS,
                        EquipmentSlot.FEET, Material.GOLDEN_BOOTS
                )
        ),
        IRON_SET(
                Component.text("Iron Set"),
                2000,
                Map.of(
                        EquipmentSlot.HEAD, Material.IRON_HELMET,
                        EquipmentSlot.CHEST, Material.IRON_CHESTPLATE,
                        EquipmentSlot.LEGS, Material.IRON_LEGGINGS,
                        EquipmentSlot.FEET, Material.IRON_BOOTS
                )
        ),
        DIAMOND_BOOTS(
                Component.text("Diamond Boots").color(NamedTextColor.AQUA),
                5000,
                Map.of(
                        EquipmentSlot.HEAD, Material.IRON_HELMET,
                        EquipmentSlot.CHEST, Material.IRON_CHESTPLATE,
                        EquipmentSlot.LEGS, Material.IRON_LEGGINGS,
                        EquipmentSlot.FEET, Material.DIAMOND_BOOTS
                )
        ),
        DIAMOND_HELMET(
                Component.text("Diamond Helmet").color(NamedTextColor.AQUA),
                10000,
                Map.of(
                        EquipmentSlot.HEAD, Material.DIAMOND_HELMET,
                        EquipmentSlot.CHEST, Material.IRON_CHESTPLATE,
                        EquipmentSlot.LEGS, Material.IRON_LEGGINGS,
                        EquipmentSlot.FEET, Material.DIAMOND_BOOTS
                )
        ),
        DIAMOND_LEGGINGS(
                Component.text("Diamond Leggings").color(NamedTextColor.AQUA),
                20000,
                Map.of(
                        EquipmentSlot.HEAD, Material.DIAMOND_HELMET,
                        EquipmentSlot.CHEST, Material.IRON_CHESTPLATE,
                        EquipmentSlot.LEGS, Material.DIAMOND_LEGGINGS,
                        EquipmentSlot.FEET, Material.DIAMOND_BOOTS
                )
        ),
        DIAMOND_CHESTPLATE(
                Component.text("Diamond Chestplate").color(NamedTextColor.AQUA),
                100000,
                Map.of(
                        EquipmentSlot.HEAD, Material.DIAMOND_HELMET,
                        EquipmentSlot.CHEST, Material.DIAMOND_CHESTPLATE,
                        EquipmentSlot.LEGS, Material.DIAMOND_LEGGINGS,
                        EquipmentSlot.FEET, Material.DIAMOND_BOOTS
                )
        );

        public final Component displayName;
        public final int price;
        public final Map<EquipmentSlot, Material> armor;

        Upgrade(final Component displayName, final int price, final Map<EquipmentSlot, Material> armor) {
            this.displayName = displayName;
            this.price = price;
            this.armor = armor;
        }
    }
}
