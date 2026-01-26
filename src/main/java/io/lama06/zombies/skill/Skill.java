package io.lama06.zombies.skill;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.data.AttributeId;
import io.lama06.zombies.data.ComponentId;
import io.lama06.zombies.data.Storage;
import io.lama06.zombies.data.StorageSession;
import io.lama06.zombies.util.pdc.EnumPersistentDataType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public final class Skill extends Storage {
    public static final int SLOT = 4;

    public static final AttributeId<Boolean> IS_SKILL = new AttributeId<>("is_skill", PersistentDataType.BOOLEAN);
    public static final AttributeId<SkillType> TYPE = new AttributeId<>("skill_type", new EnumPersistentDataType<>(SkillType.class));

    public static final ComponentId COOLDOWN = new ComponentId("skill_cooldown");

    public static final AttributeId<Integer> REMAINING_COOLDOWN = new AttributeId<>("remaining_cooldown", PersistentDataType.INTEGER);

    private final ZombiesPlayer player;

    public Skill(final ZombiesPlayer player) {
        this.player = player;
    }

    public ZombiesPlayer getPlayer() {
        return player;
    }

    public ItemStack getItem() {
        return player.getBukkit().getInventory().getItem(SLOT);
    }

    public ZombiesWorld getWorld() {
        return new ZombiesWorld(player.getBukkit().getWorld());
    }

    public boolean isSkill() {
        final ItemStack item = getItem();
        if (item == null) {
            return false;
        }
        return Objects.requireNonNullElse(get(IS_SKILL), false);
    }

    public SkillType getType() {
        return get(TYPE);
    }

    public SkillData getData() {
        final SkillType type = getType();
        return type != null ? type.data : null;
    }

    public int getRemainingCooldown() {
        final io.lama06.zombies.data.Component cooldown = getComponent(COOLDOWN);
        if (cooldown == null) {
            return 0;
        }
        final Integer remaining = cooldown.get(REMAINING_COOLDOWN);
        return remaining != null ? remaining : 0;
    }

    public boolean isOnCooldown() {
        return getRemainingCooldown() > 0;
    }

    @Override
    protected StorageSession startSession() {
        final ItemStack item = getItem();
        final ItemMeta meta = item.getItemMeta();
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return new StorageSession() {
            @Override
            public PersistentDataContainer getData() {
                return pdc;
            }

            @Override
            public void applyChanges() {
                item.setItemMeta(meta);
            }
        };
    }
}
