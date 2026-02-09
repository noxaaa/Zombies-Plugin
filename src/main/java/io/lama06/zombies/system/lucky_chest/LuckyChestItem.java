package io.lama06.zombies.system.lucky_chest;

import io.lama06.zombies.ZombiesPlugin;
import org.bukkit.NamespacedKey;

public final class LuckyChestItem {
    public static final int TOTAL_SHUFFLES = 20;
    static final double START_INTERVAL = 0.3;  // 秒
    static final double END_INTERVAL = 0.75;   // 秒
    static final float[] PITCH_CYCLE = {0.65f, 1.0f, 1.5f, 1.0f};
    public static final int CLAIM_TIMEOUT_TICKS = 10 * 20;  // 10秒

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

    public static NamespacedKey getRemainingTimeKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_remaining_time");
    }

    public static NamespacedKey getShuffleCountKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_shuffle_count");
    }

    static NamespacedKey getNextShuffleTimeKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_next_shuffle_time");
    }

    public static NamespacedKey getWeaponKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_weapon");
    }

    public static NamespacedKey getSkillKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_skill");
    }

    public static NamespacedKey getOffhandKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_offhand");
    }

    public static NamespacedKey getItemTypeKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_item_type");
    }

    // 开箱玩家的 UUID
    public static NamespacedKey getOwnerUuidKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_owner_uuid");
    }

    // 开箱玩家的名字（用于显示）
    public static NamespacedKey getOwnerNameKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_owner_name");
    }

    // 领取倒计时（ticks）
    public static NamespacedKey getClaimTimeoutKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_claim_timeout");
    }

    // Lucky Chest 在配置中的索引
    public static NamespacedKey getChestIndexKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "lucky_chest_index");
    }
}
