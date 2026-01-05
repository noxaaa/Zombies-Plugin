package io.lama06.zombies.weapon;

/**
 * Spread damage data for weapons that deal area damage.
 * @param radius The radius in blocks to search for additional targets
 * @param maxTargets Maximum number of additional targets (not including the primary target)
 */
public record SpreadDamageData(
        double radius,
        int maxTargets
) {
}
