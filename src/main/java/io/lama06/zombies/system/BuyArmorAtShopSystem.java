package io.lama06.zombies.system;

import io.lama06.zombies.ArmorShop;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.player.PlayerGoldChangeEvent;
import io.lama06.zombies.ZombiesPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class BuyArmorAtShopSystem implements Listener {
    @EventHandler
    private void onPlayerInteractAtEntity(final PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof final ArmorStand armorStand)) {
            return;
        }

        final PersistentDataContainer pdc = armorStand.getPersistentDataContainer();
        if (!pdc.has(ArmorShopDisplaySystem.getArmorShopKey(), PersistentDataType.BOOLEAN)) {
            return;
        }

        final Integer shopIndex = pdc.get(ArmorShopDisplaySystem.getArmorShopIndexKey(), PersistentDataType.INTEGER);
        if (shopIndex == null) {
            return;
        }

        final ZombiesPlayer player = new ZombiesPlayer(event.getPlayer());
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }

        final var armorShops = world.getConfig().armorShops;
        if (shopIndex < 0 || shopIndex >= armorShops.size()) {
            return;
        }

        final ArmorShop armorShop = armorShops.get(shopIndex);

        // Find which slots can be upgraded (player has lower tier armor)
        final PlayerInventory inventory = player.getBukkit().getInventory();
        final List<EquipmentSlot> slotsToUpgrade = new ArrayList<>();

        for (final EquipmentSlot slot : armorShop.part.getEquipmentSlots()) {
            final int shopTier = armorShop.quality.tier;
            final int playerTier = ArmorShop.Quality.getTierOfMaterial(inventory.getItem(slot).getType());
            if (playerTier < shopTier) {
                slotsToUpgrade.add(slot);
            }
        }

        // If no slots can be upgraded, player already has all pieces at same or higher tier
        if (slotsToUpgrade.isEmpty()) {
            player.sendMessage(Component.text("You already have equal or better armor").color(NamedTextColor.RED));
            return;
        }

        // Check if player can afford
        final int gold = player.get(ZombiesPlayer.GOLD);
        if (gold < armorShop.price) {
            player.sendMessage(Component.text("You cannot afford this").color(NamedTextColor.RED));
            return;
        }

        // Deduct gold and equip armor for upgradable slots only
        final int newGold = gold - armorShop.price;
        player.set(ZombiesPlayer.GOLD, newGold);
        Bukkit.getPluginManager().callEvent(new PlayerGoldChangeEvent(player, gold, newGold));

        for (final EquipmentSlot slot : slotsToUpgrade) {
            final ItemStack item = new ItemStack(armorShop.quality.materials.get(slot));
            inventory.setItem(slot, item);
        }

        player.sendMessage(Component.text("Successfully bought the armor").color(NamedTextColor.GREEN));
    }
}
