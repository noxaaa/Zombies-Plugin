package io.lama06.zombies.system.perk.global;

import io.lama06.zombies.Window;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesWorld;
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

import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class SpawnPerkItemsOnZombieDeathSystem implements Listener {
    private static final int PERK_ITEM_TIME = 60 * 20;  // 60秒
    private static final double PERK_PROBABILITY = 0.05;

    @EventHandler
    private void onEntityDeath(final EntityDeathEvent event) {
        final Zombie zombie = new Zombie(event.getEntity());
        if (!zombie.isZombie()) {
            return;
        }
        final RandomGenerator rnd = ThreadLocalRandom.current();
        if (rnd.nextDouble() >= PERK_PROBABILITY) {
            return;
        }
        final GlobalPerk[] perks = GlobalPerk.values();
        final GlobalPerk perk = perks[rnd.nextInt(perks.length)];
        final World world = event.getEntity().getWorld();

        // Calculate spawn location and adjust if inside a window
        Location spawnLocation = zombie.getEntity().getLocation().clone().add(0, 1, 0);
        spawnLocation = adjustLocationIfInsideWindow(world, spawnLocation);

        final ItemDisplay display = world.spawn(spawnLocation, ItemDisplay.class);
        display.setCustomNameVisible(true);
        display.customName(perk.getDisplayName());
        display.setItemStack(new ItemStack(perk.getMaterial()));
        final PersistentDataContainer pdc = display.getPersistentDataContainer();
        pdc.set(PerkItem.getPerkNameKey(), PersistentDataType.STRING, perk.name());
        pdc.set(PerkItem.getRemainingTimeKey(), PersistentDataType.INTEGER, PERK_ITEM_TIME);
    }

    private Location adjustLocationIfInsideWindow(final World world, final Location location) {
        final ZombiesWorld zombiesWorld = new ZombiesWorld(world);
        final WorldConfig config = zombiesWorld.getConfig();
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
                // Inside a window, move to repairArea center
                if (window.repairArea != null) {
                    final double centerX = (window.repairArea.getLowerX() + window.repairArea.getUpperX()) / 2.0 + 0.5;
                    final double centerY = window.repairArea.getUpperY() + 1.5;
                    final double centerZ = (window.repairArea.getLowerZ() + window.repairArea.getUpperZ()) / 2.0 + 0.5;
                    return new Location(world, centerX, centerY, centerZ);
                }
            }
        }

        return location;
    }
}
