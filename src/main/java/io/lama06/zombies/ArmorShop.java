package io.lama06.zombies;

import io.lama06.zombies.menu.*;
import io.lama06.zombies.util.PositionUtil;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;
import java.util.Set;

public final class ArmorShop implements CheckableConfig {
    public BlockPosition position;
    public int price = 100;
    public Part part = Part.UPPER_BODY;
    public Quality quality = Quality.LEATHER;

    @Override
    public void check() throws InvalidConfigException {
        InvalidConfigException.mustBeSet(position, "position");
        InvalidConfigException.mustBeSet(part, "part");
        InvalidConfigException.mustBeSet(quality, "quality");
    }

    public void openMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openMenu(player, callback);
        SelectionMenu.open(
                player,
                Component.text("Armor Shop"),
                callback,
                new SelectionEntry(
                        Component.text("Position: " + PositionUtil.format(position)),
                        Material.LEVER,
                        () -> BlockPositionSelection.open(
                                player,
                                Component.text("Armor Shop Position"),
                                reopen,
                                position -> {
                                    this.position = position;
                                    reopen.run();
                                }
                        )
                ),
                new SelectionEntry(
                        Component.text("Price: " + price),
                        Material.GOLD_NUGGET,
                        () -> InputMenu.open(
                                player,
                                Component.text("Amor Shop Price"),
                                price,
                                new IntegerInputType(),
                                price -> {
                                    this.price = price;
                                    reopen.run();
                                },
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Part: ").append(part.getDisplayName()),
                        part.getDisplayMaterial(),
                        () -> EnumSelectionMenu.open(
                                Part.class,
                                player,
                                Component.text("Armor Part"),
                                reopen,
                                part -> {
                                    this.part = part;
                                    reopen.run();
                                }
                        )
                ),
                new SelectionEntry(
                        Component.text("Quality: ").append(quality.getDisplayName()),
                        quality.getDisplayMaterial(),
                        () -> EnumSelectionMenu.open(
                                Quality.class,
                                player,
                                Component.text("Armor Quality"),
                                reopen,
                                quality -> {
                                    this.quality = quality;
                                    reopen.run();
                                }
                        )
                )
        );
    }

    public enum Part implements MenuDisplayableEnum {
        UPPER_BODY,
        LOWER_BODY;

        @Override
        public Component getDisplayName() {
            return Component.text(switch (this) {
                case LOWER_BODY -> "Lower Body";
                case UPPER_BODY -> "Upper Body";
            });
        }

        @Override
        public Material getDisplayMaterial() {
            return switch (this) {
                case LOWER_BODY -> Material.CHAINMAIL_LEGGINGS;
                case UPPER_BODY -> Material.CHAINMAIL_CHESTPLATE;
            };
        }

        public Set<EquipmentSlot> getEquipmentSlots() {
            return switch (this) {
                case UPPER_BODY -> Set.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST);
                case LOWER_BODY -> Set.of(EquipmentSlot.LEGS, EquipmentSlot.FEET);
            };
        }
    }

    public enum Quality implements MenuDisplayableEnum {
        LEATHER(0, Map.of(
                EquipmentSlot.HEAD, Material.LEATHER_HELMET,
                EquipmentSlot.CHEST, Material.LEATHER_CHESTPLATE,
                EquipmentSlot.LEGS, Material.LEATHER_LEGGINGS,
                EquipmentSlot.FEET, Material.LEATHER_BOOTS
        )),
        GOLD(1, Map.of(
                EquipmentSlot.HEAD, Material.GOLDEN_HELMET,
                EquipmentSlot.CHEST, Material.GOLDEN_CHESTPLATE,
                EquipmentSlot.LEGS, Material.GOLDEN_LEGGINGS,
                EquipmentSlot.FEET, Material.GOLDEN_BOOTS
        )),
        IRON(2, Map.of(
                EquipmentSlot.HEAD, Material.IRON_HELMET,
                EquipmentSlot.CHEST, Material.IRON_CHESTPLATE,
                EquipmentSlot.LEGS, Material.IRON_LEGGINGS,
                EquipmentSlot.FEET, Material.IRON_BOOTS
        )),
        DIAMOND(3, Map.of(
                EquipmentSlot.HEAD, Material.DIAMOND_HELMET,
                EquipmentSlot.CHEST, Material.DIAMOND_CHESTPLATE,
                EquipmentSlot.LEGS, Material.DIAMOND_LEGGINGS,
                EquipmentSlot.FEET, Material.DIAMOND_BOOTS
        )),
        NETHERITE(4, Map.of(
                EquipmentSlot.HEAD, Material.NETHERITE_HELMET,
                EquipmentSlot.CHEST, Material.NETHERITE_CHESTPLATE,
                EquipmentSlot.LEGS, Material.NETHERITE_LEGGINGS,
                EquipmentSlot.FEET, Material.NETHERITE_BOOTS
        ));

        public final int tier;
        public final Map<EquipmentSlot, Material> materials;

        Quality(final int tier, final Map<EquipmentSlot, Material> materials) {
            this.tier = tier;
            this.materials = materials;
        }

        /**
         * Gets the tier level of a material. Returns -1 for non-armor or AIR.
         */
        public static int getTierOfMaterial(final Material material) {
            if (material == null || material == Material.AIR) {
                return -1;
            }
            for (final Quality quality : values()) {
                if (quality.materials.containsValue(material)) {
                    return quality.tier;
                }
            }
            // Check chainmail separately (tier between leather and gold)
            if (material == Material.CHAINMAIL_HELMET || material == Material.CHAINMAIL_CHESTPLATE ||
                material == Material.CHAINMAIL_LEGGINGS || material == Material.CHAINMAIL_BOOTS) {
                return 0; // Same tier as leather
            }
            return -1;
        }

        @Override
        public Component getDisplayName() {
            return switch (this) {
                case LEATHER -> Component.text("Leather");
                case GOLD -> Component.text("Gold").color(NamedTextColor.GOLD);
                case IRON -> Component.text("Iron");
                case DIAMOND -> Component.text("Diamond").color(NamedTextColor.AQUA);
                case NETHERITE -> Component.text("Netherite").color(NamedTextColor.DARK_RED);
            };
        }

        @Override
        public Material getDisplayMaterial() {
            return switch (this) {
                case LEATHER -> Material.LEATHER;
                case GOLD -> Material.GOLD_INGOT;
                case IRON -> Material.IRON_INGOT;
                case DIAMOND -> Material.DIAMOND;
                case NETHERITE -> Material.NETHERITE_INGOT;
            };
        }
    }
}
