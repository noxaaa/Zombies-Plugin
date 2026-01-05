package io.lama06.zombies.weapon;

import org.bukkit.Sound;

public record ShootSoundData(Sound sound, float volume, float pitch) {
    public ShootSoundData(final Sound sound, final float pitch) {
        this(sound, 1.0f, pitch);
    }

    public ShootSoundData(final Sound sound) {
        this(sound, 1.0f, 1.0f);
    }
}
