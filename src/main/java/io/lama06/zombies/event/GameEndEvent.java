package io.lama06.zombies.event;

import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.util.HandlerListGetter;
import org.bukkit.event.HandlerList;

public final class GameEndEvent extends ZombiesEvent {
    public static final HandlerList HANDLERS = new HandlerList();

    @HandlerListGetter
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final boolean victory;

    public GameEndEvent(final ZombiesWorld world) {
        this(world, false);
    }

    public GameEndEvent(final ZombiesWorld world, final boolean victory) {
        super(world);
        this.victory = victory;
    }

    public boolean isVictory() {
        return victory;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
