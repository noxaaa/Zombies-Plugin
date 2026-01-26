package io.lama06.zombies.system.skill;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.event.skill.SkillUseEvent;
import io.lama06.zombies.skill.Skill;
import io.lama06.zombies.skill.SkillType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PerformHealSkillSystem implements Listener {
    private static final double SELF_HEAL_AMOUNT = 16.0;
    private static final double NEARBY_HEAL_AMOUNT = 8.0;
    private static final double NEARBY_RANGE = 5.0;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onSkillUse(final SkillUseEvent event) {
        final Skill skill = event.getSkill();
        if (skill.getType() != SkillType.HEAL) {
            return;
        }

        final ZombiesPlayer zombiesPlayer = skill.getPlayer();
        final Player player = zombiesPlayer.getBukkit();

        // Heal self (up to 16 HP)
        final double currentHealth = player.getHealth();
        final double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        final double newHealth = Math.min(currentHealth + SELF_HEAL_AMOUNT, maxHealth);
        player.setHealth(newHealth);

        // Heal nearby players (up to 8 HP)
        for (final Player nearby : player.getWorld().getPlayers()) {
            if (nearby.equals(player)) {
                continue;
            }
            if (nearby.getLocation().distance(player.getLocation()) > NEARBY_RANGE) {
                continue;
            }
            final ZombiesPlayer nearbyZombiesPlayer = new ZombiesPlayer(nearby);
            if (!nearbyZombiesPlayer.isAlive()) {
                continue;
            }
            if (!nearbyZombiesPlayer.getWorld().isGameRunning()) {
                continue;
            }

            final double nearbyHealth = nearby.getHealth();
            final double nearbyMaxHealth = nearby.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            final double nearbyNewHealth = Math.min(nearbyHealth + NEARBY_HEAL_AMOUNT, nearbyMaxHealth);
            nearby.setHealth(nearbyNewHealth);

            // Show particles at nearby player
            nearby.getWorld().spawnParticle(
                    Particle.HEART,
                    nearby.getLocation().add(0, 1, 0),
                    5,
                    0.5, 0.5, 0.5,
                    0
            );
        }

        // Play sound and show particles at caster
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.getWorld().spawnParticle(
                Particle.HEART,
                player.getLocation().add(0, 1, 0),
                10,
                0.5, 0.5, 0.5,
                0
        );
    }
}
