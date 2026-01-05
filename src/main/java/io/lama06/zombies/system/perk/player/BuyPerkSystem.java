package io.lama06.zombies.system.perk.player;

import io.lama06.zombies.PlaceholderItem;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.perk.PerkMachine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class BuyPerkSystem implements Listener {
    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().isLeftClick()) {
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
        final PerkMachine perkMachine = world.getConfig().perkMachines.stream()
                .filter(machine -> machine.position.equals(clickedBlock.getLocation().toBlock()))
                .findAny().orElse(null);
        if (perkMachine == null) {
            return;
        }
        if (!world.get(ZombiesWorld.POWER_SWITCH)) {
            player.sendMessage(Component.text("The power switch isn't enabled"));
            return;
        }
        if (player.hasPerk(perkMachine.perk)) {
            player.sendMessage(Component.text("You already own this perk").color(NamedTextColor.RED));
            return;
        }
        if (!player.requireGold(perkMachine.gold)) {
            return;
        }

        final PlayerInventory inventory = player.getBukkit().getInventory();

        // Find first empty perk slot (placeholder)
        Integer emptySlot = null;
        for (int slot = 6; slot < 9; slot++) {
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
            // No empty slot, must be holding a perk to replace
            final int heldSlot = inventory.getHeldItemSlot();
            if (heldSlot < 6 || heldSlot >= 9) {
                player.sendMessage(Component.text("No empty perk slot. Hold a perk to replace.").color(NamedTextColor.RED));
                return;
            }
            if (player.getPerk(heldSlot) == null) {
                player.sendMessage(Component.text("Hold a perk to replace.").color(NamedTextColor.RED));
                return;
            }
            targetSlot = heldSlot;
        }

        player.givePerk(targetSlot, perkMachine.perk);
        player.pay(perkMachine.gold);
        player.sendMessage(Component.text("Successfully bought ").color(NamedTextColor.GREEN).append(perkMachine.perk.getDisplayName()));
    }
}
