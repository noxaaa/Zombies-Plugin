package io.lama06.zombies.system.weapon.attack;

import io.lama06.zombies.perk.GlobalPerk;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.player.PlayerAttackZombieEvent;
import io.lama06.zombies.event.player.PlayerGoldChangeEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.weapon.AttackData;
import io.lama06.zombies.weapon.Weapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class GiveGoldAfterAttackSystem implements Listener {
    @EventHandler
    private void onPlayerAttacksZombie(final PlayerAttackZombieEvent event) {
        // 检查僵尸是否存活
        final Entity entity = event.getZombie().getEntity();
        if (entity instanceof LivingEntity living && living.isDead()) {
            return;  // 僵尸已死亡，不给予金币
        }

        final Weapon weapon = event.getWeapon();
        final AttackData attackData = weapon.getData().attack;
        if (attackData == null) {
            return;
        }
        final ZombiesPlayer player = event.getPlayer();
        final ZombiesWorld world = event.getWorld();
        final int goldBefore = player.get(ZombiesPlayer.GOLD);
        int baseGold = attackData.gold();
        if (event.isHeadshot()) {
            baseGold += attackData.headshotBonusGold();
        }
        final int goldAdd = (world.isPerkEnabled(GlobalPerk.DOUBLE_GOLD) ? 2 : 1) * baseGold;
        final int goldAfter = goldBefore + goldAdd;
        player.set(ZombiesPlayer.GOLD, goldAfter);
        if (event.isHeadshot()) {
            player.getBukkit().playSound(player.getBukkit().getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.5f);
            player.sendMessage(Component.text("HEADSHOT! +%s Gold".formatted(goldAdd)).color(NamedTextColor.RED));
        } else {
            player.getBukkit().playSound(player.getBukkit().getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 2.0f);
            player.sendMessage(Component.text("Zombie Attacked: +%s Gold".formatted(goldAdd)).color(NamedTextColor.GOLD));
        }
        Bukkit.getPluginManager().callEvent(new PlayerGoldChangeEvent(player, goldBefore, goldAfter));
    }
}
