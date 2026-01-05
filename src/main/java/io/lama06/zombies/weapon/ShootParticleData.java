package io.lama06.zombies.weapon;

import org.bukkit.Particle;

public record ShootParticleData(
    Particle particle,
    int count,
    double spacing,
    Object particleData
) {
    public ShootParticleData(final Particle particle) {
        this(particle, 15, 1, null);
    }

    public ShootParticleData(final Particle particle, final Object particleData) {
        this(particle, 15, 1, particleData);
    }
}
