package io.lama06.zombies.system.zombie.creeper_bee;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.util.pdc.UuidDataType;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.bukkit.craftbukkit.entity.CraftMob;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class CreeperBeeChaseSystem implements Listener {
    private static final double SPEED = 5.0;
    private static final double MAX_Y_SPEED = 1.0;

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getGameWorlds()) {
            final List<Player> targets = getTargets(world);
            if (targets.isEmpty()) {
                continue;
            }
            for (final Bee bee : world.getBukkit().getEntitiesByClass(Bee.class)) {
                if (!isSuiciderBee(bee)) {
                    continue;
                }
                if (!isPartnerAlive(bee)) {
                    bee.remove();
                    continue;
                }
                final Player target = targets.stream()
                        .min(Comparator.comparingDouble(p ->
                                p.getLocation().distanceSquared(bee.getLocation())))
                        .orElse(null);
                if (target == null) {
                    continue;
                }
                final Vector toTarget = target.getLocation().toVector().subtract(bee.getLocation().toVector());
                if (toTarget.lengthSquared() < 0.01) {
                    continue;
                }
                boolean moved = false;
                if (bee instanceof final CraftMob craftMob) {
                    final var nmsMob = craftMob.getHandle();
                    nmsMob.getLookControl().setLookAt(
                            target.getLocation().getX(),
                            target.getLocation().getY() + 1,
                            target.getLocation().getZ()
                    );
                    nmsMob.getNavigation().moveTo(
                            target.getLocation().getX(),
                            target.getLocation().getY(),
                            target.getLocation().getZ(),
                            SPEED
                    );
                    moved = true;
                }
                if (!moved) {
                    final Vector velocity = toTarget.normalize().multiply(SPEED);
                    final double clampedY = Math.max(-MAX_Y_SPEED, Math.min(MAX_Y_SPEED, velocity.getY()));
                    velocity.setY(clampedY);
                    bee.setVelocity(velocity);
                }
            }
        }
    }

    private List<Player> getTargets(final ZombiesWorld world) {
        return world.getAlivePlayers().stream()
                .map(ZombiesPlayer::getBukkit)
                .toList();
    }

    private boolean isSuiciderBee(final Bee bee) {
        return bee.getPersistentDataContainer().has(CreeperBeeKeys.partnerKey(), UuidDataType.INSTANCE);
    }

    private boolean isPartnerAlive(final Bee bee) {
        final PersistentDataContainer pdc = bee.getPersistentDataContainer();
        final UUID partnerId = pdc.get(CreeperBeeKeys.partnerKey(), UuidDataType.INSTANCE);
        if (partnerId == null) {
            return false;
        }
        final Entity partner = bee.getWorld().getEntity(partnerId);
        return partner != null && partner.isValid();
    }
}
