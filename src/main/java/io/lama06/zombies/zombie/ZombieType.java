package io.lama06.zombies.zombie;

import io.lama06.zombies.menu.MenuDisplayableEnum;
import io.lama06.zombies.util.PlayerHeads;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public enum ZombieType implements MenuDisplayableEnum {
    SKELETON(
            new ZombieData()
                    .setEntity(EntityType.SKELETON)
                    .addEquipment(EquipmentSlot.HEAD, createCustomPlayerHead(
                            createUuidFromIntArray(1781922283, -1565044638, -1359055109, -183965397),
                            "https://textures.minecraft.net/texture/e4af7437af155c37527f349935c4f616237b3f1e69312543c6f310a0047d9210",
                            "https://namemc.com/skin/1cb2369efd1a2412"
                    ))
                    .addEquipment(EquipmentSlot.CHEST, createColoredLeatherArmor(Material.LEATHER_CHESTPLATE, Color.BLACK))
                    .addEquipment(EquipmentSlot.LEGS, createColoredLeatherArmor(Material.LEATHER_LEGGINGS, Color.BLACK))
                    .addEquipment(EquipmentSlot.FEET, createColoredLeatherArmor(Material.LEATHER_BOOTS, Color.BLACK))
                    .addEquipment(EquipmentSlot.HAND, new ItemStack(Material.BOW))
                    .setBreakWindow(new BreakWindowData(20))
                    .setHealth(20)
                    .setDefense(6)
    ),
    NORMAL_EASY(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .addEquipment(EquipmentSlot.CHEST, createColoredLeatherArmor(Material.LEATHER_CHESTPLATE, Color.GREEN))
                    .addEquipment(EquipmentSlot.LEGS, createColoredLeatherArmor(Material.LEATHER_LEGGINGS, Color.GREEN))
                    .addEquipment(EquipmentSlot.FEET, createColoredLeatherArmor(Material.LEATHER_BOOTS, Color.GREEN))
                    .setBreakWindow(new BreakWindowData(40))
                    .setHealth(20)
                    .setKnockbackResistance(0.8)
                    .setMeleeDamage(3)
                    .setDefense(0)
    ),
    NORMAL_MEDIUM(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .addEquipment(EquipmentSlot.HEAD, createColoredLeatherArmor(Material.LEATHER_HELMET, Color.BLACK))
                    .addEquipment(EquipmentSlot.CHEST, new ItemStack(Material.CHAINMAIL_CHESTPLATE))
                    .addEquipment(EquipmentSlot.FEET, new ItemStack(Material.CHAINMAIL_BOOTS))
                    .setBreakWindow(new BreakWindowData(30))
                    .setHealth(20)
                    .setKnockbackResistance(0.8)
                    .setMeleeDamage(4)
                    .setDefense(2)
    ),
    NORMAL_HARD(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .addEquipment(EquipmentSlot.CHEST, new ItemStack(Material.IRON_CHESTPLATE))
                    .addEquipment(EquipmentSlot.LEGS, new ItemStack(Material.IRON_LEGGINGS))
                    .addEquipment(EquipmentSlot.HAND, new ItemStack(Material.DIAMOND_AXE))
                    .setBreakWindow(new BreakWindowData(20))
                    .setHealth(20)
                    .setKnockbackResistance(0.95)
                    .setMeleeDamage(5)
                    .setDefense(5)
    ),
    PIG_ZOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIFIED_PIGLIN)
                    .setHealth(20)
                    .setBreakWindow(new BreakWindowData(20))
                    .setFireImmune(true)
                    .setKnockbackResistance(0.8)
                    .setMeleeDamage(5)
                    .setDefense(0)
    ),
    MAGMA_CUBE(
            new ZombieData()
                    .setEntity(EntityType.MAGMA_CUBE)
                    .setHealth(3)
                    .setFireImmune(true)
                    .setInitializer(entity -> ((MagmaCube) entity).setSize(2))
                    .setKnockbackResistance(0.0)
                    .setMeleeDamage(2)
                    .setDefense(0)
    ),
    SLIME_BLOB(
            new ZombieData()
                    .setEntity(EntityType.SLIME)
                    .setHealth(25)
                    .setInitializer(entity -> ((Slime) entity).setSize(1))
                    .setMeleeDamage(3)
                    .setDefense(0)
    ),
    WARDEN(
            new ZombieData()
                    .setEntity(EntityType.WARDEN)
                    .setHealth(200)
                    .setInitializer(entity -> {
                        final Warden warden = (Warden) entity;
                        final AttributeInstance movementSpeed = warden.getAttribute(Attribute.MOVEMENT_SPEED);
                        if (movementSpeed != null) {
                            movementSpeed.setBaseValue(1.0);
                        }
                    })
                    .setKnockbackResistance(1.0)
                    .setMeleeDamage(10)
                    .setDefense(15)
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
                    .setKnockbackResistance(0.8)
                    .setMeleeDamage(4)
                    .setDefense(4)
    ),
    LITTLE_BOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setBreakWindow(new BreakWindowData(20))
                    .setHealth(10)
                    .setExplosionAttack(ExplosionAttackData.explodeOnDeath(4))
                    .addEquipment(EquipmentSlot.HEAD, new ItemStack(Material.TNT))
                    .setInitializer(entity -> ((Zombie) entity).setBaby())
                    .setKnockbackResistance(0.5)
                    .setMeleeDamage(2)
                    .setDefense(0)
    ),
    FIRE_ZOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setHealth(20)
                    .setBreakWindow(new BreakWindowData(20))
                    .setFireImmune(true)
                    .setFireAttack(new FireAttackData(3*20))
                    .addEquipment(EquipmentSlot.HAND, new ItemStack(Material.BLAZE_ROD))
                    .setKnockbackResistance(0.8)
                    .setMeleeDamage(4)
                    .setDefense(0)
    ),
    ZOMBIE_WOLF(
            new ZombieData()
                    .setEntity(EntityType.WOLF)
                    .setHealth(10)
                    .setBreakWindow(new BreakWindowData(20))
                    .setInitializer(entity -> ((Wolf) entity).setAngry(true))
                    .setKnockbackResistance(0.3)
                    .setMeleeDamage(3)
                    .setDefense(0)
    ),
    GUARDIAN_ZOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setHealth(20)
                    .setBreakWindow(new BreakWindowData(2*20))
                    .addEquipment(EquipmentSlot.HEAD, new ItemStack(Material.SEA_LANTERN))
                    .setLaserAttack(new LaserAttackData(3))
                    .setKnockbackResistance(0.8)
                    .setMeleeDamage(4)
                    .setDefense(0)
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
                    .setKnockbackResistance(1.0)
                    .setMeleeDamage(6)
                    .setDefense(5)
    ),
    SUICIDER(
            new ZombieData()
                    .setEntity(EntityType.CREEPER)
                    .setHealth(20)
                    .setKnockbackResistance(1.0)
                    .setDefense(0)
                    .setExplosionAttack(new ExplosionAttackData(0, false, 4))
    ),
    BOMBIE(
            new ZombieData()
                    .setEntity(EntityType.ZOMBIE)
                    .setHealth(100)
                    .setExplosionAttack(ExplosionAttackData.explodePeriodically(2*20 + 10, 4))
                    .setBreakWindow(new BreakWindowData(20))
                    .setKnockbackResistance(1.0)
                    .setMeleeDamage(5)
                    .setDefense(5)
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
            case SKELETON -> Material.SKELETON_SKULL;
            case NORMAL_EASY, NORMAL_MEDIUM, NORMAL_HARD -> Material.ZOMBIE_HEAD;
            case PIG_ZOMBIE -> Material.PIGLIN_HEAD;
            case MAGMA_CUBE, MAGMA_ZOMBIE -> Material.MAGMA_CREAM;
            case SLIME_BLOB -> Material.SLIME_BALL;
            case WARDEN -> Material.SCULK_SHRIEKER;
            case LITTLE_BOMBIE, BOMBIE -> Material.TNT;
            case FIRE_ZOMBIE, INFERNO -> Material.BLAZE_ROD;
            case ZOMBIE_WOLF -> Material.BONE;
            case GUARDIAN_ZOMBIE -> Material.SEA_LANTERN;
            case SUICIDER -> Material.BEE_SPAWN_EGG;
        };
    }

    private static ItemStack createColoredLeatherArmor(final Material material, final Color color) {
        final ItemStack item = new ItemStack(material);
        final LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createCustomPlayerHead(final UUID profileId, final String textureUrl, final String loreText) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        if (!(item.getItemMeta() instanceof final SkullMeta skullMeta)) {
            return item;
        }

        final PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "skeleton_zombie");
        try {
            profile.getTextures().setSkin(URI.create(textureUrl).toURL());
        } catch (final IllegalArgumentException | MalformedURLException ignored) {
            // Keep default texture if URL is invalid.
        }

        skullMeta.setOwnerProfile(profile);
        skullMeta.setLore(List.of(loreText));
        item.setItemMeta(skullMeta);
        return item;
    }

    private static UUID createUuidFromIntArray(final int part0, final int part1, final int part2, final int part3) {
        final long mostSignificantBits = ((long) part0 << 32) | (part1 & 0xFFFFFFFFL);
        final long leastSignificantBits = ((long) part2 << 32) | (part3 & 0xFFFFFFFFL);
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
