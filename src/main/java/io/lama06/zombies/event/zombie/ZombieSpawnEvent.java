package io.lama06.zombies.event.zombie;

import io.lama06.zombies.util.HandlerListGetter;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieData;
import org.bukkit.event.HandlerList;

public final class ZombieSpawnEvent extends ZombieEvent {
    public static final HandlerList HANDLERS = new HandlerList();

    @HandlerListGetter
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final ZombieData data;
    private final int healthOverride;  // 0 = 使用data.health

    public ZombieSpawnEvent(final Zombie zombie, final ZombieData data) {
        this(zombie, data, 0);
    }

    public ZombieSpawnEvent(final Zombie zombie, final ZombieData data, final int healthOverride) {
        super(zombie);
        this.data = data;
        this.healthOverride = healthOverride;
    }

    public ZombieData getData() {
        return data;
    }

    public int getHealthOverride() {
        return healthOverride;
    }

    public int getEffectiveHealth() {
        return healthOverride > 0 ? healthOverride : data.health;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
