package io.lama06.zombies.weapon;

public record AttackData(
        double damage,
        boolean fire,
        int gold,
        double headshotBonusDamage,  // 爆头额外伤害
        int headshotBonusGold        // 爆头额外金币
) {
    // 兼容旧构造函数
    public AttackData(double damage, boolean fire, int gold) {
        this(damage, fire, gold, 0, 0);
    }
}
