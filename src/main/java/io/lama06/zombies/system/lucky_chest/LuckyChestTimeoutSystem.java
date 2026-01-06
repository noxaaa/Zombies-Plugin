package io.lama06.zombies.system.lucky_chest;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.LuckyChest;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public final class LuckyChestTimeoutSystem implements Listener {
    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            if (!world.isGameRunning()) {
                continue;
            }
            final WorldConfig config = world.getConfig();
            if (config == null) {
                continue;
            }

            final Collection<ItemDisplay> itemDisplays = world.getBukkit().getEntitiesByClass(ItemDisplay.class);
            for (final ItemDisplay itemDisplay : itemDisplays) {
                final PersistentDataContainer pdc = itemDisplay.getPersistentDataContainer();

                // 检查是否是 lucky chest 物品
                if (!pdc.has(LuckyChestItem.getShuffleCountKey(), PersistentDataType.INTEGER)) {
                    continue;
                }

                // 检查是否有领取倒计时
                final Integer claimTimeout = pdc.get(LuckyChestItem.getClaimTimeoutKey(), PersistentDataType.INTEGER);
                if (claimTimeout == null) {
                    continue;
                }

                if (claimTimeout > 0) {
                    // 递减倒计时
                    pdc.set(LuckyChestItem.getClaimTimeoutKey(), PersistentDataType.INTEGER, claimTimeout - 1);
                } else {
                    // 超时，移除物品并关闭箱子
                    final Integer chestIndex = pdc.get(LuckyChestItem.getChestIndexKey(), PersistentDataType.INTEGER);
                    if (chestIndex != null && chestIndex >= 0 && chestIndex < config.luckyChests.size()) {
                        final LuckyChest chest = config.luckyChests.get(chestIndex);
                        InteractWithLuckyChestSystem.setChestOpen(chest, world.getBukkit(), false);
                    }
                    itemDisplay.remove();
                }
            }
        }
    }
}
