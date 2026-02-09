package io.lama06.zombies;

import io.lama06.zombies.data.AttributeId;
import io.lama06.zombies.data.Storage;
import io.lama06.zombies.data.StorageSession;
import io.lama06.zombies.event.player.PlayerGoldChangeEvent;
import io.lama06.zombies.event.weapon.WeaponCreateEvent;
import io.lama06.zombies.perk.PlayerPerk;
import io.lama06.zombies.skill.Skill;
import io.lama06.zombies.skill.SkillType;
import io.lama06.zombies.util.pdc.EnumPersistentDataType;
import io.lama06.zombies.weapon.Weapon;
import io.lama06.zombies.weapon.WeaponType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

public final class ZombiesPlayer extends Storage implements ForwardingAudience {
    public static final AttributeId<Integer> GAME_ID = new AttributeId<>("game_id", PersistentDataType.INTEGER);
    public static final AttributeId<Integer> GOLD = new AttributeId<>("gold", PersistentDataType.INTEGER);
    public static final AttributeId<Integer> KILLS = new AttributeId<>("kills", PersistentDataType.INTEGER);
    private static final String REMEMBERED_WEAPON_UPGRADE_PREFIX = "remembered_weapon_upgrade_";
    private static final EnumPersistentDataType<WeaponType> WEAPON_TYPE_DATA_TYPE = new EnumPersistentDataType<>(WeaponType.class);

    private final Player player;

    public ZombiesPlayer(final Player player) {
        this.player = player;
    }

    public Weapon getWeapon(final int slot) {
        final Weapon weapon = new Weapon(this, slot);
        if (!weapon.isWeapon()) {
            return null;
        }
        return new Weapon(this, slot);
    }

    public Weapon getHeldWeapon() {
        return getWeapon(player.getInventory().getHeldItemSlot());
    }

    public List<Weapon> getWeapons() {
        final List<Weapon> weapons = new ArrayList<>();
        for (int slot = 0; slot < 4; slot++) {
            final Weapon weapon = getWeapon(slot);
            if (weapon == null) {
                continue;
            }
            weapons.add(weapon);
        }
        return weapons;
    }

    public int getLastWeaponSlot() {
        final WorldConfig config = getWorld().getConfig();
        final int defaultWeaponSlots = config != null ? config.defaultWeaponSlots : 2;
        if (defaultWeaponSlots >= 3) {
            return 3;
        }
        return hasPerk(PlayerPerk.EXTRA_WEAPON) ? 3 : 2;
    }

    /**
     * Checks if the player already has a weapon of the same family (base type).
     * @param type The weapon type to check
     * @return true if the player already has a weapon of the same family
     */
    public boolean hasWeaponOfSameType(final WeaponType type) {
        return getWeaponOfSameType(type) != null;
    }

    /**
     * Gets the weapon of the same family (base type) if the player has one.
     * @param type The weapon type to check
     * @return the weapon of the same family, or null if not found
     */
    public Weapon getWeaponOfSameType(final WeaponType type) {
        for (final Weapon weapon : getWeapons()) {
            if (weapon.get(Weapon.TYPE).isSameFamily(type)) {
                return weapon;
            }
        }
        return null;
    }

    private static AttributeId<WeaponType> getRememberedWeaponUpgradeAttribute(final WeaponType baseType) {
        return new AttributeId<>(
                REMEMBERED_WEAPON_UPGRADE_PREFIX + baseType.name().toLowerCase(Locale.ROOT),
                WEAPON_TYPE_DATA_TYPE
        );
    }

    private static WeaponType getMoreAdvancedWeaponType(final WeaponType first, final WeaponType second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        if (!first.isSameFamily(second)) {
            throw new IllegalArgumentException("Weapon types must be from the same family");
        }
        WeaponType current = first;
        while (current != null) {
            if (current == second) {
                return second;
            }
            current = current.data.upgradesTo;
        }
        return first;
    }

    public WeaponType resolveRememberedWeaponType(final WeaponType requestedType) {
        final WeaponType baseType = requestedType.getBaseType();
        final WeaponType rememberedType = get(getRememberedWeaponUpgradeAttribute(baseType));
        if (rememberedType == null || !rememberedType.isSameFamily(requestedType)) {
            return requestedType;
        }
        return getMoreAdvancedWeaponType(rememberedType, requestedType);
    }

    public void rememberWeaponUpgrade(final WeaponType type) {
        final WeaponType baseType = type.getBaseType();
        if (type == baseType) {
            return;
        }
        final AttributeId<WeaponType> attribute = getRememberedWeaponUpgradeAttribute(baseType);
        final WeaponType rememberedType = get(attribute);
        if (rememberedType == null || !rememberedType.isSameFamily(type)) {
            set(attribute, type);
            return;
        }
        set(attribute, getMoreAdvancedWeaponType(rememberedType, type));
    }

    public void clearRememberedWeaponUpgrades() {
        for (final WeaponType type : WeaponType.values()) {
            if (type.getBaseType() != type) {
                continue;
            }
            remove(getRememberedWeaponUpgradeAttribute(type));
        }
    }

    public Weapon giveWeapon(final int slot, final WeaponType type) {
        final WeaponType resolvedType = resolveRememberedWeaponType(type);
        final ItemStack item = new ItemStack(resolvedType.data.material);
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(resolvedType.data.displayName);
        if (resolvedType.data.glowing) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        // 近战武器设置超高攻击速度（移除原版攻击冷却）并设为不可损坏
        if (resolvedType.data.melee != null) {
            meta.addAttributeModifier(
                    Attribute.ATTACK_SPEED,
                    new AttributeModifier(
                            UUID.randomUUID(),
                            "generic.attack_speed",
                            1000,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlot.HAND
                    )
            );
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        item.setItemMeta(meta);
        // 近战武器添加格挡功能（减少50%伤害）
        if (resolvedType.data.melee != null) {
            item.setData(DataComponentTypes.BLOCKS_ATTACKS, BlocksAttacks.blocksAttacks()
                    .blockDelaySeconds(0)
                    .addDamageReduction(DamageReduction.damageReduction()
                            .factor(0.5f)
                            .build())
                    .build());
        }
        final PlayerInventory inventory = player.getInventory();
        inventory.setItem(slot, item);
        final Weapon weapon = new Weapon(this, slot);
        weapon.set(Weapon.IS_WEAPON, true);
        weapon.set(Weapon.TYPE, resolvedType);
        Bukkit.getPluginManager().callEvent(new WeaponCreateEvent(weapon, resolvedType.data));
        rememberWeaponUpgrade(resolvedType);
        return weapon;
    }

    public boolean isAlive() {
        return player.getGameMode() == GameMode.ADVENTURE;
    }

    public PlayerPerk getPerk(final int slot) {
        final ItemStack item = player.getInventory().getItem(slot);
        if (item == null) {
            return null;
        }
        final PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        final boolean isPerk = pdc.getOrDefault(PlayerPerk.IS_PLAYER_PERK.getKey(), PlayerPerk.IS_PLAYER_PERK.type(), false);
        if (!isPerk) {
            return null;
        }
        return pdc.get(PlayerPerk.TYPE.getKey(), PlayerPerk.TYPE.type());
    }

    public List<PlayerPerk> getPerks() {
        return IntStream.range(6, 9).mapToObj(this::getPerk).filter(Objects::nonNull).toList();
    }

    public boolean hasPerk(final PlayerPerk type) {
        return getPerks().contains(type);
    }

    public void givePerk(final int slot, final PlayerPerk perk) {
        if (slot < 6 || slot >= 9) {
            throw new IllegalArgumentException();
        }
        final PlayerPerk oldPerk = getPerk(slot);
        if (oldPerk != null) {
            oldPerk.disable(this);
        }
        final ItemStack item = new ItemStack(perk.getDisplayMaterial());
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(perk.getDisplayName());
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(PlayerPerk.TYPE.getKey(), PlayerPerk.TYPE.type(), perk);
        pdc.set(PlayerPerk.IS_PLAYER_PERK.getKey(), PlayerPerk.IS_PLAYER_PERK.type(), true);
        item.setItemMeta(meta);
        player.getInventory().setItem(slot, item);
        perk.enable(this);
    }

    public void clearPerks() {
        final PlayerInventory inventory = player.getInventory();
        for (int slot = 6; slot < 9; slot++) {
            final PlayerPerk perk = getPerk(slot);
            if (perk != null) {
                perk.disable(this);
            }
            // Restore placeholder
            inventory.setItem(slot, PlaceholderItem.createPerkPlaceholder(slot - 5));
        }
    }

    public Skill getSkill() {
        final Skill skill = new Skill(this);
        if (!skill.isSkill()) {
            return null;
        }
        return skill;
    }

    public void giveSkill(final SkillType type) {
        final ItemStack item = new ItemStack(type.data.material);
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(type.data.displayName);
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Skill.IS_SKILL.getKey(), Skill.IS_SKILL.type(), true);
        pdc.set(Skill.TYPE.getKey(), Skill.TYPE.type(), type);
        item.setItemMeta(meta);
        player.getInventory().setItem(Skill.SLOT, item);
    }

    public void clearSkill() {
        player.getInventory().setItem(Skill.SLOT, PlaceholderItem.createSkillPlaceholder());
    }

    public boolean requireGold(final int gold) {
        final int currentGold = get(GOLD);
        if (currentGold < gold) {
            sendMessage(Component.text("You cannot afford this").color(NamedTextColor.RED));
            return false;
        }
        return true;
    }

    public void pay(final int gold) {
        final int currentGold = get(GOLD);
        final int newGold = currentGold - gold;
        set(GOLD, newGold);
        Bukkit.getPluginManager().callEvent(new PlayerGoldChangeEvent(this, currentGold, newGold));
    }

    @Override
    protected StorageSession startSession() {
        return player::getPersistentDataContainer;
    }

    @Override
    public Iterable<? extends Audience> audiences() {
        return Set.of(player);
    }

    public Player getBukkit() {
        return player;
    }

    public ZombiesWorld getWorld() {
        return new ZombiesWorld(player.getWorld());
    }
}
