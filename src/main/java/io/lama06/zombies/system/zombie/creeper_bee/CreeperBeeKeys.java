package io.lama06.zombies.system.zombie.creeper_bee;

import io.lama06.zombies.ZombiesPlugin;
import org.bukkit.NamespacedKey;

public final class CreeperBeeKeys {
    private CreeperBeeKeys() { }

    public static NamespacedKey partnerKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "creeper_bee_partner");
    }

    public static NamespacedKey explodedKey() {
        return new NamespacedKey(ZombiesPlugin.INSTANCE, "creeper_bee_exploded");
    }
}
