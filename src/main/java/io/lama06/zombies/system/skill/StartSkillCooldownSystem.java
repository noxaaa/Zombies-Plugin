package io.lama06.zombies.system.skill;

import io.lama06.zombies.data.Component;
import io.lama06.zombies.event.skill.SkillCooldownChangeEvent;
import io.lama06.zombies.event.skill.SkillUseEvent;
import io.lama06.zombies.skill.Skill;
import io.lama06.zombies.skill.SkillData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class StartSkillCooldownSystem implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSkillUse(final SkillUseEvent event) {
        final Skill skill = event.getSkill();
        final SkillData data = skill.getData();
        if (data == null || data.cooldownTicks <= 0) {
            return;
        }

        final int oldCooldown = skill.getRemainingCooldown();
        final int newCooldown = data.cooldownTicks;

        Component cooldownComponent = skill.getComponent(Skill.COOLDOWN);
        if (cooldownComponent == null) {
            cooldownComponent = skill.addComponent(Skill.COOLDOWN);
        }
        cooldownComponent.set(Skill.REMAINING_COOLDOWN, newCooldown);

        Bukkit.getPluginManager().callEvent(new SkillCooldownChangeEvent(skill, oldCooldown, newCooldown));
    }
}
