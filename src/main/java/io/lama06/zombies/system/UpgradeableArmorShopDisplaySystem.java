package io.lama06.zombies.system;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.UpgradeableArmorShop;
import io.lama06.zombies.UpgradeableArmorShopProgress;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.event.GameStartEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;

public final class UpgradeableArmorShopDisplaySystem implements Listener {
    private static final NamespacedKey UPGRADEABLE_ARMOR_SHOP_KEY =
            new NamespacedKey(ZombiesPlugin.INSTANCE, "upgradeable_armor_shop");
    private static final NamespacedKey UPGRADEABLE_ARMOR_SHOP_INDEX_KEY =
            new NamespacedKey(ZombiesPlugin.INSTANCE, "upgradeable_armor_shop_index");
    private static final NamespacedKey UPGRADEABLE_ARMOR_SHOP_OWNER_KEY =
            new NamespacedKey(ZombiesPlugin.INSTANCE, "upgradeable_armor_shop_owner");
    private static final NamespacedKey UPGRADEABLE_ARMOR_SHOP_DISPLAY_KEY =
            new NamespacedKey(ZombiesPlugin.INSTANCE, "upgradeable_armor_shop_display");
    private static final NamespacedKey UPGRADEABLE_ARMOR_SHOP_STAGE_CACHE_KEY =
            new NamespacedKey(ZombiesPlugin.INSTANCE, "upgradeable_armor_shop_stage_cache");

    public static NamespacedKey getUpgradeableArmorShopKey() {
        return UPGRADEABLE_ARMOR_SHOP_KEY;
    }

    public static NamespacedKey getUpgradeableArmorShopIndexKey() {
        return UPGRADEABLE_ARMOR_SHOP_INDEX_KEY;
    }

    public static NamespacedKey getUpgradeableArmorShopOwnerKey() {
        return UPGRADEABLE_ARMOR_SHOP_OWNER_KEY;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onGameStart(final GameStartEvent event) {
        final ZombiesWorld world = event.getWorld();
        final World bukkit = world.getBukkit();
        removeAllShopDisplays(bukkit);

        final WorldConfig config = world.getConfig();
        if (config == null || config.upgradeableArmorShops.isEmpty()) {
            return;
        }

        for (final ZombiesPlayer player : world.getPlayers()) {
            spawnDisplaysForPlayer(bukkit, config, player.getBukkit());
        }
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isGameRunning()) {
            return;
        }

        final WorldConfig config = world.getConfig();
        if (config == null || config.upgradeableArmorShops.isEmpty()) {
            return;
        }

        // New players should only see their own shop entities.
        applyVisibilityForViewer(world.getBukkit(), player);
        spawnDisplaysForPlayer(world.getBukkit(), config, player);
    }

    @EventHandler
    private void onTick(final ServerTickEndEvent event) {
        for (final World world : Bukkit.getWorlds()) {
            final ZombiesWorld zombiesWorld = new ZombiesWorld(world);
            if (!zombiesWorld.isGameRunning()) {
                continue;
            }

            final WorldConfig config = zombiesWorld.getConfig();
            if (config == null || config.upgradeableArmorShops.isEmpty()) {
                continue;
            }

            updateArmorDisplays(world, config);
            updateTextDisplays(world, config);
        }
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        removeDisplaysForOwner(event.getPlayer().getWorld(), event.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    private void onGameEnd(final GameEndEvent event) {
        removeAllShopDisplays(event.getWorld().getBukkit());
    }

    private void spawnDisplaysForPlayer(final World world, final WorldConfig config, final Player owner) {
        final String ownerId = owner.getUniqueId().toString();
        removeDisplaysForOwner(world, ownerId);

        final ZombiesPlayer zombiesOwner = new ZombiesPlayer(owner);
        for (int i = 0; i < config.upgradeableArmorShops.size(); i++) {
            final int shopIndex = i;
            final UpgradeableArmorShop shop = config.upgradeableArmorShops.get(i);
            final int purchaseCount = clampPurchaseCount(UpgradeableArmorShopProgress.getPurchaseCount(zombiesOwner, shopIndex));

            final Location armorDisplayLocation = new Location(
                    world,
                    shop.position.blockX() + 0.5,
                    shop.position.blockY() + 0.3,
                    shop.position.blockZ() + 0.5
            );

            final ArmorStand armorStand = world.spawn(armorDisplayLocation, ArmorStand.class, entity -> {
                entity.setVisible(false);
                entity.setGravity(false);
                entity.setInvulnerable(true);
                entity.setCanPickupItems(false);
                entity.setSmall(true);
                entity.setMarker(false);
                entity.setArms(false);
                entity.setBasePlate(false);

                setArmorPreview(entity, purchaseCount);

                final PersistentDataContainer pdc = entity.getPersistentDataContainer();
                pdc.set(UPGRADEABLE_ARMOR_SHOP_KEY, PersistentDataType.BOOLEAN, true);
                pdc.set(UPGRADEABLE_ARMOR_SHOP_INDEX_KEY, PersistentDataType.INTEGER, shopIndex);
                pdc.set(UPGRADEABLE_ARMOR_SHOP_OWNER_KEY, PersistentDataType.STRING, ownerId);
                pdc.set(UPGRADEABLE_ARMOR_SHOP_STAGE_CACHE_KEY, PersistentDataType.INTEGER, purchaseCount);
            });
            applyOwnerVisibility(armorStand, owner.getUniqueId(), world);

            final Location textLocation = new Location(
                    world,
                    shop.position.blockX() + 0.5,
                    shop.position.blockY() - 0.3,
                    shop.position.blockZ() + 0.5
            );

            final TextDisplay textDisplay = world.spawn(textLocation, TextDisplay.class, display -> {
                display.text(getShopDisplayText(purchaseCount));
                display.setBillboard(Display.Billboard.CENTER);
                display.setAlignment(TextDisplay.TextAlignment.CENTER);
                display.setShadowed(true);
                display.setBackgroundColor(org.bukkit.Color.fromARGB(100, 0, 0, 0));
                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 1),
                        new Vector3f(0.8f, 0.8f, 0.8f),
                        new AxisAngle4f(0, 0, 0, 1)
                ));

                final PersistentDataContainer pdc = display.getPersistentDataContainer();
                pdc.set(UPGRADEABLE_ARMOR_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN, true);
                pdc.set(UPGRADEABLE_ARMOR_SHOP_INDEX_KEY, PersistentDataType.INTEGER, shopIndex);
                pdc.set(UPGRADEABLE_ARMOR_SHOP_OWNER_KEY, PersistentDataType.STRING, ownerId);
                pdc.set(UPGRADEABLE_ARMOR_SHOP_STAGE_CACHE_KEY, PersistentDataType.INTEGER, purchaseCount);
            });
            applyOwnerVisibility(textDisplay, owner.getUniqueId(), world);
        }
    }

    private void applyOwnerVisibility(final Entity entity, final UUID owner, final World world) {
        for (final Player player : world.getPlayers()) {
            if (player.getUniqueId().equals(owner)) {
                player.showEntity(ZombiesPlugin.INSTANCE, entity);
            } else {
                player.hideEntity(ZombiesPlugin.INSTANCE, entity);
            }
        }
    }

    private void applyVisibilityForViewer(final World world, final Player viewer) {
        final UUID viewerId = viewer.getUniqueId();

        for (final ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
            final PersistentDataContainer pdc = armorStand.getPersistentDataContainer();
            if (!pdc.has(UPGRADEABLE_ARMOR_SHOP_KEY, PersistentDataType.BOOLEAN)) {
                continue;
            }
            final UUID owner = getOwnerUuid(pdc);
            if (owner == null) {
                continue;
            }
            if (owner.equals(viewerId)) {
                viewer.showEntity(ZombiesPlugin.INSTANCE, armorStand);
            } else {
                viewer.hideEntity(ZombiesPlugin.INSTANCE, armorStand);
            }
        }

        for (final TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
            final PersistentDataContainer pdc = display.getPersistentDataContainer();
            if (!pdc.has(UPGRADEABLE_ARMOR_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN)) {
                continue;
            }
            final UUID owner = getOwnerUuid(pdc);
            if (owner == null) {
                continue;
            }
            if (owner.equals(viewerId)) {
                viewer.showEntity(ZombiesPlugin.INSTANCE, display);
            } else {
                viewer.hideEntity(ZombiesPlugin.INSTANCE, display);
            }
        }
    }

    private void updateArmorDisplays(final World world, final WorldConfig config) {
        for (final ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
            final PersistentDataContainer pdc = armorStand.getPersistentDataContainer();
            if (!pdc.has(UPGRADEABLE_ARMOR_SHOP_KEY, PersistentDataType.BOOLEAN)) {
                continue;
            }

            final Integer shopIndex = pdc.get(UPGRADEABLE_ARMOR_SHOP_INDEX_KEY, PersistentDataType.INTEGER);
            if (shopIndex == null || shopIndex < 0 || shopIndex >= config.upgradeableArmorShops.size()) {
                continue;
            }

            final Player owner = getOwnerPlayer(world, pdc);
            if (owner == null) {
                continue;
            }

            final int purchaseCount = clampPurchaseCount(
                    UpgradeableArmorShopProgress.getPurchaseCount(new ZombiesPlayer(owner), shopIndex)
            );
            final Integer cachedPurchaseCount = pdc.get(UPGRADEABLE_ARMOR_SHOP_STAGE_CACHE_KEY, PersistentDataType.INTEGER);
            if (cachedPurchaseCount != null && cachedPurchaseCount == purchaseCount) {
                continue;
            }

            setArmorPreview(armorStand, purchaseCount);
            pdc.set(UPGRADEABLE_ARMOR_SHOP_STAGE_CACHE_KEY, PersistentDataType.INTEGER, purchaseCount);
        }
    }

    private void updateTextDisplays(final World world, final WorldConfig config) {
        for (final TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
            final PersistentDataContainer pdc = display.getPersistentDataContainer();
            if (!pdc.has(UPGRADEABLE_ARMOR_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN)) {
                continue;
            }

            final Integer shopIndex = pdc.get(UPGRADEABLE_ARMOR_SHOP_INDEX_KEY, PersistentDataType.INTEGER);
            if (shopIndex == null || shopIndex < 0 || shopIndex >= config.upgradeableArmorShops.size()) {
                continue;
            }

            final Player owner = getOwnerPlayer(world, pdc);
            if (owner == null) {
                continue;
            }

            final int purchaseCount = clampPurchaseCount(
                    UpgradeableArmorShopProgress.getPurchaseCount(new ZombiesPlayer(owner), shopIndex)
            );
            final Integer cachedPurchaseCount = pdc.get(UPGRADEABLE_ARMOR_SHOP_STAGE_CACHE_KEY, PersistentDataType.INTEGER);
            if (cachedPurchaseCount != null && cachedPurchaseCount == purchaseCount) {
                continue;
            }

            display.text(getShopDisplayText(purchaseCount));
            pdc.set(UPGRADEABLE_ARMOR_SHOP_STAGE_CACHE_KEY, PersistentDataType.INTEGER, purchaseCount);
        }
    }

    private Component getShopDisplayText(final int purchaseCount) {
        final UpgradeableArmorShop.Upgrade nextUpgrade = UpgradeableArmorShop.getUpgradeByPurchaseCount(purchaseCount);
        if (nextUpgrade == null) {
            return Component.text("Upgradeable Armor").color(NamedTextColor.GREEN)
                    .appendNewline()
                    .append(Component.text("UNLOCKED").color(NamedTextColor.GRAY));
        }
        return Component.text("Upgrade: ").color(NamedTextColor.GREEN)
                .append(nextUpgrade.displayName)
                .appendNewline()
                .append(Component.text(nextUpgrade.price + " Gold").color(NamedTextColor.GOLD));
    }

    private void setArmorPreview(final ArmorStand armorStand, final int purchaseCount) {
        final Map<EquipmentSlot, org.bukkit.Material> armor =
                UpgradeableArmorShop.getArmorPreviewForPurchaseCount(purchaseCount);
        if (armorStand.getEquipment() == null) {
            return;
        }
        for (final Map.Entry<EquipmentSlot, org.bukkit.Material> entry : armor.entrySet()) {
            armorStand.getEquipment().setItem(entry.getKey(), new ItemStack(entry.getValue()));
        }
    }

    private Player getOwnerPlayer(final World world, final PersistentDataContainer pdc) {
        final UUID ownerUuid = getOwnerUuid(pdc);
        if (ownerUuid == null) {
            return null;
        }
        return world.getPlayer(ownerUuid);
    }

    private UUID getOwnerUuid(final PersistentDataContainer pdc) {
        final String raw = pdc.get(UPGRADEABLE_ARMOR_SHOP_OWNER_KEY, PersistentDataType.STRING);
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    private int clampPurchaseCount(final int purchaseCount) {
        return Math.max(0, Math.min(purchaseCount, UpgradeableArmorShop.getMaxPurchases()));
    }

    private void removeDisplaysForOwner(final World world, final String ownerId) {
        world.getEntitiesByClass(ArmorStand.class).stream()
                .filter(stand -> stand.getPersistentDataContainer().has(UPGRADEABLE_ARMOR_SHOP_KEY, PersistentDataType.BOOLEAN))
                .filter(stand -> ownerId.equals(stand.getPersistentDataContainer().get(
                        UPGRADEABLE_ARMOR_SHOP_OWNER_KEY,
                        PersistentDataType.STRING
                )))
                .forEach(ArmorStand::remove);

        world.getEntitiesByClass(TextDisplay.class).stream()
                .filter(display -> display.getPersistentDataContainer().has(UPGRADEABLE_ARMOR_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN))
                .filter(display -> ownerId.equals(display.getPersistentDataContainer().get(
                        UPGRADEABLE_ARMOR_SHOP_OWNER_KEY,
                        PersistentDataType.STRING
                )))
                .forEach(TextDisplay::remove);
    }

    private void removeAllShopDisplays(final World world) {
        world.getEntitiesByClass(ArmorStand.class).stream()
                .filter(stand -> stand.getPersistentDataContainer().has(UPGRADEABLE_ARMOR_SHOP_KEY, PersistentDataType.BOOLEAN))
                .forEach(ArmorStand::remove);

        world.getEntitiesByClass(TextDisplay.class).stream()
                .filter(display -> display.getPersistentDataContainer().has(UPGRADEABLE_ARMOR_SHOP_DISPLAY_KEY, PersistentDataType.BOOLEAN))
                .forEach(TextDisplay::remove);
    }
}
