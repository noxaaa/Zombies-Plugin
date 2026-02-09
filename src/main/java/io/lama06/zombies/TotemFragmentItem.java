package io.lama06.zombies;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class TotemFragmentItem {
    public static ItemStack create() {
        final ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Totem Fragment").color(NamedTextColor.GOLD));
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isTotemFragment(final ItemStack item) {
        return item != null && item.getType() == Material.TOTEM_OF_UNDYING;
    }

    private TotemFragmentItem() { }
}
