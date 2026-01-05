package io.lama06.zombies;

import io.lama06.zombies.zombie.ZombieType;

import java.util.HashMap;
import java.util.Map;

public final class Wave {
    public int delayTicks;
    public Map<ZombieType, Integer> zombies = new HashMap<>();
    public Map<ZombieType, Integer> zombieHealth = new HashMap<>();  // 每种僵尸的血量覆盖, 0或不存在 = 使用僵尸类型默认血量
    public Map<ZombieType, Double> zombieSpeed = new HashMap<>();    // 每种僵尸的速度覆盖, 0或不存在 = 使用默认速度
    public Map<ZombieType, Double> zombieDamage = new HashMap<>();   // 每种僵尸的攻击力覆盖, 0或不存在 = 使用默认攻击力

    public int getTotalZombies() {
        return zombies.values().stream().mapToInt(i -> i).sum();
    }

    public int getHealthForType(final ZombieType type) {
        return zombieHealth.getOrDefault(type, 0);
    }

    public double getSpeedForType(final ZombieType type) {
        return zombieSpeed.getOrDefault(type, 0.0);
    }

    public double getDamageForType(final ZombieType type) {
        return zombieDamage.getOrDefault(type, 0.0);
    }
}
