package io.lama06.zombies.system.perk.global;

import io.lama06.zombies.RoundConfig;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.StartRoundEvent;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public final class ClearPerkItemsOnRoundStartSystem implements Listener {
    @EventHandler
    private void onRoundStart(final StartRoundEvent event) {
        final ZombiesWorld world = event.getWorld();
        final WorldConfig config = world.getConfig();
        if (config == null) {
            return;
        }

        final int round = event.getRound();
        if (round < 1 || round > config.rounds.size()) {
            return;
        }

        final RoundConfig roundConfig = config.rounds.get(round - 1);

        // 只有当设置为true时才清理
        if (!roundConfig.clearBuffsOnRoundStart) {
            return;
        }

        // 移除所有perk ItemDisplay实体
        final Collection<ItemDisplay> displays = world.getBukkit().getEntitiesByClass(ItemDisplay.class);
        for (final ItemDisplay display : displays) {
            final PersistentDataContainer pdc = display.getPersistentDataContainer();
            if (pdc.has(PerkItem.getPerkNameKey(), PersistentDataType.STRING)) {
                display.remove();
            }
        }
    }
}
