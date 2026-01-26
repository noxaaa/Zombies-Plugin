package io.lama06.zombies.system.skill;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.data.Component;
import io.lama06.zombies.event.skill.SkillCooldownChangeEvent;
import io.lama06.zombies.skill.Skill;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class TickSkillCooldownSystem implements Listener {
    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final Skill skill : ZombiesPlugin.INSTANCE.getSkills()) {
            final Component cooldownComponent = skill.getComponent(Skill.COOLDOWN);
            if (cooldownComponent == null) {
                continue;
            }
            final Integer remaining = cooldownComponent.get(Skill.REMAINING_COOLDOWN);
            if (remaining == null || remaining <= 0) {
                continue;
            }

            final int newRemaining = remaining - 1;
            cooldownComponent.set(Skill.REMAINING_COOLDOWN, newRemaining);

            Bukkit.getPluginManager().callEvent(new SkillCooldownChangeEvent(skill, remaining, newRemaining));
        }
    }
}
