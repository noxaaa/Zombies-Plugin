package io.lama06.zombies.system.skill;

import io.lama06.zombies.event.skill.SkillUseEvent;
import io.lama06.zombies.skill.Skill;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PreventSkillUseDuringCooldownSystem implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    private void onSkillUse(final SkillUseEvent event) {
        final Skill skill = event.getSkill();
        if (!skill.isOnCooldown()) {
            return;
        }
        event.setCancelled(true);
        final int remainingSeconds = (int) Math.ceil(skill.getRemainingCooldown() / 20.0);
        skill.getPlayer().sendMessage(
                Component.text("Skill on cooldown: " + remainingSeconds + "s remaining").color(NamedTextColor.RED)
        );
    }
}
