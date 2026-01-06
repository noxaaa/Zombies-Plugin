package io.lama06.zombies.zombie;

import io.lama06.zombies.menu.MenuDisplayableEnum;
import io.lama06.zombies.util.PlayerHeads;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public enum ZombieType implements MenuDisplayableEnum {
    NORMAL_EASY(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setBreakWindow(new BreakWindowData(40))
                    .setHealth(20)
                    .setKnockbackResistance(0.8)
    ),
    NORMAL_MEDIUM(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .addEquipment(EquipmentSlot.CHEST, new ItemStack(Material.LEATHER_CHESTPLATE))
                    .addEquipment(EquipmentSlot.FEET, new ItemStack(Material.LEATHER_BOOTS))
                    .setBreakWindow(new BreakWindowData(30))
                    .setHealth(20)
                    .setKnockbackResistance(0.2)
    ),
    NORMAL_HARD(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .addEquipment(EquipmentSlot.CHEST, new ItemStack(Material.IRON_CHESTPLATE))
                    .addEquipment(EquipmentSlot.LEGS, new ItemStack(Material.IRON_LEGGINGS))
                    .addEquipment(EquipmentSlot.HAND, new ItemStack(Material.DIAMOND_AXE))
                    .setBreakWindow(new BreakWindowData(20))
                    .setHealth(20)
                    .setKnockbackResistance(0.4)
    ),
    PIG_ZOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIFIED_PIGLIN)
                    .setHealth(20)
                    .setBreakWindow(new BreakWindowData(20))
                    .setFireImmune(true)
                    .setKnockbackResistance(0.3)
    ),
    MAGMA_CUBE(
            new ZombieData()
                    .setEntity(EntityType.MAGMA_CUBE)
                    .setHealth(3)
                    .setFireImmune(true)
                    .setInitializer(entity -> ((MagmaCube) entity).setSize(2))
                    .setKnockbackResistance(0.0)
    ),
    MAGMA_ZOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setBreakWindow(new BreakWindowData(20))
                    .setHealth(20)
                    .setFireImmune(true)
                    .setDescendants(new DescendantsData(ZombieType.MAGMA_CUBE, 3))
                    .addEquipment(EquipmentSlot.HAND, new ItemStack(Material.GOLDEN_SWORD))
                    .addEquipment(EquipmentSlot.HEAD, PlayerHeads.MAGMA_CUBE.createItem())
                    .addEquipment(EquipmentSlot.CHEST, new ItemStack(Material.GOLDEN_CHESTPLATE))
                    .addEquipment(EquipmentSlot.LEGS, new ItemStack(Material.GOLDEN_LEGGINGS))
                    .addEquipment(EquipmentSlot.FEET, new ItemStack(Material.GOLDEN_BOOTS))
                    .setKnockbackResistance(0.35)
    ),
    LITTLE_BOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setBreakWindow(new BreakWindowData(20))
                    .setHealth(10)
                    .setExplosionAttack(ExplosionAttackData.explodeOnDeath(4))
                    .addEquipment(EquipmentSlot.HEAD, new ItemStack(Material.TNT))
                    .setInitializer(entity -> ((Zombie) entity).setBaby())
                    .setKnockbackResistance(0.0)
    ),
    FIRE_ZOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setHealth(20)
                    .setBreakWindow(new BreakWindowData(20))
                    .setFireImmune(true)
                    .setFireAttack(new FireAttackData(3*20))
                    .addEquipment(EquipmentSlot.HAND, new ItemStack(Material.BLAZE_ROD))
                    .setKnockbackResistance(0.25)
    ),
    ZOMBIE_WOLF(
            new ZombieData()
                    .setEntity(EntityType.WOLF)
                    .setHealth(10)
                    .setBreakWindow(new BreakWindowData(20))
                    .setInitializer(entity -> ((Wolf) entity).setAngry(true))
                    .setKnockbackResistance(0.0)
    ),
    GUARDIAN_ZOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setHealth(20)
                    .setBreakWindow(new BreakWindowData(2*20))
                    .addEquipment(EquipmentSlot.HEAD, new ItemStack(Material.SEA_LANTERN))
                    .setLaserAttack(new LaserAttackData(3))
                    .setKnockbackResistance(0.5)
    ),
    INFERNO(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setHealth(100)
                    .addEquipment(EquipmentSlot.HAND, new ItemStack(Material.BLAZE_ROD))
                    .setFireTrail(true)
                    .setBreakWindow(new BreakWindowData(20))
                    .setFireImmune(true)
                    .setFireBallAttack(new FireBallAttackData(4, 40))
                    .setFireAttack(new FireAttackData(40))
                    .setKnockbackResistance(0.8)
    ),
    BOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setHealth(100)
                    .setExplosionAttack(ExplosionAttackData.explodePeriodically(2*20 + 10, 4))
                    .setBreakWindow(new BreakWindowData(20))
                    .setKnockbackResistance(0.7)
    );

    public final ZombieData data;

    ZombieType(final ZombieData data) {
        this.data = data;
    }

    @Override
    public Component getDisplayName() {
        return Component.text(name());
    }

    @Override
    public Material getDisplayMaterial() {
        return switch (this) {
            case NORMAL_EASY, NORMAL_MEDIUM, NORMAL_HARD -> Material.ZOMBIE_HEAD;
            case PIG_ZOMBIE -> Material.PIGLIN_HEAD;
            case MAGMA_CUBE, MAGMA_ZOMBIE -> Material.MAGMA_CREAM;
            case LITTLE_BOMBIE, BOMBIE -> Material.TNT;
            case FIRE_ZOMBIE, INFERNO -> Material.BLAZE_ROD;
            case ZOMBIE_WOLF -> Material.BONE;
            case GUARDIAN_ZOMBIE -> Material.SEA_LANTERN;
        };
    }
}
