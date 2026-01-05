package io.lama06.zombies.system;

import io.lama06.zombies.Area;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.util.BlockArea;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class AreaVisualizationSystem implements Listener {
    private static final List<Color> COLORS = List.of(
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.PURPLE, Color.AQUA, Color.ORANGE, Color.WHITE,
            Color.FUCHSIA, Color.LIME, Color.MAROON, Color.NAVY
    );

    private static final double PARTICLE_SPACING = 0.5;
    private static final int TICK_INTERVAL = 10;

    private static AreaVisualizationSystem instance;

    private final Map<UUID, Area> visualizingPlayers = new HashMap<>();
    private BukkitRunnable particleTask;

    public AreaVisualizationSystem() {
        instance = this;
        startParticleTask();
    }

    public static AreaVisualizationSystem getInstance() {
        return instance;
    }

    public void toggleVisualization(final Player player, final Area area) {
        final UUID uuid = player.getUniqueId();
        if (visualizingPlayers.containsKey(uuid) && visualizingPlayers.get(uuid) == area) {
            visualizingPlayers.remove(uuid);
        } else {
            visualizingPlayers.put(uuid, area);
        }
    }

    public boolean isVisualizing(final Player player, final Area area) {
        return visualizingPlayers.get(player.getUniqueId()) == area;
    }

    private void startParticleTask() {
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (final Map.Entry<UUID, Area> entry : new HashMap<>(visualizingPlayers).entrySet()) {
                    final Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null || !player.isOnline()) {
                        visualizingPlayers.remove(entry.getKey());
                        continue;
                    }
                    showParticles(player, entry.getValue());
                }
            }
        };
        particleTask.runTaskTimer(ZombiesPlugin.INSTANCE, 0, TICK_INTERVAL);
    }

    private void showParticles(final Player player, final Area area) {
        final World world = player.getWorld();
        int colorIndex = 0;

        for (final BlockArea bound : area.bounds) {
            if (bound == null) continue;
            final Color color = COLORS.get(colorIndex % COLORS.size());
            drawBoundEdges(player, world, bound, color);
            colorIndex++;
        }
    }

    private void drawBoundEdges(final Player player, final World world, final BlockArea bound, final Color color) {
        final Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);

        final double minX = bound.getLowerX();
        final double minY = bound.getLowerY();
        final double minZ = bound.getLowerZ();
        final double maxX = bound.getUpperX() + 1;
        final double maxY = bound.getUpperY() + 1;
        final double maxZ = bound.getUpperZ() + 1;

        // 8 vertices of the box
        final double[][] vertices = {
                {minX, minY, minZ}, {maxX, minY, minZ},
                {minX, maxY, minZ}, {maxX, maxY, minZ},
                {minX, minY, maxZ}, {maxX, minY, maxZ},
                {minX, maxY, maxZ}, {maxX, maxY, maxZ}
        };

        // 12 edges connecting the vertices
        final int[][] edges = {
                {0, 1}, {2, 3}, {4, 5}, {6, 7}, // X-axis edges
                {0, 2}, {1, 3}, {4, 6}, {5, 7}, // Y-axis edges
                {0, 4}, {1, 5}, {2, 6}, {3, 7}  // Z-axis edges
        };

        for (final int[] edge : edges) {
            drawLine(player, world, vertices[edge[0]], vertices[edge[1]], dustOptions);
        }
    }

    private void drawLine(final Player player, final World world, final double[] from, final double[] to,
                          final Particle.DustOptions dustOptions) {
        final double dx = to[0] - from[0];
        final double dy = to[1] - from[1];
        final double dz = to[2] - from[2];
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length == 0) return;

        final int steps = (int) Math.ceil(length / PARTICLE_SPACING);
        final double stepX = dx / steps;
        final double stepY = dy / steps;
        final double stepZ = dz / steps;

        for (int i = 0; i <= steps; i++) {
            final double x = from[0] + stepX * i;
            final double y = from[1] + stepY * i;
            final double z = from[2] + stepZ * i;

            // Only show particles within render distance
            if (player.getLocation().distanceSquared(
                    new org.bukkit.Location(world, x, y, z)) < 100 * 100) {
                player.spawnParticle(Particle.DUST, x, y, z, 1, dustOptions);
            }
        }
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        visualizingPlayers.remove(event.getPlayer().getUniqueId());
    }
}
