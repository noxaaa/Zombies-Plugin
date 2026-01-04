package io.lama06.zombies;

import io.lama06.zombies.zombie.ZombieType;

import java.util.HashMap;
import java.util.Map;

public final class Wave {
    public int delayTicks;
    public Map<ZombieType, Integer> zombies = new HashMap<>();

    public int getTotalZombies() {
        return zombies.values().stream().mapToInt(i -> i).sum();
    }
}
