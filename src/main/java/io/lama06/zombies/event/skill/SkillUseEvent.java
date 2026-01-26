package io.lama06.zombies.event.skill;

import io.lama06.zombies.skill.Skill;
import io.lama06.zombies.util.HandlerListGetter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class SkillUseEvent extends SkillEvent implements Cancellable {
    public static final HandlerList HANDLERS = new HandlerList();

    private boolean cancel;

    public SkillUseEvent(final Skill skill) {
        super(skill);
    }

    @HandlerListGetter
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
