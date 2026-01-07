package io.lama06.zombies.system.perk.global;

import io.lama06.zombies.*;
import io.lama06.zombies.event.StartRoundEvent;
import io.lama06.zombies.perk.GlobalPerk;
import io.lama06.zombies.zombie.Zombie;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class SpawnPerkItemsOnZombieDeathSystem implements Listener {
    private static final int PERK_ITEM_TIME = 60 * 20;  // 60秒

    // 追踪每个round预定的掉落: worldName_round -> (zombieDeathIndex -> perk)
    private final Map<String, Map<Integer, GlobalPerk>> roundPerkDropSchedule = new HashMap<>();
    // 追踪每个round的僵尸死亡计数
    private final Map<String, Integer> zombieDeathCounter = new HashMap<>();

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
        final int totalZombiesInRound = roundConfig.getTotalZombies();
        if (totalZombiesInRound <= 0) {
            return;
        }

        final String worldKey = world.getBukkit().getName() + "_" + round;
        zombieDeathCounter.put(worldKey, 0);

        // 判定每个perk是否应该在本round掉落
        final Map<Integer, GlobalPerk> dropSchedule = new HashMap<>();
        final RandomGenerator rnd = ThreadLocalRandom.current();

        for (final GlobalPerk perk : GlobalPerk.values()) {
            // 跳过已经掉落过的perk
            if (world.hasPerkDropped(perk)) {
                continue;
            }

            // 获取本round该perk的掉落概率
            final double probability = roundConfig.getPerkDropProbability(perk);

            // roll是否掉落
            if (rnd.nextDouble() < probability) {
                // 选择一个随机的僵尸死亡序号来掉落
                int zombieIndex;
                int attempts = 0;
                do {
                    zombieIndex = rnd.nextInt(totalZombiesInRound);
                    attempts++;
                } while (dropSchedule.containsKey(zombieIndex) && attempts < 100);

                if (!dropSchedule.containsKey(zombieIndex)) {
                    dropSchedule.put(zombieIndex, perk);
                }
            }
        }

        roundPerkDropSchedule.put(worldKey, dropSchedule);
    }

    @EventHandler
    private void onEntityDeath(final EntityDeathEvent event) {
        final Zombie zombie = new Zombie(event.getEntity());
        if (!zombie.isZombie()) {
            return;
        }

        final World bukkitWorld = event.getEntity().getWorld();
        final ZombiesWorld world = new ZombiesWorld(bukkitWorld);
        if (!world.isGameRunning()) {
            return;
        }

        final Integer currentRound = world.get(ZombiesWorld.ROUND);
        if (currentRound == null) {
            return;
        }

        final String worldKey = bukkitWorld.getName() + "_" + currentRound;

        // 获取当前死亡序号并递增
        final int deathIndex = zombieDeathCounter.getOrDefault(worldKey, 0);
        zombieDeathCounter.put(worldKey, deathIndex + 1);

        // 检查是否应该掉落perk
        final Map<Integer, GlobalPerk> dropSchedule = roundPerkDropSchedule.get(worldKey);
        if (dropSchedule == null || !dropSchedule.containsKey(deathIndex)) {
            return;
        }

        final GlobalPerk perk = dropSchedule.get(deathIndex);

        // 再次检查是否已掉落（防止竞态条件）
        if (world.hasPerkDropped(perk)) {
            return;
        }

        // 标记为已掉落
        world.markPerkAsDropped(perk);

        // 生成perk物品
        spawnPerkItem(world, zombie, perk);
    }

    private void spawnPerkItem(final ZombiesWorld world, final Zombie zombie, final GlobalPerk perk) {
        final World bukkitWorld = world.getBukkit();

        // 计算生成位置（头部高度）并调整如果在窗户内
        Location spawnLocation = zombie.getEntity().getLocation().clone().add(0, 2, 0);
        spawnLocation = adjustLocationIfInsideWindow(world, spawnLocation);

        final ItemDisplay display = bukkitWorld.spawn(spawnLocation, ItemDisplay.class);
        display.setCustomNameVisible(true);
        display.customName(perk.getDisplayName());
        display.setItemStack(new ItemStack(perk.getMaterial()));
        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(0, 0, 0, 1),
                new Vector3f(0.6f, 0.6f, 0.6f),
                new AxisAngle4f(0, 0, 0, 1)
        ));
        final PersistentDataContainer pdc = display.getPersistentDataContainer();
        pdc.set(PerkItem.getPerkNameKey(), PersistentDataType.STRING, perk.name());
        pdc.set(PerkItem.getRemainingTimeKey(), PersistentDataType.INTEGER, PERK_ITEM_TIME);
    }

    private Location adjustLocationIfInsideWindow(final ZombiesWorld world, final Location location) {
        final WorldConfig config = world.getConfig();
        if (config == null) {
            return location;
        }

        final BlockPosition blockPos = Position.block(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        for (final Window window : config.windows) {
            if (window.blocks != null && window.blocks.containsBlock(blockPos)) {
                // Inside a window, move to repairArea center at head height
                if (window.repairArea != null) {
                    final double centerX = (window.repairArea.getLowerX() + window.repairArea.getUpperX()) / 2.0 + 0.5;
                    final double centerY = window.repairArea.getUpperY() + 2.5;
                    final double centerZ = (window.repairArea.getLowerZ() + window.repairArea.getUpperZ()) / 2.0 + 0.5;
                    return new Location(location.getWorld(), centerX, centerY, centerZ);
                }
            }
        }

        return location;
    }
}
