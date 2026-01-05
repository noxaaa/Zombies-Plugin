package io.lama06.zombies.system.weapon.melee;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.weapon.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 刀格挡系统：右键格挡时减少50%受到的伤害
 */
public final class KnifeBlockingSystem implements Listener {
    private static final int BLOCK_TIMEOUT_TICKS = 4;  // 停止右键后多少tick取消格挡
    private static final double BLOCK_DAMAGE_REDUCTION = 0.5;  // 格挡时伤害减少50%

    private record BlockingState(int lastActivityTick) { }
    private final Map<UUID, BlockingState> blockingPlayers = new HashMap<>();
    private int currentTick = 0;

    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        final Player player = event.getPlayer();
        final ZombiesPlayer zombiesPlayer = new ZombiesPlayer(player);
        if (!zombiesPlayer.getWorld().isGameRunning() || !zombiesPlayer.isAlive()) {
            return;
        }
        final Weapon weapon = zombiesPlayer.getHeldWeapon();
        if (weapon == null || weapon.getData().melee == null) {
            return;  // 只有近战武器可以格挡
        }
        // 标记玩家正在格挡
        blockingPlayers.put(player.getUniqueId(), new BlockingState(currentTick));
    }

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        currentTick++;
        // 清理超时的格挡状态
        final Iterator<Map.Entry<UUID, BlockingState>> iterator = blockingPlayers.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<UUID, BlockingState> entry = iterator.next();
            if (currentTick - entry.getValue().lastActivityTick > BLOCK_TIMEOUT_TICKS) {
                iterator.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onEntityDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!blockingPlayers.containsKey(player.getUniqueId())) {
            return;  // 玩家没有在格挡
        }
        final ZombiesPlayer zombiesPlayer = new ZombiesPlayer(player);
        final Weapon weapon = zombiesPlayer.getHeldWeapon();
        if (weapon == null || weapon.getData().melee == null) {
            blockingPlayers.remove(player.getUniqueId());
            return;  // 玩家切换了武器，取消格挡
        }
        // 减少50%伤害
        final double originalDamage = event.getDamage();
        final double reducedDamage = originalDamage * (1 - BLOCK_DAMAGE_REDUCTION);
        event.setDamage(reducedDamage);
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        blockingPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onPlayerItemHeld(final PlayerItemHeldEvent event) {
        // 切换物品时取消格挡
        blockingPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onGameEnd(final GameEndEvent event) {
        for (final ZombiesPlayer player : event.getWorld().getPlayers()) {
            blockingPlayers.remove(player.getBukkit().getUniqueId());
        }
    }

    /**
     * 检查玩家是否正在格挡
     */
    public boolean isBlocking(final Player player) {
        return blockingPlayers.containsKey(player.getUniqueId());
    }
}
