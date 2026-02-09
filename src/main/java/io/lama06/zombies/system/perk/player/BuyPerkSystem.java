package io.lama06.zombies.system.perk.player;

import io.lama06.zombies.PlaceholderItem;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.perk.PerkMachine;
import io.lama06.zombies.perk.PerkOffer;
import io.lama06.zombies.perk.PlayerPerk;
import io.lama06.zombies.menu.SelectionEntry;
import io.lama06.zombies.menu.SelectionMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public final class BuyPerkSystem implements Listener {
    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().isLeftClick() && !event.getAction().isRightClick()) {
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
        final PerkMachine machine = world.getConfig().perkMachines.stream()
                .filter(perkMachine -> perkMachine.position.equals(clickedBlock.getLocation().toBlock()))
                .findAny().orElse(null);
        if (machine == null) {
            return;
        }
        event.setCancelled(true);
        if (!Boolean.TRUE.equals(world.get(ZombiesWorld.POWER_SWITCH))) {
            player.sendMessage(Component.text("The power switch isn't enabled"));
            return;
        }

        final List<PerkOffer> offers = machine.getOffers();
        if (offers.isEmpty()) {
            player.sendMessage(Component.text("This vending machine has no perks configured").color(NamedTextColor.RED));
            return;
        }

        openMachineMenu(player, offers);
    }

    private void openMachineMenu(final ZombiesPlayer player, final List<PerkOffer> offers) {
        final List<SelectionEntry> entries = new ArrayList<>();
        for (final PerkOffer offer : offers) {
            entries.add(new SelectionEntry(
                    offer.perk.getDisplayName()
                            .append(Component.text(" - " + offer.gold + " Gold").color(NamedTextColor.GOLD)),
                    offer.perk.getDisplayMaterial(),
                    () -> tryBuyPerk(player, offer)
            ));
        }
        SelectionMenu.open(
                player.getBukkit(),
                Component.text("Vending Machine"),
                () -> {},
                entries.toArray(SelectionEntry[]::new)
        );
    }

    private void tryBuyPerk(final ZombiesPlayer player, final PerkOffer offer) {
        if (player.hasPerk(offer.perk)) {
            player.sendMessage(Component.text("You already own this perk").color(NamedTextColor.RED));
            return;
        }

        final Integer emptySlot = getEmptyPerkSlot(player);
        if (emptySlot != null) {
            completePurchase(player, offer, emptySlot);
            return;
        }

        openReplaceMenu(player, offer);
    }

    private void openReplaceMenu(final ZombiesPlayer player, final PerkOffer offer) {
        final List<SelectionEntry> entries = new ArrayList<>();
        for (int slot = 6; slot < 9; slot++) {
            final int replaceSlot = slot;
            final PlayerPerk equippedPerk = player.getPerk(slot);
            if (equippedPerk == null) {
                continue;
            }
            entries.add(new SelectionEntry(
                    Component.text("Replace ").append(equippedPerk.getDisplayName()),
                    equippedPerk.getDisplayMaterial(),
                    () -> completePurchase(player, offer, replaceSlot)
            ));
        }
        if (entries.isEmpty()) {
            player.sendMessage(Component.text("No perk slot available").color(NamedTextColor.RED));
            return;
        }
        SelectionMenu.open(
                player.getBukkit(),
                Component.text("Replace Perk"),
                () -> {},
                entries.toArray(SelectionEntry[]::new)
        );
    }

    private Integer getEmptyPerkSlot(final ZombiesPlayer player) {
        final PlayerInventory inventory = player.getBukkit().getInventory();
        for (int slot = 6; slot < 9; slot++) {
            final ItemStack item = inventory.getItem(slot);
            if (PlaceholderItem.isPlaceholder(item)) {
                return slot;
            }
        }
        return null;
    }

    private void completePurchase(final ZombiesPlayer player, final PerkOffer offer, final int slot) {
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }
        if (!Boolean.TRUE.equals(world.get(ZombiesWorld.POWER_SWITCH))) {
            player.sendMessage(Component.text("The power switch isn't enabled"));
            return;
        }
        if (slot < 6 || slot >= 9) {
            player.sendMessage(Component.text("Invalid perk slot").color(NamedTextColor.RED));
            return;
        }
        if (player.hasPerk(offer.perk)) {
            player.sendMessage(Component.text("You already own this perk").color(NamedTextColor.RED));
            return;
        }
        if (!player.requireGold(offer.gold)) {
            return;
        }

        player.givePerk(slot, offer.perk);
        player.pay(offer.gold);
        player.sendMessage(Component.text("Successfully bought ").color(NamedTextColor.GREEN).append(offer.perk.getDisplayName()));
    }
}
