package io.lama06.zombies.system.skill;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.event.skill.SkillUseEvent;
import io.lama06.zombies.skill.Skill;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class UseSkillSystem implements Listener {
    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        final ZombiesPlayer player = new ZombiesPlayer(event.getPlayer());
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }
        if (!player.isAlive()) {
            return;
        }

        final int heldSlot = player.getBukkit().getInventory().getHeldItemSlot();
        if (heldSlot != Skill.SLOT) {
            return;
        }

        final Skill skill = player.getSkill();
        if (skill == null) {
            return;
        }

        event.setCancelled(true);

        final SkillUseEvent skillUseEvent = new SkillUseEvent(skill);
        Bukkit.getPluginManager().callEvent(skillUseEvent);
    }
}
