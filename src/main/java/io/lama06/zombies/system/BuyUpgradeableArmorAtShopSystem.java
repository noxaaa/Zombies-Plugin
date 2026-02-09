package io.lama06.zombies.system;

import io.lama06.zombies.UpgradeableArmorShop;
import io.lama06.zombies.UpgradeableArmorShopProgress;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.player.PlayerGoldChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public final class BuyUpgradeableArmorAtShopSystem implements Listener {
    @EventHandler
    private void onPlayerInteractAtEntity(final PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof final ArmorStand armorStand)) {
            return;
        }

        final PersistentDataContainer pdc = armorStand.getPersistentDataContainer();
        if (!pdc.has(UpgradeableArmorShopDisplaySystem.getUpgradeableArmorShopKey(), PersistentDataType.BOOLEAN)) {
            return;
        }

        final String ownerId = pdc.get(UpgradeableArmorShopDisplaySystem.getUpgradeableArmorShopOwnerKey(), PersistentDataType.STRING);
        if (ownerId == null || !ownerId.equals(event.getPlayer().getUniqueId().toString())) {
            return;
        }

        final Integer shopIndex = pdc.get(UpgradeableArmorShopDisplaySystem.getUpgradeableArmorShopIndexKey(), PersistentDataType.INTEGER);
        if (shopIndex == null) {
            return;
        }

        final ZombiesPlayer player = new ZombiesPlayer(event.getPlayer());
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }

        final WorldConfig config = world.getConfig();
        if (config == null || shopIndex < 0 || shopIndex >= config.upgradeableArmorShops.size()) {
            return;
        }

        final int purchaseCount = Math.max(0, Math.min(
                UpgradeableArmorShopProgress.getPurchaseCount(player, shopIndex),
                UpgradeableArmorShop.getMaxPurchases()
        ));
        final UpgradeableArmorShop.Upgrade nextUpgrade = UpgradeableArmorShop.getUpgradeByPurchaseCount(purchaseCount);
        if (nextUpgrade == null) {
            player.sendMessage(Component.text("Armor is already fully upgraded").color(NamedTextColor.RED));
            return;
        }

        final int oldGold = player.get(ZombiesPlayer.GOLD);
        if (oldGold < nextUpgrade.price) {
            player.sendMessage(Component.text("You cannot afford this").color(NamedTextColor.RED));
            return;
        }

        equipArmor(player, nextUpgrade.armor);

        final int newGold = oldGold - nextUpgrade.price;
        player.set(ZombiesPlayer.GOLD, newGold);
        Bukkit.getPluginManager().callEvent(new PlayerGoldChangeEvent(player, oldGold, newGold));

        UpgradeableArmorShopProgress.setPurchaseCount(player, shopIndex, purchaseCount + 1);
        player.sendMessage(Component.text("Successfully upgraded armor").color(NamedTextColor.GREEN));
    }

    private void equipArmor(final ZombiesPlayer player, final Map<EquipmentSlot, org.bukkit.Material> armor) {
        final var inventory = player.getBukkit().getInventory();
        for (final Map.Entry<EquipmentSlot, org.bukkit.Material> entry : armor.entrySet()) {
            inventory.setItem(entry.getKey(), new ItemStack(entry.getValue()));
        }
    }
}
