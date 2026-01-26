package io.lama06.zombies.system.lucky_chest;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.menu.MenuDisplayableEnum;
import io.lama06.zombies.skill.SkillType;
import io.lama06.zombies.util.pdc.EnumPersistentDataType;
import io.lama06.zombies.weapon.WeaponType;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class ShuffleLuckyChestItemSystem implements Listener {
    private record LuckyChestEntry(String type, MenuDisplayableEnum item) {}

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            final WorldConfig config = world.getConfig();
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

                // 收集所有可用的武器和技能
                final List<LuckyChestEntry> entries = new ArrayList<>();

                // 武器（优先使用配置，否则使用默认）
                final List<WeaponType> weaponTypes;
                if (config != null && !config.luckyChestWeapons.isEmpty()) {
                    weaponTypes = config.luckyChestWeapons;
                } else {
                    weaponTypes = Arrays.stream(WeaponType.values())
                            .filter(weaponType -> weaponType.data.inLuckyChest).toList();
                }
                for (final WeaponType weaponType : weaponTypes) {
                    entries.add(new LuckyChestEntry("WEAPON", weaponType));
                }

                // 技能
                final List<SkillType> skillTypes = Arrays.stream(SkillType.values())
                        .filter(skillType -> skillType.data.inLuckyChest).toList();
                for (final SkillType skillType : skillTypes) {
                    entries.add(new LuckyChestEntry("SKILL", skillType));
                }

                // 随机选择
                final RandomGenerator rnd = ThreadLocalRandom.current();
                final LuckyChestEntry entry = entries.get(rnd.nextInt(entries.size()));

                // 设置 PDC
                pdc.set(LuckyChestItem.getItemTypeKey(), PersistentDataType.STRING, entry.type);
                if ("WEAPON".equals(entry.type)) {
                    final WeaponType weaponType = (WeaponType) entry.item;
                    pdc.set(LuckyChestItem.getWeaponKey(), new EnumPersistentDataType<>(WeaponType.class), weaponType);
                    pdc.remove(LuckyChestItem.getSkillKey());
                } else {
                    final SkillType skillType = (SkillType) entry.item;
                    pdc.set(LuckyChestItem.getSkillKey(), new EnumPersistentDataType<>(SkillType.class), skillType);
                    pdc.remove(LuckyChestItem.getWeaponKey());
                }

                // 更新显示（缩小到0.5倍）
                itemDisplay.setItemStack(new ItemStack(entry.item.getDisplayMaterial()));
                itemDisplay.setCustomNameVisible(true);
                itemDisplay.customName(entry.item.getDisplayName());
                itemDisplay.setBillboard(Display.Billboard.VERTICAL);
                itemDisplay.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 1),
                        new Vector3f(0.5f, 0.5f, 0.5f),
                        new AxisAngle4f(0, 0, 0, 1)
                ));

                // 播放音效（循环 pitch）
                final float pitch = LuckyChestItem.getPitchForShuffle(newShuffleCount);
                world.getBukkit().playSound(itemDisplay.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.BLOCKS, 1, pitch);

                // 设置下一次切换的间隔（如果还没完成）
                if (newShuffleCount < LuckyChestItem.TOTAL_SHUFFLES) {
                    final int nextInterval = LuckyChestItem.getIntervalTicksForShuffle(newShuffleCount);
                    pdc.set(LuckyChestItem.getNextShuffleTimeKey(), PersistentDataType.INTEGER, nextInterval);
                }

                // 完成后设置领取倒计时
                if (newShuffleCount >= LuckyChestItem.TOTAL_SHUFFLES) {
                    pdc.set(LuckyChestItem.getRemainingTimeKey(), PersistentDataType.INTEGER, 0);
                    pdc.set(LuckyChestItem.getClaimTimeoutKey(), PersistentDataType.INTEGER, LuckyChestItem.CLAIM_TIMEOUT_TICKS);
                }
            }
        }
    }
}
