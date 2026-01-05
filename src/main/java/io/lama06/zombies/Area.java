package io.lama06.zombies;

import io.lama06.zombies.menu.*;
import io.lama06.zombies.system.AreaVisualizationSystem;
import io.lama06.zombies.util.BlockArea;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

    public void splitOverlappingBounds() {
        if (bounds.size() < 2) return;

        final List<BlockArea> result = new ArrayList<>();
        final List<BlockArea> toProcess = new ArrayList<>(bounds);

        while (!toProcess.isEmpty()) {
            final BlockArea current = toProcess.remove(0);
            boolean hadOverlap = false;

            for (int i = 0; i < toProcess.size(); i++) {
                final BlockArea other = toProcess.get(i);
                if (overlaps(current, other)) {
                    hadOverlap = true;
                    final List<BlockArea> splitResult = splitTwoOverlapping(current, other);
                    toProcess.remove(i);
                    toProcess.addAll(splitResult);
                    break;
                }
            }

            if (!hadOverlap) {
                result.add(current);
            }
        }

        bounds.clear();
        bounds.addAll(result);
    }

    private boolean overlaps(final BlockArea a, final BlockArea b) {
        return a.getLowerX() <= b.getUpperX() && a.getUpperX() >= b.getLowerX() &&
               a.getLowerY() <= b.getUpperY() && a.getUpperY() >= b.getLowerY() &&
               a.getLowerZ() <= b.getUpperZ() && a.getUpperZ() >= b.getLowerZ();
    }

    private List<BlockArea> splitTwoOverlapping(final BlockArea a, final BlockArea b) {
        // Collect all split points on each axis
        final List<Integer> xPoints = Stream.of(
                a.getLowerX(), a.getUpperX() + 1, b.getLowerX(), b.getUpperX() + 1
        ).distinct().sorted().toList();

        final List<Integer> yPoints = Stream.of(
                a.getLowerY(), a.getUpperY() + 1, b.getLowerY(), b.getUpperY() + 1
        ).distinct().sorted().toList();

        final List<Integer> zPoints = Stream.of(
                a.getLowerZ(), a.getUpperZ() + 1, b.getLowerZ(), b.getUpperZ() + 1
        ).distinct().sorted().toList();

        final List<BlockArea> result = new ArrayList<>();

        // Create grid cells
        for (int xi = 0; xi < xPoints.size() - 1; xi++) {
            for (int yi = 0; yi < yPoints.size() - 1; yi++) {
                for (int zi = 0; zi < zPoints.size() - 1; zi++) {
                    final int x1 = xPoints.get(xi);
                    final int y1 = yPoints.get(yi);
                    final int z1 = zPoints.get(zi);
                    final int x2 = xPoints.get(xi + 1) - 1;
                    final int y2 = yPoints.get(yi + 1) - 1;
                    final int z2 = zPoints.get(zi + 1) - 1;

                    if (x2 < x1 || y2 < y1 || z2 < z1) continue;

                    final BlockArea cell = new BlockArea(
                            Position.block(x1, y1, z1),
                            Position.block(x2, y2, z2)
                    );

                    // Check if cell center is within a or b
                    final BlockPosition center = Position.block(
                            (x1 + x2) / 2, (y1 + y2) / 2, (z1 + z2) / 2
                    );

                    if (a.containsBlock(center) || b.containsBlock(center)) {
                        result.add(cell);
                    }
                }
            }
        }

        return result;
    }

    public void mergeAdjacentBounds() {
        if (bounds.size() < 2) return;

        boolean merged;
        do {
            merged = false;
            outer:
            for (int i = 0; i < bounds.size(); i++) {
                for (int j = i + 1; j < bounds.size(); j++) {
                    final BlockArea a = bounds.get(i);
                    final BlockArea b = bounds.get(j);
                    final BlockArea mergedArea = tryMerge(a, b);
                    if (mergedArea != null) {
                        bounds.remove(j);
                        bounds.remove(i);
                        bounds.add(mergedArea);
                        merged = true;
                        break outer;
                    }
                }
            }
        } while (merged);
    }

    private BlockArea tryMerge(final BlockArea a, final BlockArea b) {
        // Check X-axis adjacent (Y,Z aligned)
        if (alignedYZ(a, b) && adjacentX(a, b)) {
            return mergeOnX(a, b);
        }
        // Check Y-axis adjacent (X,Z aligned)
        if (alignedXZ(a, b) && adjacentY(a, b)) {
            return mergeOnY(a, b);
        }
        // Check Z-axis adjacent (X,Y aligned)
        if (alignedXY(a, b) && adjacentZ(a, b)) {
            return mergeOnZ(a, b);
        }
        return null;
    }

    private boolean alignedYZ(final BlockArea a, final BlockArea b) {
        return a.getLowerY() == b.getLowerY() && a.getUpperY() == b.getUpperY() &&
               a.getLowerZ() == b.getLowerZ() && a.getUpperZ() == b.getUpperZ();
    }

    private boolean alignedXZ(final BlockArea a, final BlockArea b) {
        return a.getLowerX() == b.getLowerX() && a.getUpperX() == b.getUpperX() &&
               a.getLowerZ() == b.getLowerZ() && a.getUpperZ() == b.getUpperZ();
    }

    private boolean alignedXY(final BlockArea a, final BlockArea b) {
        return a.getLowerX() == b.getLowerX() && a.getUpperX() == b.getUpperX() &&
               a.getLowerY() == b.getLowerY() && a.getUpperY() == b.getUpperY();
    }

    private boolean adjacentX(final BlockArea a, final BlockArea b) {
        return a.getUpperX() + 1 == b.getLowerX() || b.getUpperX() + 1 == a.getLowerX();
    }

    private boolean adjacentY(final BlockArea a, final BlockArea b) {
        return a.getUpperY() + 1 == b.getLowerY() || b.getUpperY() + 1 == a.getLowerY();
    }

    private boolean adjacentZ(final BlockArea a, final BlockArea b) {
        return a.getUpperZ() + 1 == b.getLowerZ() || b.getUpperZ() + 1 == a.getLowerZ();
    }

    private BlockArea mergeOnX(final BlockArea a, final BlockArea b) {
        return new BlockArea(
                Position.block(Math.min(a.getLowerX(), b.getLowerX()), a.getLowerY(), a.getLowerZ()),
                Position.block(Math.max(a.getUpperX(), b.getUpperX()), a.getUpperY(), a.getUpperZ())
        );
    }

    private BlockArea mergeOnY(final BlockArea a, final BlockArea b) {
        return new BlockArea(
                Position.block(a.getLowerX(), Math.min(a.getLowerY(), b.getLowerY()), a.getLowerZ()),
                Position.block(a.getUpperX(), Math.max(a.getUpperY(), b.getUpperY()), a.getUpperZ())
        );
    }

    private BlockArea mergeOnZ(final BlockArea a, final BlockArea b) {
        return new BlockArea(
                Position.block(a.getLowerX(), a.getLowerY(), Math.min(a.getLowerZ(), b.getLowerZ())),
                Position.block(a.getUpperX(), a.getUpperY(), Math.max(a.getUpperZ(), b.getUpperZ()))
        );
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
                        () -> openBoundsMenu(player, reopen)
                )
        );
    }

    private void openBoundsMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openBoundsMenu(player, callback);

        final List<SelectionEntry> entries = new ArrayList<>();

        // Remove any null values that may have been added by previous bugs
        bounds.removeIf(b -> b == null);

        // Add button
        entries.add(new SelectionEntry(
                Component.text("Add Region").color(NamedTextColor.GREEN),
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
        ));

        // Visualization toggle button
        final AreaVisualizationSystem vizSystem = AreaVisualizationSystem.getInstance();
        final boolean isVisualizing = vizSystem != null && vizSystem.isVisualizing(player, this);
        entries.add(new SelectionEntry(
                Component.text(isVisualizing ? "Hide Visualization" : "Show Visualization")
                        .color(NamedTextColor.LIGHT_PURPLE),
                Material.ENDER_EYE,
                () -> {
                    if (vizSystem != null) {
                        vizSystem.toggleVisualization(player, this);
                    }
                    reopen.run();
                }
        ));

        // Split overlapping regions button
        entries.add(new SelectionEntry(
                Component.text("Split Overlapping Regions").color(NamedTextColor.GOLD),
                Material.SHEARS,
                () -> {
                    final int beforeCount = bounds.size();
                    splitOverlappingBounds();
                    player.sendMessage(Component.text("Split from " + beforeCount + " to " + bounds.size() + " regions")
                            .color(NamedTextColor.GREEN));
                    reopen.run();
                }
        ));

        // Merge adjacent regions button
        entries.add(new SelectionEntry(
                Component.text("Merge Adjacent Regions").color(NamedTextColor.AQUA),
                Material.SLIME_BALL,
                () -> {
                    final int beforeCount = bounds.size();
                    mergeAdjacentBounds();
                    player.sendMessage(Component.text("Merged from " + beforeCount + " to " + bounds.size() + " regions")
                            .color(NamedTextColor.GREEN));
                    reopen.run();
                }
        ));

        // Existing bounds with delete option
        for (int i = 0; i < bounds.size(); i++) {
            final int index = i;
            final BlockArea bound = bounds.get(i);
            entries.add(new SelectionEntry(
                    Component.text("Region " + (i + 1) + ": " + bound.toString()),
                    Material.STONE,
                    reopen,  // Click does nothing, just reopens
                    Component.text("Delete").color(NamedTextColor.RED),
                    () -> {
                        bounds.remove(index);
                        reopen.run();
                    }
            ));
        }

        SelectionMenu.open(
                player,
                Component.text("Area Bounds"),
                callback,
                entries.toArray(SelectionEntry[]::new)
        );
    }
}
