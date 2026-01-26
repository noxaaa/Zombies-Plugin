package io.lama06.zombies.event.skill;

import io.lama06.zombies.skill.Skill;
import io.lama06.zombies.util.HandlerListGetter;
import org.bukkit.event.HandlerList;

public final class SkillCooldownChangeEvent extends SkillEvent {
    public static final HandlerList HANDLERS = new HandlerList();

    private final int oldCooldown;
    private final int newCooldown;

    public SkillCooldownChangeEvent(final Skill skill, final int oldCooldown, final int newCooldown) {
        super(skill);
        this.oldCooldown = oldCooldown;
        this.newCooldown = newCooldown;
    }

    public int getOldCooldown() {
        return oldCooldown;
    }

    public int getNewCooldown() {
        return newCooldown;
    }

    @HandlerListGetter
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
