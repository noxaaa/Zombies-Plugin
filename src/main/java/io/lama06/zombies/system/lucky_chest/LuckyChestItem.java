package io.lama06.zombies.system.lucky_chest;

import io.lama06.zombies.ZombiesPlugin;
import org.bukkit.NamespacedKey;

final class LuckyChestItem {
    static final int TOTAL_SHUFFLES = 20;
    static final double START_INTERVAL = 0.3;  // 秒
    static final double END_INTERVAL = 0.75;   // 秒
    static final float[] PITCH_CYCLE = {1.0f, 1.4f, 1.7f, 1.4f};

    // 计算第 n 次切换的间隔（秒），n 从 1 开始
    static double getIntervalForShuffle(int n) {
        if (n < 1) n = 1;
        if (n > TOTAL_SHUFFLES - 1) n = TOTAL_SHUFFLES - 1;
        return START_INTERVAL + (n - 1) * (END_INTERVAL - START_INTERVAL) / (TOTAL_SHUFFLES - 2);
    }

    // 计算第 n 次切换的间隔（ticks）
    static int getIntervalTicksForShuffle(int n) {
        return (int) Math.round(getIntervalForShuffle(n) * 20);
    }

    // 获取第 n 次切换的 pitch（n 从 1 开始）
    static float getPitchForShuffle(int n) {
        return PITCH_CYCLE[(n - 1) % PITCH_CYCLE.length];
    }

    static NamespacedKey getRemainingTimeKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_remaining_time");
    }

    static NamespacedKey getShuffleCountKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_shuffle_count");
    }

    static NamespacedKey getNextShuffleTimeKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_next_shuffle_time");
    }

    static NamespacedKey getWeaponKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_weapon");
    }
}
