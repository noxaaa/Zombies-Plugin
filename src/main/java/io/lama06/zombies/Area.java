package io.lama06.zombies;

import io.lama06.zombies.menu.*;
import io.lama06.zombies.util.BlockArea;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class Area implements CheckableConfig {
    public String name = "";
    public final List<BlockArea> bounds = new ArrayList<>();

    public boolean contains(final Location loc) {
        final BlockPosition pos = Position.block(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return bounds.stream().anyMatch(area -> area.containsBlock(pos));
    }

    @Override
    public void check() throws InvalidConfigException {
        InvalidConfigException.mustBeSet(name, "name");
        if (bounds.isEmpty()) {
            throw new InvalidConfigException("bounds cannot be empty");
        }
    }

    public void openMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openMenu(player, callback);

        SelectionMenu.open(
                player,
                Component.text("Edit Area"),
                callback,
                new SelectionEntry(
                        Component.text("Name: " + (name.isEmpty() ? "_" : name)),
                        Material.OAK_FENCE,
                        () -> InputMenu.open(
                                player,
                                Component.text("Area Name"),
                                name,
                                new TextInputType(),
                                newName -> {
                                    this.name = newName;
                                    reopen.run();
                                },
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Bounds (" + bounds.size() + " regions)"),
                        Material.GLASS,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Area Bounds"),
                                bounds,
                                Material.STONE,
                                bound -> Component.text(bound.toString()),
                                () -> null,  // Will be replaced by BlockAreaSelection
                                bound -> reopen.run(),  // No sub-edit for BlockArea
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Add Bound Region"),
                        Material.GREEN_STAINED_GLASS_PANE,
                        () -> BlockAreaSelection.open(
                                player,
                                Component.text("Select Bound Region"),
                                reopen,
                                bound -> {
                                    bounds.add(bound);
                                    reopen.run();
                                }
                        )
                )
        );
    }
}
