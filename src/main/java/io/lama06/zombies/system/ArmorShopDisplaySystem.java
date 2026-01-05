package io.lama06.zombies.system;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ArmorShop;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.event.GameStartEvent;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public final class ArmorShopDisplaySystem implements Listener {
    private static final NamespacedKey ARMOR_SHOP_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "armor_shop");
    private static final NamespacedKey ARMOR_SHOP_INDEX_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "armor_shop_index");
    private static final NamespacedKey ARMOR_SHOP_DISPLAY_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "armor_shop_display");

    public static NamespacedKey getArmorShopKey() {
        return ARMOR_SHOP_KEY;
    }

    public static NamespacedKey getArmorShopIndexKey() {
        return ARMOR_SHOP_INDEX_KEY;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onGameStart(final GameStartEvent event) {
        final ZombiesWorld world = event.getWorld();
        final WorldConfig config = world.getConfig();
        if (config == null) {
            return;
        }

        final World bukkit = world.getBukkit();

        for (int i = 0; i < config.armorShops.size(); i++) {
            final int shopIndex = i;
            final ArmorShop shop = config.armorShops.get(i);

            // Spawn invisible small armor stand wearing the armor for display
            final Location armorDisplayLocation = new Location(
                    bukkit,
                    shop.position.blockX() + 0.5,
                    shop.position.blockY() + 0.3,
                    shop.position.blockZ() + 0.5
            );

            bukkit.spawn(armorDisplayLocation, ArmorStand.class, entity -> {
                entity.setVisible(false);
                entity.setGravity(false);
                entity.setInvulnerable(true);
                entity.setCanPickupItems(false);
                entity.setSmall(true);
                entity.setMarker(false); // Must be false to allow interaction
                entity.setArms(false);
                entity.setBasePlate(false);

                // Equip armor based on shop part and quality
                for (final EquipmentSlot slot : shop.part.getEquipmentSlots()) {
                    final ItemStack armor = new ItemStack(shop.quality.materials.get(slot));
                    entity.getEquipment().setItem(slot, armor);
                }

                // Mark as armor shop
                final PersistentDataContainer pdc = entity.getPersistentDataContainer();
                pdc.set(ARMOR_SHOP_KEY, PersistentDataType.BOOLEAN, true);
                pdc.set(ARMOR_SHOP_INDEX_KEY, PersistentDataType.INTEGER, shopIndex);
            });

            // Spawn text display below the armor stand
            final Location textLocation = new Location(
                    bukkit,
                    shop.position.blockX() + 0.5,
                    shop.position.blockY() - 0.3,
                    shop.position.blockZ() + 0.5
            );

            bukkit.spawn(textLocation, TextDisplay.class, display -> {
                // Default text - will be updated per-player by tick system
                final Component text = getShopDisplayName(shop)
                        .appendNewline()
                        .append(Component.text(shop.price + " Gold").color(NamedTextColor.GOLD));

                display.text(text);
                display.setBillboard(Display.Billboard.CENTER);
                display.setAlignment(TextDisplay.TextAlignment.CENTER);
                display.setShadowed(true);
                display.setBackgroundColor(org.bukkit.Color.fromARGB(100, 0, 0, 0));

                // Scale down a bit
                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 1),
                        new Vector3f(0.8f, 0.8f, 0.8f),
                        new AxisAngle4f(0, 0, 0, 1)
                ));

                // Mark as armor shop display with index
                final PersistentDataContainer pdc = display.getPersistentDataContainer();
                pdc.set(ARMOR_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN, true);
                pdc.set(ARMOR_SHOP_INDEX_KEY, PersistentDataType.INTEGER, shopIndex);
            });
        }
    }

    private Component getShopDisplayName(final ArmorShop shop) {
        final String partName = switch (shop.part) {
            case UPPER_BODY -> "Upper Body";
            case LOWER_BODY -> "Lower Body";
        };
        return shop.quality.getDisplayName()
                .append(Component.text(" " + partName).color(NamedTextColor.GREEN));
    }

    @EventHandler
    private void onTick(final ServerTickEndEvent event) {
        for (final World world : org.bukkit.Bukkit.getWorlds()) {
            final ZombiesWorld zombiesWorld = new ZombiesWorld(world);
            if (!zombiesWorld.isGameRunning()) {
                continue;
            }

            final WorldConfig config = zombiesWorld.getConfig();
            if (config == null) {
                continue;
            }

            for (final TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
                final PersistentDataContainer pdc = display.getPersistentDataContainer();
                if (!pdc.has(ARMOR_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN)) {
                    continue;
                }

                final Integer shopIndex = pdc.get(ARMOR_SHOP_INDEX_KEY, PersistentDataType.INTEGER);
                if (shopIndex == null || shopIndex < 0 || shopIndex >= config.armorShops.size()) {
                    continue;
                }

                final ArmorShop shop = config.armorShops.get(shopIndex);

                // Find nearest player
                Player nearestPlayer = null;
                double nearestDistance = Double.MAX_VALUE;
                for (final Player player : display.getLocation().getNearbyPlayers(10)) {
                    final double dist = player.getLocation().distanceSquared(display.getLocation());
                    if (dist < nearestDistance) {
                        nearestDistance = dist;
                        nearestPlayer = player;
                    }
                }

                // Update text based on nearest player
                final Component text;
                if (nearestPlayer != null) {
                    final ZombiesPlayer zombiesPlayer = new ZombiesPlayer(nearestPlayer);
                    final boolean hasAllArmor = playerHasAllArmorOfShop(zombiesPlayer, shop);

                    if (hasAllArmor) {
                        // Player has all armor pieces at this tier or higher
                        text = getShopDisplayName(shop)
                                .appendNewline()
                                .append(Component.text("UNLOCKED").color(NamedTextColor.GRAY));
                    } else {
                        // Player can still buy/upgrade some pieces
                        text = getShopDisplayName(shop)
                                .appendNewline()
                                .append(Component.text(shop.price + " Gold").color(NamedTextColor.GOLD));
                    }
                } else {
                    // No player nearby - show default
                    text = getShopDisplayName(shop)
                            .appendNewline()
                            .append(Component.text(shop.price + " Gold").color(NamedTextColor.GOLD));
                }

                display.text(text);
            }
        }
    }

    /**
     * Checks if player has all armor pieces from this shop at the same or higher tier.
     */
    private boolean playerHasAllArmorOfShop(final ZombiesPlayer player, final ArmorShop shop) {
        final PlayerInventory inventory = player.getBukkit().getInventory();
        for (final EquipmentSlot slot : shop.part.getEquipmentSlots()) {
            final int shopTier = shop.quality.tier;
            final int playerTier = ArmorShop.Quality.getTierOfMaterial(inventory.getItem(slot).getType());
            if (playerTier < shopTier) {
                return false; // Player can upgrade this slot
            }
        }
        return true;
    }

    @EventHandler
    private void onGameEnd(final GameEndEvent event) {
        final World bukkit = event.getWorld().getBukkit();

        // Remove all armor shop armor stands
        bukkit.getEntitiesByClass(ArmorStand.class).stream()
                .filter(stand -> stand.getPersistentDataContainer().has(ARMOR_SHOP_KEY, PersistentDataType.BOOLEAN))
                .forEach(ArmorStand::remove);

        // Remove all armor shop text displays
        bukkit.getEntitiesByClass(TextDisplay.class).stream()
                .filter(display -> display.getPersistentDataContainer().has(ARMOR_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN))
                .forEach(TextDisplay::remove);
    }
}
