package io.lama06.zombies.weapon;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public final class WeaponData {
    public Component displayName;
    public Material material;
    public AmmoData ammo;
    public DelayData delay;
    public ReloadData reload;
    public ShootData shoot;
    public ShootParticleData shootParticle;
    public MeleeData melee;
    public AttackData attack;
    public boolean inLuckyChest;
    public WeaponType upgradesTo;  // 升级后的武器类型，null 表示不可升级/已满级
    public boolean glowing;  // 是否发光（升级版武器）
    public BurstData burst;  // 连发数据，null 表示不连发

    public WeaponData setDisplayName(final Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public WeaponData setMaterial(final Material material) {
        this.material = material;
        return this;
    }

    public WeaponData setAmmo(final AmmoData ammo) {
        this.ammo = ammo;
        return this;
    }

    public WeaponData setDelay(final DelayData delay) {
        this.delay = delay;
        return this;
    }

    public WeaponData setReload(final ReloadData reload) {
        this.reload = reload;
        return this;
    }

    public WeaponData setShoot(final ShootData shoot) {
        this.shoot = shoot;
        return this;
    }

    public WeaponData setShootParticle(final ShootParticleData shootParticle) {
        this.shootParticle = shootParticle;
        return this;
    }

    public WeaponData setMelee(final MeleeData melee) {
        this.melee = melee;
        return this;
    }

    public WeaponData setAttack(final AttackData attack) {
        this.attack = attack;
        return this;
    }

    public WeaponData includeInLuckyChest() {
        inLuckyChest = true;
        return this;
    }

    public WeaponData setUpgradesTo(final WeaponType upgradesTo) {
        this.upgradesTo = upgradesTo;
        return this;
    }

    public WeaponData setGlowing() {
        this.glowing = true;
        return this;
    }

    public WeaponData setBurst(final BurstData burst) {
        this.burst = burst;
        return this;
    }
}
