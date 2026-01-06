package io.lama06.zombies.system;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.LuckyChest;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.event.GameStartEvent;
import io.lama06.zombies.system.lucky_chest.LuckyChestItem;
import io.papermc.paper.math.FinePosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Collection;

public final class LuckyChestDisplaySystem implements Listener {
    private static final NamespacedKey LUCKY_CHEST_DISPLAY_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_display");
    private static final NamespacedKey LUCKY_CHEST_DISPLAY_INDEX_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_display_index");
    private static final NamespacedKey LUCKY_CHEST_COUNTDOWN_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_countdown");

    @EventHandler(priority = EventPriority.HIGH)
    private void onGameStart(final GameStartEvent event) {
        final ZombiesWorld world = event.getWorld();
        final WorldConfig config = world.getConfig();
        if (config == null) {
            return;
        }

        final World bukkit = world.getBukkit();

        for (int i = 0; i < config.luckyChests.size(); i++) {
            final LuckyChest chest = config.luckyChests.get(i);
            // 获取物品位置（考虑大箱子）
            final FinePosition itemPosition = chest.getItemPosition(bukkit);

            // 文字位置：物品位置上方 0.5 格
            final Location textLocation = new Location(
                    bukkit,
                    itemPosition.x(),
                    itemPosition.y() + 0.5,
                    itemPosition.z()
            );

            final int chestIndex = i;
            bukkit.spawn(textLocation, TextDisplay.class, display -> {
                // 紫色非粗体 "Lucky Chest"
                // 黄色粗体 "N Gold"
                final Component text = Component.text("Lucky Chest").color(NamedTextColor.LIGHT_PURPLE)
                        .appendNewline()
                        .append(Component.text(chest.gold + " Gold").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));

                display.text(text);
                display.setBillboard(Display.Billboard.CENTER);
                display.setAlignment(TextDisplay.TextAlignment.CENTER);
                display.setShadowed(true);
                display.setBackgroundColor(Color.fromARGB(100, 0, 0, 0));

                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 1),
                        new Vector3f(0.8f, 0.8f, 0.8f),
                        new AxisAngle4f(0, 0, 0, 1)
                ));

                display.getPersistentDataContainer().set(LUCKY_CHEST_DISPLAY_KEY, PersistentDataType.BOOLEAN, true);
                display.getPersistentDataContainer().set(LUCKY_CHEST_DISPLAY_INDEX_KEY, PersistentDataType.INTEGER, chestIndex);
            });
        }
    }

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

            final World bukkit = world.getBukkit();

            // 获取所有 TextDisplay
            final Collection<TextDisplay> textDisplays = bukkit.getEntitiesByClass(TextDisplay.class);

            for (final TextDisplay textDisplay : textDisplays) {
                final PersistentDataContainer displayPdc = textDisplay.getPersistentDataContainer();
                if (!displayPdc.has(LUCKY_CHEST_DISPLAY_KEY, PersistentDataType.BOOLEAN)) {
                    continue;
                }

                final Integer chestIndex = displayPdc.get(LUCKY_CHEST_DISPLAY_INDEX_KEY, PersistentDataType.INTEGER);
                if (chestIndex == null || chestIndex < 0 || chestIndex >= config.luckyChests.size()) {
                    continue;
                }

                final LuckyChest chest = config.luckyChests.get(chestIndex);

                // 查找对应的 ItemDisplay（正在抽奖的物品）
                final ItemDisplay itemDisplay = findLuckyChestItem(chest, bukkit, chestIndex);

                if (itemDisplay == null) {
                    // 没有抽奖进行中，显示默认文字
                    updateDefaultText(textDisplay, chest);
                    // 移除倒计时文字
                    removeCountdownDisplay(bukkit, chestIndex);
                } else {
                    final PersistentDataContainer itemPdc = itemDisplay.getPersistentDataContainer();
                    final Integer shuffleCount = itemPdc.get(LuckyChestItem.getShuffleCountKey(), PersistentDataType.INTEGER);
                    final Integer claimTimeout = itemPdc.get(LuckyChestItem.getClaimTimeoutKey(), PersistentDataType.INTEGER);
                    final String ownerName = itemPdc.get(LuckyChestItem.getOwnerNameKey(), PersistentDataType.STRING);

                    if (shuffleCount != null && shuffleCount < LuckyChestItem.TOTAL_SHUFFLES) {
                        // 正在抽奖中
                        updateRollingText(textDisplay, ownerName);
                        removeCountdownDisplay(bukkit, chestIndex);
                    } else if (claimTimeout != null && claimTimeout > 0) {
                        // 抽奖完成，等待领取
                        updateClaimText(textDisplay);
                        updateCountdownDisplay(bukkit, chest, chestIndex, claimTimeout);
                    }
                }
            }
        }
    }

    private ItemDisplay findLuckyChestItem(final LuckyChest chest, final World world, final int chestIndex) {
        final Collection<ItemDisplay> nearbyItemDisplays = chest.position.toLocation(world).getNearbyEntitiesByType(ItemDisplay.class, 5);
        for (final ItemDisplay itemDisplay : nearbyItemDisplays) {
            final PersistentDataContainer pdc = itemDisplay.getPersistentDataContainer();
            if (!pdc.has(LuckyChestItem.getShuffleCountKey(), PersistentDataType.INTEGER)) {
                continue;
            }
            final Integer storedIndex = pdc.get(LuckyChestItem.getChestIndexKey(), PersistentDataType.INTEGER);
            if (storedIndex != null && storedIndex.equals(chestIndex)) {
                return itemDisplay;
            }
        }
        return null;
    }

    private void updateDefaultText(final TextDisplay display, final LuckyChest chest) {
        final Component text = Component.text("Lucky Chest").color(NamedTextColor.LIGHT_PURPLE)
                .appendNewline()
                .append(Component.text(chest.gold + " Gold").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
        display.text(text);
    }

    private void updateRollingText(final TextDisplay display, final String ownerName) {
        final String name = ownerName != null ? ownerName : "Someone";
        final Component text = Component.text("Lucky Chest").color(NamedTextColor.LIGHT_PURPLE)
                .appendNewline()
                .append(Component.text(name + " is rolling...").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
        display.text(text);
    }

    private void updateClaimText(final TextDisplay display) {
        final Component text = Component.text("Lucky Chest").color(NamedTextColor.LIGHT_PURPLE)
                .appendNewline()
                .append(Component.text("RIGHT-CLICK TO CLAIM").color(NamedTextColor.YELLOW));
        display.text(text);
    }

    private void updateCountdownDisplay(final World world, final LuckyChest chest, final int chestIndex, final int ticksRemaining) {
        final FinePosition itemPosition = chest.getItemPosition(world);
        final Location countdownLocation = new Location(
                world,
                itemPosition.x(),
                itemPosition.y() + 1.5,  // 物品上方1格 (物品在+1，这里+1.5 = 箱子上方2.5格)
                itemPosition.z()
        );

        // 查找现有的倒计时文字
        TextDisplay countdownDisplay = null;
        for (final TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
            final PersistentDataContainer pdc = display.getPersistentDataContainer();
            if (pdc.has(LUCKY_CHEST_COUNTDOWN_KEY, PersistentDataType.INTEGER)) {
                final Integer index = pdc.get(LUCKY_CHEST_COUNTDOWN_KEY, PersistentDataType.INTEGER);
                if (index != null && index.equals(chestIndex)) {
                    countdownDisplay = display;
                    break;
                }
            }
        }

        final double seconds = ticksRemaining / 20.0;
        final Component text = Component.text(String.format("%.1fs", seconds)).color(NamedTextColor.RED);

        if (countdownDisplay == null) {
            // 创建新的倒计时文字
            world.spawn(countdownLocation, TextDisplay.class, display -> {
                display.text(text);
                display.setBillboard(Display.Billboard.CENTER);
                display.setAlignment(TextDisplay.TextAlignment.CENTER);
                display.setShadowed(true);
                display.setBackgroundColor(Color.fromARGB(100, 0, 0, 0));
                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 1),
                        new Vector3f(0.8f, 0.8f, 0.8f),
                        new AxisAngle4f(0, 0, 0, 1)
                ));
                display.getPersistentDataContainer().set(LUCKY_CHEST_COUNTDOWN_KEY, PersistentDataType.INTEGER, chestIndex);
            });
        } else {
            // 更新现有的倒计时文字
            countdownDisplay.text(text);
        }
    }

    private void removeCountdownDisplay(final World world, final int chestIndex) {
        for (final TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
            final PersistentDataContainer pdc = display.getPersistentDataContainer();
            if (pdc.has(LUCKY_CHEST_COUNTDOWN_KEY, PersistentDataType.INTEGER)) {
                final Integer index = pdc.get(LUCKY_CHEST_COUNTDOWN_KEY, PersistentDataType.INTEGER);
                if (index != null && index.equals(chestIndex)) {
                    display.remove();
                    break;
                }
            }
        }
    }

    @EventHandler
    private void onGameEnd(final GameEndEvent event) {
        final World bukkit = event.getWorld().getBukkit();

        bukkit.getEntitiesByClass(TextDisplay.class).stream()
                .filter(display -> {
                    final PersistentDataContainer pdc = display.getPersistentDataContainer();
                    return pdc.has(LUCKY_CHEST_DISPLAY_KEY, PersistentDataType.BOOLEAN) ||
                           pdc.has(LUCKY_CHEST_COUNTDOWN_KEY, PersistentDataType.INTEGER);
                })
                .forEach(TextDisplay::remove);
    }
}
