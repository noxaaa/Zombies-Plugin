package io.lama06.zombies;

import io.lama06.zombies.menu.*;
import io.lama06.zombies.util.PositionUtil;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class AmmoShop implements CheckableConfig {
    public BlockPosition position;
    public int price = 600;

    @Override
    public void check() throws InvalidConfigException {
        InvalidConfigException.mustBeSet(position, "position");
    }

    public void openMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openMenu(player, callback);
        SelectionMenu.open(
                player,
                Component.text("Ammo Shop"),
                callback,
                new SelectionEntry(
                        Component.text("Position: " + PositionUtil.format(position)),
                        Material.LEVER,
                        () -> BlockPositionSelection.open(
                                player,
                                Component.text("Ammo Shop Position"),
                                reopen,
                                position -> {
                                    this.position = position;
                                    reopen.run();
                                }
                        )
                ),
                new SelectionEntry(
                        Component.text("Price: " + price),
                        Material.GOLD_NUGGET,
                        () -> InputMenu.open(
                                player,
                                Component.text("Ammo Price"),
                                price,
                                new IntegerInputType(),
                                price -> {
                                    this.price = price;
                                    reopen.run();
                                },
                                reopen
                        )
                )
        );
    }
}
