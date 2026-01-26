package io.lama06.zombies.system.zombie.pathfinding;

import io.lama06.zombies.Window;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.data.Component;
import io.lama06.zombies.zombie.BreakWindowData;
import io.lama06.zombies.zombie.Zombie;
import io.papermc.paper.math.BlockPosition;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumSet;

public final class ZombieBreakWindowGoal extends Goal {
    private final Mob mob;
    private final Zombie zombie;
    private final double maxWindowDistance;
    private BlockPosition targetWindow;
    private boolean isBreaking;

    public ZombieBreakWindowGoal(final Mob mob, final Zombie zombie, final double maxDistance) {
        this.mob = mob;
        this.zombie = zombie;
        this.maxWindowDistance = maxDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (zombie.getData().breakWindow == null) {
            return false;
        }

        final Component comp = zombie.getComponent(Zombie.BREAK_WINDOW);
        if (comp != null && comp.get(BreakWindowData.REMAINING_TIME) != null) {
            isBreaking = true;
            return true;
        }

        targetWindow = findNearestWindow();
        return targetWindow != null;
    }

    @Override
    public void start() {
        if (!isBreaking && targetWindow != null) {
            navigateToWindow();
        }
    }

    @Override
    public void tick() {
        final Component comp = zombie.getComponent(Zombie.BREAK_WINDOW);
        if (comp != null && comp.get(BreakWindowData.REMAINING_TIME) != null) {
            mob.getNavigation().stop();
            isBreaking = true;
            if (targetWindow != null) {
                mob.getLookControl().setLookAt(
                    targetWindow.blockX() + 0.5,
                    targetWindow.blockY() + 0.5,
                    targetWindow.blockZ() + 0.5
                );
            }
            return;
        }

        isBreaking = false;

        if (targetWindow != null && mob.getNavigation().isDone()) {
            navigateToWindow();
        }
    }

    private void navigateToWindow() {
        if (targetWindow == null) {
            return;
        }
        mob.getNavigation().moveTo(
            targetWindow.blockX() + 0.5,
            targetWindow.blockY(),
            targetWindow.blockZ() + 0.5,
            1.0
        );
    }

    private BlockPosition findNearestWindow() {
        final ZombiesWorld world = zombie.getWorld();
        final Location zombieLoc = zombie.getEntity().getLocation();

        BlockPosition nearest = null;
        double minDist = Double.MAX_VALUE;

        for (final Window window : world.getConfig().windows) {
            for (final BlockPosition pos : window.blocks.getBlocks()) {
                final Block block = pos.toLocation(world.getBukkit()).getBlock();
                if (block.getType() == Material.AIR) {
                    continue;
                }

                final double dist = pos.toCenter().toVector().distance(zombieLoc.toVector());
                if (dist < minDist && dist <= maxWindowDistance) {
                    minDist = dist;
                    nearest = pos;
                }
            }
        }
        return nearest;
    }

    @Override
    public boolean canContinueToUse() {
        if (isBreaking) {
            return true;
        }
        if (targetWindow == null) {
            return false;
        }

        final Block block = targetWindow.toLocation(zombie.getWorld().getBukkit()).getBlock();
        return block.getType() != Material.AIR;
    }

    @Override
    public void stop() {
        targetWindow = null;
        isBreaking = false;
    }
}
