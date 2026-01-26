package io.lama06.zombies.system.skill;

import io.lama06.zombies.event.skill.SkillCooldownChangeEvent;
import io.lama06.zombies.skill.Skill;
import io.lama06.zombies.skill.SkillData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class RenderSkillCooldownSystem implements Listener {
    @EventHandler
    private void onCooldownChange(final SkillCooldownChangeEvent event) {
        final Skill skill = event.getSkill();
        final SkillData data = skill.getData();
        if (data == null) {
            return;
        }

        final int newCooldown = event.getNewCooldown();
        final ItemStack item = skill.getItem();
        if (item == null) {
            return;
        }

        if (newCooldown <= 0) {
            // Cooldown ended - restore original item
            item.setType(data.material);
            item.setAmount(1);
            final ItemMeta meta = item.getItemMeta();
            meta.displayName(data.displayName);
            item.setItemMeta(meta);

            // Play ready sound
            skill.getPlayer().getBukkit().playSound(
                    skill.getPlayer().getBukkit().getLocation(),
                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                    1.0f,
                    1.5f
            );
        } else {
            // Show cooldown - gray dye with seconds count
            final int remainingSeconds = (int) Math.ceil(newCooldown / 20.0);
            item.setType(Material.GRAY_DYE);
            item.setAmount(Math.min(remainingSeconds, 64));
            final ItemMeta meta = item.getItemMeta();
            meta.displayName(
                    Component.text()
                            .append(data.displayName)
                            .append(Component.text(" (Cooldown)").color(NamedTextColor.GRAY))
                            .build()
            );
            item.setItemMeta(meta);
        }
    }
}
