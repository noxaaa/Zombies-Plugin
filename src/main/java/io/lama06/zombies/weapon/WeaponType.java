package io.lama06.zombies.weapon;

import io.lama06.zombies.menu.MenuDisplayableEnum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;

public enum WeaponType implements MenuDisplayableEnum {
    KNIFE(
            new WeaponData()
                    .setDisplayName(Component.text("Knife"))
                    .setMaterial(Material.IRON_SWORD)
                    .setMelee(new MeleeData(5))
                    .setAttack(new AttackData(5, false, 10))
                    .setDelay(new DelayData(10))
    ),
    PISTOL(
            new WeaponData()
                    .setDisplayName(Component.text("Pistol"))
                    .setMaterial(Material.WOODEN_HOE)
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.CRIT))
                    .setAttack(new AttackData(6, false, 10))
                    .setAmmo(new AmmoData(300, 10))
                    .setDelay(new DelayData(10))  // 0.5s
                    .setReload(new ReloadData(30))  // 1.5s
    ),
    RIFLE(
            new WeaponData()
                    .setDisplayName(Component.text("Rifle"))
                    .setMaterial(Material.STONE_HOE)
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(6, false, 10))
                    .setAmmo(new AmmoData(256, 32))
                    .setDelay(new DelayData(4))  // 0.2s
                    .setReload(new ReloadData(30))  // 1.5s
    ),
    SHOTGUN(
            new WeaponData()
                    .setDisplayName(Component.text("Shotgun"))
                    .setMaterial(Material.IRON_HOE)
                    .setShoot(new ShootData(10, 0.85))
                    .setShootParticle(new ShootParticleData(Particle.LARGE_SMOKE))
                    .setAttack(new AttackData(6.5, false, 10))
                    .setAmmo(new AmmoData(65, 5))
                    .setDelay(new DelayData(28))  // 1.4s
                    .setReload(new ReloadData(30))  // 1.5s
    ),
    SNIPER(
            new WeaponData()
                    .setDisplayName(Component.text("Sniper"))
                    .setMaterial(Material.WOODEN_SHOVEL)
                    .setShoot(new ShootData(1, 1, 2))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(30, false, 30))
                    .setAmmo(new AmmoData(40, 4))
                    .setDelay(new DelayData(20))  // 1.0s
                    .setReload(new ReloadData(40))  // 2.0s
                    .includeInLuckyChest()
    ),
    FLAME_THROWER(
            new WeaponData()
                    .setDisplayName(Component.text("Flame Thrower"))
                    .setMaterial(Material.GOLDEN_HOE)
                    .setShoot(new ShootData(1, 0.95))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(2, true, 4))
                    .setAmmo(new AmmoData(350, 50))
                    .setDelay(new DelayData(2))  // 0.1s
                    .setReload(new ReloadData(60))  // 3.0s
                    .includeInLuckyChest()
    ),
    GOLD_DIGGER(
            new WeaponData()
                    .setDisplayName(Component.text("Gold Digger").color(NamedTextColor.GOLD))
                    .setMaterial(Material.GOLDEN_PICKAXE)
                    .setAttack(new AttackData(6, false, 15))
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setDelay(new DelayData(10))  // 0.5s, DPS=12
                    .setReload(new ReloadData(30))
                    .setAmmo(new AmmoData(70, 7))
                    .includeInLuckyChest()
    ),

    // 升级版武器
    KNIFE_UPGRADED(
            new WeaponData()
                    .setDisplayName(Component.text("Knife Ultimate"))
                    .setMaterial(Material.DIAMOND_SWORD)
                    .setMelee(new MeleeData(6))
                    .setAttack(new AttackData(8, false, 15))
                    .setDelay(new DelayData(8))
                    .setGlowing()
    ),
    PISTOL_UPGRADED(
            new WeaponData()
                    .setDisplayName(Component.text("Pistol Ultimate"))
                    .setMaterial(Material.WOODEN_HOE)
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.CRIT))
                    .setAttack(new AttackData(8, false, 15))
                    .setAmmo(new AmmoData(450, 14))  // 450总弹药, 14弹匣(7次连发×2)
                    .setDelay(new DelayData(10))  // 0.5s
                    .setReload(new ReloadData(25))
                    .setBurst(new BurstData(2, 2))  // 双连发, 间隔0.1s(2tick)
                    .setGlowing()
    ),
    RIFLE_UPGRADED(
            new WeaponData()
                    .setDisplayName(Component.text("Rifle Ultimate"))
                    .setMaterial(Material.STONE_HOE)
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(8, false, 10))
                    .setAmmo(new AmmoData(400, 40))
                    .setDelay(new DelayData(3))
                    .setReload(new ReloadData(25))
                    .setGlowing()
    ),
    SHOTGUN_UPGRADED(
            new WeaponData()
                    .setDisplayName(Component.text("Shotgun Ultimate"))
                    .setMaterial(Material.IRON_HOE)
                    .setShoot(new ShootData(12, 0.85))
                    .setShootParticle(new ShootParticleData(Particle.LARGE_SMOKE))
                    .setAttack(new AttackData(2.5, false, 12))
                    .setAmmo(new AmmoData(85, 7))
                    .setDelay(new DelayData((int) (1.2 * 20)))
                    .setReload(new ReloadData((int) (1.2 * 20)))
                    .setGlowing()
    ),
    SNIPER_UPGRADED(
            new WeaponData()
                    .setDisplayName(Component.text("Sniper Ultimate"))
                    .setMaterial(Material.WOODEN_SHOVEL)
                    .setShoot(new ShootData(2, 1, 3))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(30, false, 40))
                    .setAmmo(new AmmoData(60, 6))
                    .setDelay(new DelayData(25))
                    .setReload(new ReloadData(35))
                    .setGlowing()
    ),
    FLAME_THROWER_UPGRADED(
            new WeaponData()
                    .setDisplayName(Component.text("Flame Thrower Ultimate"))
                    .setMaterial(Material.GOLDEN_HOE)
                    .setShoot(new ShootData(1, 0.95))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(4, true, 6))
                    .setAmmo(new AmmoData(500, 70))
                    .setDelay(new DelayData(2))
                    .setReload(new ReloadData(25))
                    .setGlowing()
    ),
    GOLD_DIGGER_V2(
            new WeaponData()
                    .setDisplayName(Component.text("Gold Digger Ultimate I").color(NamedTextColor.GOLD))
                    .setMaterial(Material.GOLDEN_PICKAXE)
                    .setAttack(new AttackData(8, false, 18))
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setDelay(new DelayData(10))  // 0.5s, DPS=16
                    .setReload(new ReloadData(28))
                    .setAmmo(new AmmoData(100, 10))
                    .setGlowing()
    ),
    GOLD_DIGGER_V3(
            new WeaponData()
                    .setDisplayName(Component.text("Gold Digger Ultimate II").color(NamedTextColor.GOLD))
                    .setMaterial(Material.GOLDEN_PICKAXE)
                    .setAttack(new AttackData(10, false, 20))
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setDelay(new DelayData(8))  // 0.4s, DPS=25
                    .setReload(new ReloadData(26))
                    .setAmmo(new AmmoData(130, 13))
                    .setGlowing()
    ),
    GOLD_DIGGER_V4(
            new WeaponData()
                    .setDisplayName(Component.text("Gold Digger Ultimate III").color(NamedTextColor.GOLD))
                    .setMaterial(Material.GOLDEN_PICKAXE)
                    .setAttack(new AttackData(12, false, 22))
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setDelay(new DelayData(6))  // 0.3s, DPS=40
                    .setReload(new ReloadData(24))
                    .setAmmo(new AmmoData(170, 17))
                    .setGlowing()
    ),
    GOLD_DIGGER_V5(
            new WeaponData()
                    .setDisplayName(Component.text("Gold Digger Ultimate IV").color(NamedTextColor.GOLD))
                    .setMaterial(Material.GOLDEN_PICKAXE)
                    .setAttack(new AttackData(15, false, 25))
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setDelay(new DelayData(6))  // 0.3s, DPS=50
                    .setReload(new ReloadData(22))
                    .setAmmo(new AmmoData(200, 20))
                    .setGlowing()
    ),
    GOLD_DIGGER_V6(
            new WeaponData()
                    .setDisplayName(Component.text("Gold Digger Ultimate V").color(NamedTextColor.GOLD))
                    .setMaterial(Material.GOLDEN_PICKAXE)
                    .setAttack(new AttackData(20, false, 30))
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setDelay(new DelayData(4))  // 0.2s, DPS=100
                    .setReload(new ReloadData(20))  // 1.0s
                    .setAmmo(new AmmoData(250, 25))
                    .setGlowing()
    );

    static {
        // 设置升级链
        KNIFE.data.upgradesTo = KNIFE_UPGRADED;
        PISTOL.data.upgradesTo = PISTOL_UPGRADED;
        RIFLE.data.upgradesTo = RIFLE_UPGRADED;
        SHOTGUN.data.upgradesTo = SHOTGUN_UPGRADED;
        SNIPER.data.upgradesTo = SNIPER_UPGRADED;
        FLAME_THROWER.data.upgradesTo = FLAME_THROWER_UPGRADED;
        GOLD_DIGGER.data.upgradesTo = GOLD_DIGGER_V2;
        GOLD_DIGGER_V2.data.upgradesTo = GOLD_DIGGER_V3;
        GOLD_DIGGER_V3.data.upgradesTo = GOLD_DIGGER_V4;
        GOLD_DIGGER_V4.data.upgradesTo = GOLD_DIGGER_V5;
        GOLD_DIGGER_V5.data.upgradesTo = GOLD_DIGGER_V6;
    }

    public final WeaponData data;

    WeaponType(final WeaponData data) {
        this.data = data;
    }

    @Override
    public Component getDisplayName() {
        return data.displayName;
    }

    @Override
    public Material getDisplayMaterial() {
        return data.material;
    }
}
