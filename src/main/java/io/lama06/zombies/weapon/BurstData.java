package io.lama06.zombies.weapon;

/**
 * 连发数据
 * @param count 每次点击发射的子弹数
 * @param interval 连发间隔（tick）
 */
public record BurstData(int count, int interval) {
}
