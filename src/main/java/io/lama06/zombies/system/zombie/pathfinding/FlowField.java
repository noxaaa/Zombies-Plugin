package io.lama06.zombies.system.zombie.pathfinding;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Flow field pathfinding - BFS from target to create a direction field.
 * Zombies follow the field to reach the target through any complex terrain.
 */
public final class FlowField {
    private static final int MAX_DISTANCE = 512;
    private static final BlockFace[] HORIZONTAL_FACES = {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final World world;
    private final Map<Long, DirectionEntry> field = new HashMap<>();
    private final Location target;
    private final long creationTime;

    public FlowField(final Location target) {
        this.world = target.getWorld();
        this.target = target.clone();
        this.creationTime = System.currentTimeMillis();
        calculate();
    }

    private void calculate() {
        final Queue<BlockPos> queue = new LinkedList<>();
        final Map<Long, Integer> distances = new HashMap<>();

        // Start from target position
        final int startX = target.getBlockX();
        final int startY = target.getBlockY();
        final int startZ = target.getBlockZ();

        final long startKey = packPos(startX, startY, startZ);
        distances.put(startKey, 0);
        queue.add(new BlockPos(startX, startY, startZ));

        while (!queue.isEmpty()) {
            final BlockPos current = queue.poll();
            final long currentKey = packPos(current.x, current.y, current.z);
            final int currentDist = distances.get(currentKey);

            if (currentDist >= MAX_DISTANCE) {
                continue;
            }

            // Try all neighbors
            for (final Neighbor neighbor : getWalkableNeighbors(current)) {
                final long neighborKey = packPos(neighbor.pos.x, neighbor.pos.y, neighbor.pos.z);

                if (!distances.containsKey(neighborKey)) {
                    distances.put(neighborKey, currentDist + 1);
                    // Store direction TO current (towards target)
                    field.put(neighborKey, new DirectionEntry(
                        current.x - neighbor.pos.x,
                        current.y - neighbor.pos.y,
                        current.z - neighbor.pos.z,
                        currentDist + 1
                    ));
                    queue.add(neighbor.pos);
                }
            }
        }
    }

    /**
     * Get the direction a zombie at the given location should move.
     * Returns null if no path exists.
     */
    public Vector getDirection(final Location from) {
        final int x = from.getBlockX();
        final int y = from.getBlockY();
        final int z = from.getBlockZ();

        // Check current block and one below (in case standing on edge)
        for (int dy = 0; dy >= -1; dy--) {
            final long key = packPos(x, y + dy, z);
            final DirectionEntry entry = field.get(key);
            if (entry != null) {
                return new Vector(entry.dx, entry.dy, entry.dz).normalize();
            }
        }
        return null;
    }

    /**
     * Get distance to target from a location. Returns -1 if not reachable.
     */
    public int getDistance(final Location from) {
        final int x = from.getBlockX();
        final int y = from.getBlockY();
        final int z = from.getBlockZ();

        // Check current block and one below (consistent with getDirection)
        for (int dy = 0; dy >= -1; dy--) {
            final long key = packPos(x, y + dy, z);
            final DirectionEntry entry = field.get(key);
            if (entry != null) {
                return entry.distance;
            }
        }
        return -1;
    }

    public Location getTarget() {
        return target.clone();
    }

    public long getAge() {
        return System.currentTimeMillis() - creationTime;
    }

    private Iterable<Neighbor> getWalkableNeighbors(final BlockPos pos) {
        final java.util.List<Neighbor> neighbors = new java.util.ArrayList<>();

        for (final BlockFace face : HORIZONTAL_FACES) {
            // Same level
            if (canWalkTo(pos, pos.x + face.getModX(), pos.y, pos.z + face.getModZ())) {
                neighbors.add(new Neighbor(
                    new BlockPos(pos.x + face.getModX(), pos.y, pos.z + face.getModZ()),
                    1
                ));
            }

            // Step up (stairs, slabs, 1-block jump)
            if (canWalkTo(pos, pos.x + face.getModX(), pos.y + 1, pos.z + face.getModZ())) {
                neighbors.add(new Neighbor(
                    new BlockPos(pos.x + face.getModX(), pos.y + 1, pos.z + face.getModZ()),
                    1
                ));
            }

            // Step down
            if (canStepDown(pos, pos.x + face.getModX(), pos.y - 1, pos.z + face.getModZ())) {
                neighbors.add(new Neighbor(
                    new BlockPos(pos.x + face.getModX(), pos.y - 1, pos.z + face.getModZ()),
                    1
                ));
            }
        }

        return neighbors;
    }

    private boolean canWalkTo(final BlockPos from, final int toX, final int toY, final int toZ) {
        final Block ground = world.getBlockAt(toX, toY - 1, toZ);
        final Block feet = world.getBlockAt(toX, toY, toZ);
        final Block head = world.getBlockAt(toX, toY + 1, toZ);

        // Need solid ground, passable feet and head
        return isSolid(ground) && isPassable(feet) && isPassable(head);
    }

    private boolean canStepDown(final BlockPos from, final int toX, final int toY, final int toZ) {
        final Block ground = world.getBlockAt(toX, toY - 1, toZ);
        final Block feet = world.getBlockAt(toX, toY, toZ);
        final Block head = world.getBlockAt(toX, toY + 1, toZ);
        final Block aboveHead = world.getBlockAt(toX, toY + 2, toZ);

        // Need solid ground at lower level, and space to move
        return isSolid(ground) && isPassable(feet) && isPassable(head) && isPassable(aboveHead);
    }

    private boolean isSolid(final Block block) {
        final Material type = block.getType();
        return type.isSolid() || type == Material.WATER;
    }

    private boolean isPassable(final Block block) {
        final Material type = block.getType();
        if (!type.isSolid()) {
            return true;
        }
        // Check for non-full blocks that entities can walk through/on
        final String name = type.name();
        // Slabs, stairs, and other partial blocks are passable at feet level
        if (name.endsWith("_SLAB") || name.endsWith("_STAIRS")) {
            return true;
        }
        return isPassableBlock(type);
    }

    private boolean isPassableBlock(final Material type) {
        return type == Material.OAK_DOOR || type == Material.IRON_DOOR ||
               type == Material.SPRUCE_DOOR || type == Material.BIRCH_DOOR ||
               type == Material.JUNGLE_DOOR || type == Material.ACACIA_DOOR ||
               type == Material.DARK_OAK_DOOR || type == Material.CRIMSON_DOOR ||
               type == Material.WARPED_DOOR || type == Material.OAK_FENCE_GATE ||
               type == Material.SPRUCE_FENCE_GATE || type == Material.BIRCH_FENCE_GATE ||
               type == Material.JUNGLE_FENCE_GATE || type == Material.ACACIA_FENCE_GATE ||
               type == Material.DARK_OAK_FENCE_GATE || type == Material.CRIMSON_FENCE_GATE ||
               type == Material.WARPED_FENCE_GATE;
    }

    private static long packPos(final int x, final int y, final int z) {
        return ((long) x & 0x3FFFFFF) | (((long) z & 0x3FFFFFF) << 26) | (((long) y & 0xFFF) << 52);
    }

    private record BlockPos(int x, int y, int z) {}
    private record Neighbor(BlockPos pos, int cost) {}
    private record DirectionEntry(int dx, int dy, int dz, int distance) {}
}
