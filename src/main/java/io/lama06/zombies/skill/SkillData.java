package io.lama06.zombies.skill;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public final class SkillData {
    public Component displayName;
    public Material material;
    public int cooldownTicks;
    public boolean inLuckyChest;

    public SkillData setDisplayName(final Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public SkillData setMaterial(final Material material) {
        this.material = material;
        return this;
    }

    public SkillData setCooldownTicks(final int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
        return this;
    }

    public SkillData includeInLuckyChest() {
        this.inLuckyChest = true;
        return this;
    }
}
