package io.lama06.zombies.system.zombie.pathfinding;

import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class InitZombieGoalsSystem implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    private void onZombieSpawn(final ZombieSpawnEvent event) {
        final Zombie zombie = event.getZombie();
        if (!(zombie.getEntity() instanceof final org.bukkit.entity.Mob bukkitMob)) {
            return;
        }

        final Mob nmsMob = ((CraftMob) bukkitMob).getHandle();
        final ZombieData data = zombie.getData();

        // Clear all vanilla goals
        nmsMob.goalSelector.removeAllGoals(goal -> true);
        nmsMob.targetSelector.removeAllGoals(goal -> true);

        // Set special anger states for certain entity types
        if (zombie.getEntity() instanceof final PigZombie pigZombie) {
            pigZombie.setAngry(true);
        } else if (zombie.getEntity() instanceof final Wolf wolf) {
            wolf.setAngry(true);
            wolf.setCollarColor(DyeColor.RED);
        }

        // 0. Float in water (basic behavior)
        nmsMob.goalSelector.addGoal(0, new FloatGoal(nmsMob));

        // 1. Window breaking (if zombie is configured with breakWindow)
        if (data.breakWindow != null) {
            nmsMob.goalSelector.addGoal(1, new ZombieBreakWindowGoal(
                nmsMob, zombie, data.breakWindow.maxDistance() + 10
            ));
        }

        // 2. Chase player (uses flow field for complex navigation)
        nmsMob.goalSelector.addGoal(2, new ZombieChasePlayerGoal(nmsMob, zombie, 1.0, 48.0));

        // 3. Ranged attack (reserved for future, based on ZombieData configuration)
        // if (data.rangedAttack != null) {
        //     nmsMob.goalSelector.addGoal(3, new ZombieRangedAttackGoal(...));
        // }
    }
}
