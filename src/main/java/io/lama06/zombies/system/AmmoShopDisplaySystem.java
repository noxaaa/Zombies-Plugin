package io.lama06.zombies.system;

import io.lama06.zombies.AmmoShop;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.event.GameStartEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
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

public final class AmmoShopDisplaySystem implements Listener {
    private static final NamespacedKey AMMO_SHOP_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "ammo_shop");
    private static final NamespacedKey AMMO_SHOP_INDEX_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "ammo_shop_index");
    private static final NamespacedKey AMMO_SHOP_DISPLAY_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "ammo_shop_display");

    public static NamespacedKey getAmmoShopKey() {
        return AMMO_SHOP_KEY;
    }

    public static NamespacedKey getAmmoShopIndexKey() {
        return AMMO_SHOP_INDEX_KEY;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onGameStart(final GameStartEvent event) {
        final ZombiesWorld world = event.getWorld();
        final WorldConfig config = world.getConfig();
        if (config == null) {
            return;
        }

        final World bukkit = world.getBukkit();

        for (int i = 0; i < config.ammoShops.size(); i++) {
            final int shopIndex = i;
            final AmmoShop shop = config.ammoShops.get(i);

            // Item display location - center of block, raised
            final Location itemLocation = new Location(
                    bukkit,
                    shop.position.blockX() + 0.5,
                    shop.position.blockY() + 0.9,
                    shop.position.blockZ() + 0.5
            );

            // Spawn ItemDisplay for visual (GUNPOWDER)
            bukkit.spawn(itemLocation, ItemDisplay.class, display -> {
                display.setItemStack(new ItemStack(Material.GUNPOWDER));
                display.setBillboard(Display.Billboard.VERTICAL);
                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 1),
                        new Vector3f(0.6f, 0.6f, 0.6f),
                        new AxisAngle4f(0, 0, 0, 1)
                ));

                display.getPersistentDataContainer().set(AMMO_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN, true);
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

                // Mark as ammo shop
                final PersistentDataContainer pdc = entity.getPersistentDataContainer();
                pdc.set(AMMO_SHOP_KEY, PersistentDataType.BOOLEAN, true);
                pdc.set(AMMO_SHOP_INDEX_KEY, PersistentDataType.INTEGER, shopIndex);
            });

            // Spawn text display below the item
            final Location textLocation = new Location(
                    bukkit,
                    shop.position.blockX() + 0.5,
                    shop.position.blockY() + 0.1,
                    shop.position.blockZ() + 0.5
            );

            bukkit.spawn(textLocation, TextDisplay.class, display -> {
                final Component text = Component.text("Ammo").color(NamedTextColor.GREEN)
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

                display.getPersistentDataContainer().set(AMMO_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN, true);
            });
        }
    }

    @EventHandler
    private void onGameEnd(final GameEndEvent event) {
        final World bukkit = event.getWorld().getBukkit();

        // Remove all ammo shop armor stands
        bukkit.getEntitiesByClass(ArmorStand.class).stream()
                .filter(stand -> stand.getPersistentDataContainer().has(AMMO_SHOP_KEY, PersistentDataType.BOOLEAN))
                .forEach(ArmorStand::remove);

        // Remove all ammo shop item displays
        bukkit.getEntitiesByClass(ItemDisplay.class).stream()
                .filter(display -> display.getPersistentDataContainer().has(AMMO_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN))
                .forEach(ItemDisplay::remove);

        // Remove all ammo shop text displays
        bukkit.getEntitiesByClass(TextDisplay.class).stream()
                .filter(display -> display.getPersistentDataContainer().has(AMMO_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN))
                .forEach(TextDisplay::remove);
    }
}
