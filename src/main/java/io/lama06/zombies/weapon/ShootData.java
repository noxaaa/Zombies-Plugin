package io.lama06.zombies.weapon;

public record ShootData(int bullets, double precision, int piercing) {
    public ShootData(int bullets, double precision) {
        this(bullets, precision, 1);
    }
}
