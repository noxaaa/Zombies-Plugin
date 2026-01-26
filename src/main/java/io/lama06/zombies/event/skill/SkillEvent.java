package io.lama06.zombies.event.skill;

import io.lama06.zombies.event.ZombiesEvent;
import io.lama06.zombies.skill.Skill;

public abstract class SkillEvent extends ZombiesEvent {
    private final Skill skill;

    protected SkillEvent(final Skill skill) {
        super(skill.getWorld());
        this.skill = skill;
    }

    public Skill getSkill() {
        return skill;
    }
}
