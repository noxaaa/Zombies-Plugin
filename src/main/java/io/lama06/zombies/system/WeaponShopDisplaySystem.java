package io.lama06.zombies.system;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.WeaponShop;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.event.GameStartEvent;
import io.lama06.zombies.weapon.WeaponType;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public final class WeaponShopDisplaySystem implements Listener {
    private static final NamespacedKey WEAPON_SHOP_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "weapon_shop");
    private static final NamespacedKey WEAPON_SHOP_INDEX_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "weapon_shop_index");
    private static final NamespacedKey WEAPON_SHOP_DISPLAY_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "weapon_shop_display");

    public static NamespacedKey getWeaponShopKey() {
        return WEAPON_SHOP_KEY;
    }

    public static NamespacedKey getWeaponShopIndexKey() {
        return WEAPON_SHOP_INDEX_KEY;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onGameStart(final GameStartEvent event) {
        final ZombiesWorld world = event.getWorld();
        final WorldConfig config = world.getConfig();
        if (config == null) {
            return;
        }

        final World bukkit = world.getBukkit();

        for (int i = 0; i < config.weaponShops.size(); i++) {
            final int shopIndex = i;
            final WeaponShop shop = config.weaponShops.get(i);
            final WeaponType weaponType = shop.weaponType;

            // Item display location - center of block, raised
            final Location itemLocation = new Location(
                    bukkit,
                    shop.position.blockX() + 0.5,
                    shop.position.blockY() + 0.9,
                    shop.position.blockZ() + 0.5
            );

            // Spawn ItemDisplay for visual
            bukkit.spawn(itemLocation, ItemDisplay.class, display -> {
                display.setItemStack(new ItemStack(weaponType.getDisplayMaterial()));
                display.setBillboard(Display.Billboard.VERTICAL);
                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 1),
                        new Vector3f(0.6f, 0.6f, 0.6f),
                        new AxisAngle4f(0, 0, 0, 1)
                ));

                display.getPersistentDataContainer().set(WEAPON_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN, true);
            });

            // Spawn invisible armor stand for interaction
            final Location armorStandLocation = new Location(
                    bukkit,
                    shop.position.blockX() + 0.5,
                    shop.position.blockY() + 0.5,
                    shop.position.blockZ() + 0.5
            );

            bukkit.spawn(armorStandLocation, ArmorStand.class, entity -> {
                entity.setVisible(false);
                entity.setGravity(false);
                entity.setInvulnerable(true);
                entity.setCanPickupItems(false);
                entity.setSmall(true);
                entity.setMarker(false); // Must be false to allow interaction

                // Mark as weapon shop
                final PersistentDataContainer pdc = entity.getPersistentDataContainer();
                pdc.set(WEAPON_SHOP_KEY, PersistentDataType.BOOLEAN, true);
                pdc.set(WEAPON_SHOP_INDEX_KEY, PersistentDataType.INTEGER, shopIndex);
            });

            // Spawn text display below the item
            final Location textLocation = new Location(
                    bukkit,
                    shop.position.blockX() + 0.5,
                    shop.position.blockY() + 0.1,
                    shop.position.blockZ() + 0.5
            );

            bukkit.spawn(textLocation, TextDisplay.class, display -> {
                // Default text - will be updated per-player by tick system
                final Component text = weaponType.getDisplayName().color(NamedTextColor.GREEN)
                        .appendNewline()
                        .append(Component.text(shop.purchasePrice + " Gold").color(NamedTextColor.GOLD));

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

                // Mark as weapon shop display with index
                final PersistentDataContainer pdc = display.getPersistentDataContainer();
                pdc.set(WEAPON_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN, true);
                pdc.set(WEAPON_SHOP_INDEX_KEY, PersistentDataType.INTEGER, shopIndex);
            });
        }
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
                if (!pdc.has(WEAPON_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN)) {
                    continue;
                }

                final Integer shopIndex = pdc.get(WEAPON_SHOP_INDEX_KEY, PersistentDataType.INTEGER);
                if (shopIndex == null || shopIndex < 0 || shopIndex >= config.weaponShops.size()) {
                    continue;
                }

                final WeaponShop shop = config.weaponShops.get(shopIndex);
                final WeaponType weaponType = shop.weaponType;

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
                    final boolean hasWeapon = zombiesPlayer.hasWeaponOfSameType(weaponType);

                    if (hasWeapon) {
                        // Player has this weapon - show "Weapon Ammo" and refill price
                        text = weaponType.getDisplayName().color(NamedTextColor.GREEN)
                                .append(Component.text(" Ammo").color(NamedTextColor.GREEN))
                                .appendNewline()
                                .append(Component.text(shop.refillPrice + " Gold").color(NamedTextColor.GOLD));
                    } else {
                        // Player doesn't have this weapon - show weapon name and purchase price
                        text = weaponType.getDisplayName().color(NamedTextColor.GREEN)
                                .appendNewline()
                                .append(Component.text(shop.purchasePrice + " Gold").color(NamedTextColor.GOLD));
                    }
                } else {
                    // No player nearby - show default (purchase price)
                    text = weaponType.getDisplayName().color(NamedTextColor.GREEN)
                            .appendNewline()
                            .append(Component.text(shop.purchasePrice + " Gold").color(NamedTextColor.GOLD));
                }

                display.text(text);
            }
        }
    }

    @EventHandler
    private void onGameEnd(final GameEndEvent event) {
        final World bukkit = event.getWorld().getBukkit();

        // Remove all weapon shop armor stands
        bukkit.getEntitiesByClass(ArmorStand.class).stream()
                .filter(stand -> stand.getPersistentDataContainer().has(WEAPON_SHOP_KEY, PersistentDataType.BOOLEAN))
                .forEach(ArmorStand::remove);

        // Remove all weapon shop item displays
        bukkit.getEntitiesByClass(ItemDisplay.class).stream()
                .filter(display -> display.getPersistentDataContainer().has(WEAPON_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN))
                .forEach(ItemDisplay::remove);

        // Remove all weapon shop text displays
        bukkit.getEntitiesByClass(TextDisplay.class).stream()
                .filter(display -> display.getPersistentDataContainer().has(WEAPON_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN))
                .forEach(TextDisplay::remove);
    }
}
