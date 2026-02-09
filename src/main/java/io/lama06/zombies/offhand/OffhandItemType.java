package io.lama06.zombies.offhand;

import io.lama06.zombies.TotemFragmentItem;
import io.lama06.zombies.menu.MenuDisplayableEnum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum OffhandItemType implements MenuDisplayableEnum {
    TOTEM_FRAGMENT(
            Component.text("Totem Fragment").color(NamedTextColor.GOLD),
            Material.TOTEM_OF_UNDYING,
            true
    );

    private final Component displayName;
    private final Material displayMaterial;
    private final boolean inLuckyChest;

    OffhandItemType(final Component displayName, final Material displayMaterial, final boolean inLuckyChest) {
        this.displayName = displayName;
        this.displayMaterial = displayMaterial;
        this.inLuckyChest = inLuckyChest;
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public boolean isInLuckyChest() {
        return inLuckyChest;
    }

    public ItemStack createItem() {
        return switch (this) {
            case TOTEM_FRAGMENT -> TotemFragmentItem.create();
        };
    }

    public boolean matches(final ItemStack item) {
        return switch (this) {
            case TOTEM_FRAGMENT -> TotemFragmentItem.isTotemFragment(item);
        };
    }
}
