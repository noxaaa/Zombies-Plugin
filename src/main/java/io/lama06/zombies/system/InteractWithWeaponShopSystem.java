package io.lama06.zombies.system;

import io.lama06.zombies.PlaceholderItem;
import io.lama06.zombies.WeaponShop;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.player.PlayerGoldChangeEvent;
import io.lama06.zombies.event.weapon.WeaponAmmoChangeEvent;
import io.lama06.zombies.event.weapon.WeaponClipChangeEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.weapon.*;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class InteractWithWeaponShopSystem implements Listener {
    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        final Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        final ZombiesPlayer player = new ZombiesPlayer(event.getPlayer());
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }
        final WeaponShop weaponShop = world.getConfig().weaponShops.stream()
                .filter(shop -> shop.position.equals(clickedBlock.getLocation().toBlock()))
                .findAny().orElse(null);
        if (weaponShop == null) {
            return;
        }
        final Weapon heldWeapon = player.getHeldWeapon();
        // Use isSameFamily so PISTOL_UPGRADED can be refilled at PISTOL shop
        if (heldWeapon != null && heldWeapon.get(Weapon.TYPE).isSameFamily(weaponShop.weaponType)) {
            refillWeapon(player, weaponShop, heldWeapon);
        } else {
            buyWeapon(player, weaponShop);
        }
    }

    private void buyWeapon(final ZombiesPlayer player, final WeaponShop shop) {
        // Check if player already has a weapon of the same type
        if (player.hasWeaponOfSameType(shop.weaponType)) {
            player.sendMessage(Component.text("You already have this weapon").color(NamedTextColor.RED));
            return;
        }

        final int gold = player.get(ZombiesPlayer.GOLD);
        if (gold < shop.purchasePrice) {
            player.sendMessage(Component.text("You cannot afford this weapon").color(NamedTextColor.RED));
            return;
        }

        final PlayerInventory inventory = player.getBukkit().getInventory();

        // Find first empty slot (placeholder)
        Integer emptySlot = null;
        for (int slot = 1; slot <= player.getLastWeaponSlot(); slot++) {
            final ItemStack item = inventory.getItem(slot);
            if (PlaceholderItem.isPlaceholder(item)) {
                emptySlot = slot;
                break;
            }
        }

        final int targetSlot;
        if (emptySlot != null) {
            // Has empty slot, buy directly to empty slot
            targetSlot = emptySlot;
        } else {
            // No empty slot, must be holding a weapon to replace
            final int heldSlot = inventory.getHeldItemSlot();
            if (heldSlot == 0 || heldSlot > player.getLastWeaponSlot()) {
                player.sendMessage(Component.text("No empty slot. Hold a weapon to replace.").color(NamedTextColor.RED));
                return;
            }
            final Weapon heldWeapon = player.getHeldWeapon();
            if (heldWeapon == null) {
                player.sendMessage(Component.text("Hold a weapon to replace.").color(NamedTextColor.RED));
                return;
            }
            targetSlot = heldSlot;
        }

        player.giveWeapon(targetSlot, shop.weaponType);
        player.set(ZombiesPlayer.GOLD, gold - shop.purchasePrice);
        Bukkit.getPluginManager().callEvent(new PlayerGoldChangeEvent(player, gold, gold - shop.purchasePrice));
        player.sendMessage(Component.text("Successfully bought the weapon").color(NamedTextColor.GREEN));
    }

    private void refillWeapon(final ZombiesPlayer player, final WeaponShop shop, final Weapon weapon) {
        final int gold = player.get(ZombiesPlayer.GOLD);
        if (gold < shop.refillPrice) {
            player.sendMessage(Component.text("You cannot afford refilling this weapon").color(NamedTextColor.RED));
            return;
        }
        final io.lama06.zombies.data.Component ammComponent = weapon.getComponent(Weapon.AMMO);
        if (ammComponent == null) {
            player.sendMessage(Component.text("This weapon cannot be refilled").color(NamedTextColor.RED));
            return;
        }
        final AmmoData ammoData = weapon.getData().ammo;
        final int clip = ammComponent.get(AmmoData.CLIP);
        final int ammo = ammComponent.get(AmmoData.AMMO);
        final int maxAmmo = ammoData.ammo();
        final int maxClip = ammoData.clip();
        if (ammo == maxAmmo) {
            player.sendMessage(Component.text("This weapon is already refilled").color(NamedTextColor.RED));
            return;
        }
        ammComponent.set(AmmoData.CLIP, maxClip);
        ammComponent.set(AmmoData.AMMO, maxAmmo);
        Bukkit.getPluginManager().callEvent(new WeaponAmmoChangeEvent(weapon, ammo, maxAmmo));
        Bukkit.getPluginManager().callEvent(new WeaponClipChangeEvent(weapon, clip, maxClip));

        player.set(ZombiesPlayer.GOLD, gold - shop.refillPrice);
        Bukkit.getPluginManager().callEvent(new PlayerGoldChangeEvent(player, gold, gold - shop.refillPrice));

        player.sendMessage(Component.text("Successfully refilled the weapon").color(NamedTextColor.GREEN));
    }
}
