package io.lama06.zombies.system;

import io.lama06.zombies.LuckyChest;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.event.GameStartEvent;
import io.papermc.paper.math.FinePosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public final class LuckyChestDisplaySystem implements Listener {
    private static final NamespacedKey LUCKY_CHEST_DISPLAY_KEY = new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_display");

    @EventHandler(priority = EventPriority.HIGH)
    private void onGameStart(final GameStartEvent event) {
        final ZombiesWorld world = event.getWorld();
        final WorldConfig config = world.getConfig();
        if (config == null) {
            return;
        }

        final World bukkit = world.getBukkit();

        for (final LuckyChest chest : config.luckyChests) {
            // 获取物品位置（考虑大箱子）
            final FinePosition itemPosition = chest.getItemPosition(bukkit);

            // 文字位置：物品位置上方 1.5 格
            final Location textLocation = new Location(
                    bukkit,
                    itemPosition.x(),
                    itemPosition.y() + 0.5,  // getItemPosition 已经 +1，再加 0.5 = 1.5
                    itemPosition.z()
            );

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
            });
        }
    }

    @EventHandler
    private void onGameEnd(final GameEndEvent event) {
        final World bukkit = event.getWorld().getBukkit();

        bukkit.getEntitiesByClass(TextDisplay.class).stream()
                .filter(display -> display.getPersistentDataContainer().has(LUCKY_CHEST_DISPLAY_KEY, PersistentDataType.BOOLEAN))
                .forEach(TextDisplay::remove);
    }
}
