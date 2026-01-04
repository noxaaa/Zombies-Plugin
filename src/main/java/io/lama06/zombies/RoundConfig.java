package io.lama06.zombies;

import java.util.ArrayList;
import java.util.List;

public final class RoundConfig {
    public List<Wave> waves = new ArrayList<>();

    public int getTotalZombies() {
        return waves.stream().mapToInt(Wave::getTotalZombies).sum();
    }
}
