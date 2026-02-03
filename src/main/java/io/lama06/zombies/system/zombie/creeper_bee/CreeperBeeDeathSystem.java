package io.lama06.zombies.system.zombie.creeper_bee;

import io.lama06.zombies.util.pdc.UuidDataType;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.UUID;

/**
 * Handles the death of the Bee in a Creeper-Bee pair.
 * When the Bee dies, the Creeper partner is also removed.
 */
public final class CreeperBeeDeathSystem implements Listener {

    @EventHandler
    private void onBeeDeeath(final EntityDeathEvent event) {
        if (!(event.getEntity() instanceof final Bee bee)) {
            return;
        }

        final PersistentDataContainer pdc = bee.getPersistentDataContainer();
        final UUID partnerId = pdc.get(CreeperBeeKeys.partnerKey(), UuidDataType.INSTANCE);
        if (partnerId == null) {
            return;
        }

        final Entity partner = bee.getWorld().getEntity(partnerId);
        if (partner != null && partner.isValid()) {
            partner.remove();
        }
    }
}
