package io.lama06.zombies;

import io.lama06.zombies.zombie.ZombieType;

import java.util.HashMap;
import java.util.Map;

public final class Wave {
    public int delayTicks;
    public Map<ZombieType, Integer> zombies = new HashMap<>();
    public Map<ZombieType, Integer> zombieHealth = new HashMap<>();  // 每种僵尸的血量覆盖, 0或不存在 = 使用僵尸类型默认血量

    public int getTotalZombies() {
        return zombies.values().stream().mapToInt(i -> i).sum();
    }

    public int getHealthForType(final ZombieType type) {
        return zombieHealth.getOrDefault(type, 0);
    }
}
