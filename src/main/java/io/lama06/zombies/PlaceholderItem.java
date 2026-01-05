package io.lama06.zombies;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PlaceholderItem {
    private static final NamespacedKey IS_PLACEHOLDER_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "is_placeholder");

    public static ItemStack createWeaponPlaceholder(final int slotNumber) {
        final ItemStack item = new ItemStack(Material.WHITE_DYE);
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Gun #" + slotNumber).color(NamedTextColor.GOLD));
        meta.getPersistentDataContainer().set(IS_PLACEHOLDER_KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createPerkPlaceholder(final int slotNumber) {
        final ItemStack item = new ItemStack(Material.GRAY_DYE);
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Perk #" + slotNumber).color(NamedTextColor.GRAY));
        meta.getPersistentDataContainer().set(IS_PLACEHOLDER_KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isPlaceholder(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(IS_PLACEHOLDER_KEY, PersistentDataType.BOOLEAN, false);
    }

    private PlaceholderItem() { }
}
