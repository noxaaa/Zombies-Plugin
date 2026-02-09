package io.lama06.zombies;

import io.lama06.zombies.menu.MenuDisplayableEnum;
import io.lama06.zombies.offhand.OffhandItemType;
import io.lama06.zombies.skill.SkillType;
import io.lama06.zombies.weapon.WeaponType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public final class LuckyChestItemEntry {
    public String type;  // "WEAPON" or "SKILL" or "OFFHAND"
    public WeaponType weapon;
    public SkillType skill;
    public OffhandItemType offhand;

    public LuckyChestItemEntry() {
    }

    public LuckyChestItemEntry(final WeaponType weapon) {
        this.type = "WEAPON";
        this.weapon = weapon;
    }

    public LuckyChestItemEntry(final SkillType skill) {
        this.type = "SKILL";
        this.skill = skill;
    }

    public LuckyChestItemEntry(final OffhandItemType offhand) {
        this.type = "OFFHAND";
        this.offhand = offhand;
    }

    public boolean isWeapon() {
        return "WEAPON".equals(type);
    }

    public boolean isSkill() {
        return "SKILL".equals(type);
    }

    public boolean isOffhand() {
        return "OFFHAND".equals(type);
    }

    public Component getDisplayName() {
        if (isWeapon() && weapon != null) {
            return weapon.getDisplayName();
        } else if (isSkill() && skill != null) {
            return skill.getDisplayName();
        } else if (isOffhand() && offhand != null) {
            return offhand.getDisplayName();
        }
        return Component.text("Unknown");
    }

    public Material getDisplayMaterial() {
        if (isWeapon() && weapon != null) {
            return weapon.getDisplayMaterial();
        } else if (isSkill() && skill != null) {
            return skill.getDisplayMaterial();
        } else if (isOffhand() && offhand != null) {
            return offhand.getDisplayMaterial();
        }
        return Material.BARRIER;
    }

    public MenuDisplayableEnum getItem() {
        if (isWeapon()) {
            return weapon;
        } else if (isSkill()) {
            return skill;
        } else if (isOffhand()) {
            return offhand;
        }
        return null;
    }
}
