package io.lama06.zombies;

import io.lama06.zombies.data.AttributeId;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class UpgradeableArmorShopProgress {
    private static final AttributeId<List<Integer>> PURCHASE_COUNTS = new AttributeId<>(
            "upgradeable_armor_shop_purchase_counts",
            PersistentDataType.LIST.integers()
    );
    private static final AttributeId<Integer> PURCHASE_COUNTS_GAME_ID = new AttributeId<>(
            "upgradeable_armor_shop_purchase_counts_game_id",
            PersistentDataType.INTEGER
    );

    private UpgradeableArmorShopProgress() { }

    public static int getPurchaseCount(final ZombiesPlayer player, final int shopIndex) {
        if (shopIndex < 0) {
            return 0;
        }
        final List<Integer> counts = ensureCurrentGameCounts(player);
        if (shopIndex >= counts.size()) {
            return 0;
        }
        return Math.max(0, counts.get(shopIndex));
    }

    public static void setPurchaseCount(final ZombiesPlayer player, final int shopIndex, final int purchaseCount) {
        if (shopIndex < 0) {
            return;
        }
        final List<Integer> counts = new ArrayList<>(ensureCurrentGameCounts(player));
        while (counts.size() <= shopIndex) {
            counts.add(0);
        }
        counts.set(shopIndex, Math.max(0, purchaseCount));
        player.set(PURCHASE_COUNTS, counts);
    }

    public static void clear(final ZombiesPlayer player) {
        player.remove(PURCHASE_COUNTS);
        player.remove(PURCHASE_COUNTS_GAME_ID);
    }

    private static List<Integer> ensureCurrentGameCounts(final ZombiesPlayer player) {
        final Integer worldGameId = player.getWorld().get(ZombiesWorld.GAME_ID);
        final Integer countsGameId = player.get(PURCHASE_COUNTS_GAME_ID);
        final List<Integer> counts = player.get(PURCHASE_COUNTS);

        if (!Objects.equals(worldGameId, countsGameId)) {
            player.set(PURCHASE_COUNTS, List.of());
            if (worldGameId == null) {
                player.remove(PURCHASE_COUNTS_GAME_ID);
            } else {
                player.set(PURCHASE_COUNTS_GAME_ID, worldGameId);
            }
            return List.of();
        }

        if (counts == null) {
            player.set(PURCHASE_COUNTS, List.of());
            return List.of();
        }
        return counts;
    }
}
