package io.lama06.zombies.event.weapon;

import io.lama06.zombies.weapon.Weapon;
import org.bukkit.event.Cancellable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

public final class WeaponShootEvent extends WeaponUseEvent implements Cancellable {
    private final List<Bullet> bullets;
    private final boolean skipDelay;  // 连发时中间的子弹不触发延迟

    public WeaponShootEvent(final Weapon weapon, final List<Bullet> bullets) {
        this(weapon, bullets, false);
    }

    public WeaponShootEvent(final Weapon weapon, final List<Bullet> bullets, final boolean skipDelay) {
        super(weapon);
        this.bullets = bullets;
        this.skipDelay = skipDelay;
    }

    public List<Bullet> getBullets() {
        return Collections.unmodifiableList(bullets);
    }

    public boolean isSkipDelay() {
        return skipDelay;
    }

    public record Bullet(Vector direction) {
    }
}
