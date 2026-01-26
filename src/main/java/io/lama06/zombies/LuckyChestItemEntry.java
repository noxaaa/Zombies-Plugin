package io.lama06.zombies;

import io.lama06.zombies.menu.MenuDisplayableEnum;
import io.lama06.zombies.skill.SkillType;
import io.lama06.zombies.weapon.WeaponType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public final class LuckyChestItemEntry {
    public String type;  // "WEAPON" or "SKILL"
    public WeaponType weapon;
    public SkillType skill;

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

    public boolean isWeapon() {
        return "WEAPON".equals(type);
    }

    public boolean isSkill() {
        return "SKILL".equals(type);
    }

    public Component getDisplayName() {
        if (isWeapon() && weapon != null) {
            return weapon.getDisplayName();
        } else if (isSkill() && skill != null) {
            return skill.getDisplayName();
        }
        return Component.text("Unknown");
    }

    public Material getDisplayMaterial() {
        if (isWeapon() && weapon != null) {
            return weapon.getDisplayMaterial();
        } else if (isSkill() && skill != null) {
            return skill.getDisplayMaterial();
        }
        return Material.BARRIER;
    }

    public MenuDisplayableEnum getItem() {
        if (isWeapon()) {
            return weapon;
        } else if (isSkill()) {
            return skill;
        }
        return null;
    }
}
