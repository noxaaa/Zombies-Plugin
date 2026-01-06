package io.lama06.zombies.system.perk.global;

import io.lama06.zombies.ZombiesPlugin;
import org.bukkit.NamespacedKey;

public final class PerkItem {
    public static NamespacedKey getRemainingTimeKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "perk_remaining_time");
    }

    public static NamespacedKey getPerkNameKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "perk_name");
    }
}
