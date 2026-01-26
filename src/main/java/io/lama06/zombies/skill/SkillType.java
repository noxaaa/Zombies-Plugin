package io.lama06.zombies.skill;

import io.lama06.zombies.menu.MenuDisplayableEnum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public enum SkillType implements MenuDisplayableEnum {
    HEAL(
            new SkillData()
                    .setDisplayName(Component.text("Heal").color(NamedTextColor.GOLD))
                    .setMaterial(Material.GOLDEN_APPLE)
                    .setCooldownTicks(30 * 20)  // 30 seconds
                    .includeInLuckyChest()
    );

    public final SkillData data;

    SkillType(final SkillData data) {
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
