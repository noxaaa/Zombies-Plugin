package io.lama06.zombies.system;

import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.weapon.Weapon;
import io.lama06.zombies.weapon.WeaponData;
import io.lama06.zombies.weapon.WeaponType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class UltimateMachineSystem implements Listener {
    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final ZombiesPlayer player = new ZombiesPlayer(event.getPlayer());
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }
        final WorldConfig config = world.getConfig();
        if (config.ultimateMachine == null) {
            return;
        }
        final Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!clickedBlock.getLocation().toBlock().equals(config.ultimateMachine)) {
            return;
        }
        if (!world.get(ZombiesWorld.POWER_SWITCH)) {
            player.sendMessage(Component.text("The power switch isn't enabled"));
            return;
        }

        // 获取玩家手持的武器
        final Weapon heldWeapon = player.getHeldWeapon();
        if (heldWeapon == null) {
            player.sendMessage(Component.text("You must hold a weapon to upgrade").color(NamedTextColor.RED));
            return;
        }

        final WeaponData weaponData = heldWeapon.getData();
        if (weaponData.upgradesTo == null) {
            player.sendMessage(Component.text("This weapon cannot be upgraded").color(NamedTextColor.RED));
            return;
        }

        final int price = config.ultimateMachinePrice;
        if (!player.requireGold(price)) {
            return;
        }

        // 扣除金币
        player.pay(price);

        // 升级武器
        final WeaponType upgradedType = weaponData.upgradesTo;
        final int slot = heldWeapon.getSlot();

        // 给予升级后的武器（满弹药）
        player.giveWeapon(slot, upgradedType);

        player.sendMessage(Component.text("Weapon upgraded to ").color(NamedTextColor.GREEN)
                .append(upgradedType.data.displayName));
    }
}
