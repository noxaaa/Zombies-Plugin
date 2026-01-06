package io.lama06.zombies.system.lucky_chest;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.util.pdc.EnumPersistentDataType;
import io.lama06.zombies.weapon.WeaponType;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class ShuffleLuckyChestItemSystem implements Listener {
    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            final Collection<ItemDisplay> itemDisplays = world.getBukkit().getEntitiesByClass(ItemDisplay.class);
            for (final ItemDisplay itemDisplay : itemDisplays) {
                final PersistentDataContainer pdc = itemDisplay.getPersistentDataContainer();

                // 检查是否是 lucky chest 物品
                final Integer shuffleCount = pdc.get(LuckyChestItem.getShuffleCountKey(), PersistentDataType.INTEGER);
                if (shuffleCount == null) {
                    continue;
                }

                // 已完成所有切换
                if (shuffleCount >= LuckyChestItem.TOTAL_SHUFFLES) {
                    continue;
                }

                // 检查是否到了下一次切换的时间
                final Integer nextShuffleTime = pdc.get(LuckyChestItem.getNextShuffleTimeKey(), PersistentDataType.INTEGER);
                if (nextShuffleTime == null) {
                    continue;
                }

                if (nextShuffleTime > 0) {
                    // 还没到时间，递减计时器
                    pdc.set(LuckyChestItem.getNextShuffleTimeKey(), PersistentDataType.INTEGER, nextShuffleTime - 1);
                    continue;
                }

                // 执行切换
                final int newShuffleCount = shuffleCount + 1;
                pdc.set(LuckyChestItem.getShuffleCountKey(), PersistentDataType.INTEGER, newShuffleCount);

                // 随机选择武器
                final List<WeaponType> weaponTypes = Arrays.stream(WeaponType.values())
                        .filter(weaponType -> weaponType.data.inLuckyChest).toList();
                final RandomGenerator rnd = ThreadLocalRandom.current();
                final WeaponType weaponType = weaponTypes.get(rnd.nextInt(weaponTypes.size()));
                pdc.set(LuckyChestItem.getWeaponKey(), new EnumPersistentDataType<>(WeaponType.class), weaponType);

                // 更新显示
                itemDisplay.setItemStack(new ItemStack(weaponType.getDisplayMaterial()));
                itemDisplay.setCustomNameVisible(true);
                itemDisplay.customName(weaponType.getDisplayName());

                // 播放音效（循环 pitch）
                final float pitch = LuckyChestItem.getPitchForShuffle(newShuffleCount);
                world.getBukkit().playSound(itemDisplay.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.BLOCKS, 1, pitch);

                // 设置下一次切换的间隔（如果还没完成）
                if (newShuffleCount < LuckyChestItem.TOTAL_SHUFFLES) {
                    final int nextInterval = LuckyChestItem.getIntervalTicksForShuffle(newShuffleCount);
                    pdc.set(LuckyChestItem.getNextShuffleTimeKey(), PersistentDataType.INTEGER, nextInterval);
                }

                // 完成后标记 remainingTime 为 0（用于 claimItem 检测）
                if (newShuffleCount >= LuckyChestItem.TOTAL_SHUFFLES) {
                    pdc.set(LuckyChestItem.getRemainingTimeKey(), PersistentDataType.INTEGER, 0);
                }
            }
        }
    }
}
