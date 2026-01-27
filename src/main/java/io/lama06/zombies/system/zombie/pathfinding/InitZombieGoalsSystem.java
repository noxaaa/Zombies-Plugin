package io.lama06.zombies.system.zombie.pathfinding;

import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieData;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
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

        final var nmsMob = ((CraftMob) bukkitMob).getHandle();
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

        // 1. Window breaking (highest priority - must break window before doing anything else)
        if (data.breakWindow != null) {
            nmsMob.goalSelector.addGoal(1, new ZombieBreakWindowGoal(
                nmsMob, zombie, data.breakWindow.maxDistance() + 10
            ));
        }

        // 2. Melee attack (only activates after window is passable)
        if (nmsMob instanceof final PathfinderMob pathfinderMob) {
            nmsMob.goalSelector.addGoal(2, new MeleeAttackGoal(pathfinderMob, 1.0, false));
        }

        // 3. Chase player (uses flow field for complex navigation, unlimited range)
        nmsMob.goalSelector.addGoal(3, new ZombieChasePlayerGoal(nmsMob, zombie, 1.0));

        // Target selector: target nearest player
        nmsMob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nmsMob, Player.class, true));
    }
}
