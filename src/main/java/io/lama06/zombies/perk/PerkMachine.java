package io.lama06.zombies.perk;

import io.lama06.zombies.CheckableConfig;
import io.lama06.zombies.InvalidConfigException;
import io.lama06.zombies.menu.*;
import io.lama06.zombies.util.PositionUtil;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PerkMachine implements CheckableConfig {
    public BlockPosition position;
    public final List<PerkOffer> offers = new ArrayList<>();

    // Legacy fields kept for backwards compatibility with existing configs/templates.
    public PlayerPerk perk = PlayerPerk.EXTRA_HEALTH;
    public int gold = 500;

    @Override
    public void check() throws InvalidConfigException {
        InvalidConfigException.mustBeSet(position, "position");
        if (!offers.isEmpty()) {
            InvalidConfigException.checkList(offers, true, "offers");
        } else {
            InvalidConfigException.mustBeSet(perk, "perk");
        }
    }

    public List<PerkOffer> getOffers() {
        if (!offers.isEmpty()) {
            return offers;
        }
        if (perk == null) {
            return List.of();
        }
        return List.of(new PerkOffer(perk, gold));
    }

    private List<PerkOffer> getEditableOffers() {
        if (offers.isEmpty() && perk != null) {
            offers.add(new PerkOffer(perk, gold));
        }
        return offers;
    }

    public Component getMenuDisplayName() {
        final List<PerkOffer> machineOffers = getOffers();
        if (machineOffers.isEmpty()) {
            return Component.text("Vending Machine: (no perks configured)");
        }
        if (machineOffers.size() == 1) {
            return Component.text("Vending Machine: ").append(machineOffers.getFirst().perk.getDisplayName());
        }
        return Component.text("Vending Machine (" + machineOffers.size() + " perks)");
    }

    public void openMenu(final Player player, final Runnable callback) {
        final Runnable reopen = () -> openMenu(player, callback);
        SelectionMenu.open(
                player,
                Component.text("Vending Machine"),
                callback,
                new SelectionEntry(
                        Component.text("Position: " + PositionUtil.format(position)),
                        Material.LEVER,
                        () -> BlockPositionSelection.open(
                                player,
                                Component.text("Perk Machine Position"),
                                reopen,
                                position -> {
                                    this.position = position;
                                    reopen.run();
                                }
                        )
                ),
                new SelectionEntry(
                        Component.text("Perks (" + getOffers().size() + ")"),
                        Material.COMMAND_BLOCK,
                        () -> ListConfigMenu.open(
                                player,
                                Component.text("Perk Offers"),
                                getEditableOffers(),
                                Material.GOLD_INGOT,
                                offer -> Component.text("Offer: ").append(offer.perk.getDisplayName())
                                        .append(Component.text(" - " + offer.gold + " Gold").color(NamedTextColor.GOLD)),
                                PerkOffer::new,
                                offer -> offer.openMenu(player, reopen),
                                reopen
                        )
                ),
                new SelectionEntry(
                        Component.text("Legacy Fallback: ")
                                .append(perk != null ? perk.getDisplayName() : Component.text("none").color(NamedTextColor.GRAY))
                                .append(Component.text(" (" + gold + " Gold)").color(NamedTextColor.DARK_GRAY)),
                        Material.GOLD_NUGGET,
                        () -> InputMenu.open(
                                player,
                                Component.text("Legacy Perk Price").color(NamedTextColor.GOLD),
                                this.gold,
                                new IntegerInputType(),
                                legacyGold -> {
                                    this.gold = legacyGold;
                                    reopen.run();
                                },
                                reopen
                        ),
                        Component.text("Edit Legacy Perk").color(NamedTextColor.GRAY),
                        () -> EnumSelectionMenu.open(
                                PlayerPerk.class,
                                player,
                                Component.text("Legacy Perk"),
                                reopen,
                                legacyPerk -> {
                                    this.perk = legacyPerk;
                                    reopen.run();
                                }
                        )
                )
        );
    }
}
