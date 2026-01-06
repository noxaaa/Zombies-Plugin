package io.lama06.zombies.perk;

import io.lama06.zombies.data.AttributeId;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.persistence.PersistentDataType;

public enum GlobalPerk {
    INSTANT_KILL(
            10*20,
            Material.WITHER_SKELETON_SKULL,
            Component.text("Insta Kill").color(NamedTextColor.RED),
            BarColor.RED,
            Sound.ENTITY_ZOMBIE_HORSE_DEATH,
            0.5f
    ),
    MAX_AMMO(
            0,
            Material.ARROW,
            Component.text("Max Ammo").color(NamedTextColor.BLUE),
            BarColor.BLUE,
            Sound.ENTITY_WOLF_SHAKE,
            1.5f
    ),
    DOUBLE_GOLD(
            30*20,
            Material.GOLD_INGOT,
            Component.text("Double Gold").color(NamedTextColor.GOLD),
            BarColor.YELLOW,
            Sound.ITEM_ARMOR_EQUIP_NETHERITE,
            0.5f
    );

    private final int time;
    private final Material material;
    private final Component displayName;
    private final BarColor barColor;
    private final Sound pickupSound;
    private final float pickupPitch;

    GlobalPerk(final int time, final Material material, final Component displayName, final BarColor barColor,
               final Sound pickupSound, final float pickupPitch) {
        this.time = time;
        this.material = material;
        this.displayName = displayName;
        this.barColor = barColor;
        this.pickupSound = pickupSound;
        this.pickupPitch = pickupPitch;
    }

    public AttributeId<Integer> getRemainingTimeAttribute() {
        return new AttributeId<>(name().toLowerCase(), PersistentDataType.INTEGER);
    }

    public int getTime() {
        return time;
    }

    public Material getMaterial() {
        return material;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public Sound getPickupSound() {
        return pickupSound;
    }

    public float getPickupPitch() {
        return pickupPitch;
    }
}
