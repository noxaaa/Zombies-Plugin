package io.lama06.zombies.system;

import io.lama06.zombies.AmmoShop;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.player.PlayerGoldChangeEvent;
import io.lama06.zombies.event.weapon.WeaponAmmoChangeEvent;
import io.lama06.zombies.event.weapon.WeaponClipChangeEvent;
import io.lama06.zombies.weapon.AmmoData;
import io.lama06.zombies.weapon.Weapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class InteractWithAmmoShopSystem implements Listener {
    @EventHandler
    private void onPlayerInteractAtEntity(final PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof final ArmorStand armorStand)) {
            return;
        }

        final PersistentDataContainer pdc = armorStand.getPersistentDataContainer();
        if (!pdc.has(AmmoShopDisplaySystem.getAmmoShopKey(), PersistentDataType.BOOLEAN)) {
            return;
        }

        final Integer shopIndex = pdc.get(AmmoShopDisplaySystem.getAmmoShopIndexKey(), PersistentDataType.INTEGER);
        if (shopIndex == null) {
            return;
        }

        final ZombiesPlayer player = new ZombiesPlayer(event.getPlayer());
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }

        final var ammoShops = world.getConfig().ammoShops;
        if (shopIndex < 0 || shopIndex >= ammoShops.size()) {
            return;
        }

        final AmmoShop ammoShop = ammoShops.get(shopIndex);

        // Check if player is holding a weapon
        final Weapon heldWeapon = player.getHeldWeapon();
        if (heldWeapon == null) {
            player.sendMessage(Component.text("You must hold a weapon to buy ammo").color(NamedTextColor.RED));
            return;
        }

        // Check if weapon has ammo component
        final io.lama06.zombies.data.Component ammoComponent = heldWeapon.getComponent(Weapon.AMMO);
        if (ammoComponent == null) {
            player.sendMessage(Component.text("This weapon does not use ammo").color(NamedTextColor.RED));
            return;
        }

        // Check if weapon needs ammo
        final AmmoData ammoData = heldWeapon.getData().ammo;
        final int clip = ammoComponent.get(AmmoData.CLIP);
        final int ammo = ammoComponent.get(AmmoData.AMMO);
        final int maxAmmo = ammoData.ammo();
        final int maxClip = ammoData.clip();

        if (ammo == maxAmmo && clip == maxClip) {
            player.sendMessage(Component.text("Your weapon already has full ammo").color(NamedTextColor.RED));
            return;
        }

        // Check if player has enough gold
        final int gold = player.get(ZombiesPlayer.GOLD);
        if (gold < ammoShop.price) {
            player.sendMessage(Component.text("You cannot afford ammo").color(NamedTextColor.RED));
            return;
        }

        // Refill ammo
        ammoComponent.set(AmmoData.CLIP, maxClip);
        ammoComponent.set(AmmoData.AMMO, maxAmmo);
        Bukkit.getPluginManager().callEvent(new WeaponAmmoChangeEvent(heldWeapon, ammo, maxAmmo));
        Bukkit.getPluginManager().callEvent(new WeaponClipChangeEvent(heldWeapon, clip, maxClip));

        // Deduct gold
        player.set(ZombiesPlayer.GOLD, gold - ammoShop.price);
        Bukkit.getPluginManager().callEvent(new PlayerGoldChangeEvent(player, gold, gold - ammoShop.price));

        player.sendMessage(Component.text("Successfully refilled ammo").color(NamedTextColor.GREEN));
    }
}
