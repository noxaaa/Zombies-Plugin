package io.lama06.zombies.perk;

import io.lama06.zombies.CheckableConfig;
import io.lama06.zombies.InvalidConfigException;
import io.lama06.zombies.menu.EnumSelectionMenu;
import io.lama06.zombies.menu.InputMenu;
import io.lama06.zombies.menu.IntegerInputType;
import io.lama06.zombies.menu.SelectionEntry;
import io.lama06.zombies.menu.SelectionMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PerkOffer implements CheckableConfig {
    public PlayerPerk perk = PlayerPerk.EXTRA_HEALTH;
    public int gold = 500;

    public PerkOffer() { }

    public PerkOffer(final PlayerPerk perk, final int gold) {
        this.perk = perk;
        this.gold = gold;
    }

    @Override
    public void check() throws InvalidConfigException {
        InvalidConfigException.mustBeSet(perk, "perk");
    }

    public void openMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openMenu(player, callback);
        SelectionMenu.open(
                player,
                Component.text("Perk Offer"),
                callback,
                new SelectionEntry(
                        Component.text("Perk: ").append(perk.getDisplayName()),
                        perk.getDisplayMaterial(),
                        () -> EnumSelectionMenu.open(
                                PlayerPerk.class,
                                player,
                                Component.text("Perk"),
                                reopen,
                                perk -> {
                                    this.perk = perk;
                                    reopen.run();
                                }
                        )
                ),
                new SelectionEntry(
                        Component.text("Gold: " + gold).color(NamedTextColor.GOLD),
                        Material.GOLD_NUGGET,
                        () -> InputMenu.open(
                                player,
                                Component.text("Perk Offer Price").color(NamedTextColor.GOLD),
                                gold,
                                new IntegerInputType(),
                                gold -> {
                                    this.gold = gold;
                                    reopen.run();
                                },
                                reopen
                        )
                )
        );
    }
}
