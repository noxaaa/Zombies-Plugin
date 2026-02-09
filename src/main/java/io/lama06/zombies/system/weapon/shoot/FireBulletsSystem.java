package io.lama06.zombies.system.weapon.shoot;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.papermc.paper.math.Position;
import io.lama06.zombies.data.Component;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.event.player.PlayerAttackZombieEvent;
import io.lama06.zombies.event.weapon.WeaponShootEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.util.VectorUtil;
import io.lama06.zombies.weapon.BurstData;
import io.lama06.zombies.weapon.DelayData;
import io.lama06.zombies.weapon.ShootData;
import io.lama06.zombies.weapon.Weapon;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class FireBulletsSystem implements Listener {
    private static final double TRACE_EPSILON = 1e-6;
    private static final double STEP_EPSILON = 1e-4;
    private static final int HOLD_TIMEOUT_TICKS = 4;

    private record HoldingState(int weaponSlot, int lastActivityTick) { }
    private final Map<UUID, HoldingState> holdingFire = new HashMap<>();
    private final Set<UUID> burstInProgress = new HashSet<>();  // 正在连发中的玩家
    private int currentTick = 0;

    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        final Player player = event.getPlayer();
        final ZombiesPlayer zombiesPlayer = new ZombiesPlayer(player);
        if (!zombiesPlayer.getWorld().isGameRunning() || !zombiesPlayer.isAlive()) {
            return;
        }
        final Weapon weapon = zombiesPlayer.getHeldWeapon();
        if (weapon == null || weapon.getData().shoot == null) {
            return;
        }
        holdingFire.put(player.getUniqueId(), new HoldingState(weapon.getSlot(), currentTick));
        tryFire(zombiesPlayer, weapon);
    }

    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        currentTick++;
        final Iterator<Map.Entry<UUID, HoldingState>> iterator = holdingFire.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<UUID, HoldingState> entry = iterator.next();
            final Player bukkit = Bukkit.getPlayer(entry.getKey());
            if (bukkit == null) {
                iterator.remove();
                continue;
            }
            final HoldingState state = entry.getValue();
            if (currentTick - state.lastActivityTick > HOLD_TIMEOUT_TICKS) {
                iterator.remove();
                continue;
            }
            final ZombiesPlayer player = new ZombiesPlayer(bukkit);
            if (!player.getWorld().isGameRunning() || !player.isAlive()) {
                iterator.remove();
                continue;
            }
            final Weapon weapon = player.getWeapon(state.weaponSlot);
            if (weapon == null || weapon.getData().shoot == null) {
                iterator.remove();
                continue;
            }
            if (bukkit.getInventory().getHeldItemSlot() != state.weaponSlot) {
                iterator.remove();
                continue;
            }
            final Component delayComponent = weapon.getComponent(Weapon.DELAY);
            if (delayComponent != null) {
                final int remainingDelay = delayComponent.get(DelayData.REMAINING_DELAY);
                if (remainingDelay > 0) {
                    continue;
                }
            }
            tryFire(player, weapon);
        }
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final UUID playerId = event.getPlayer().getUniqueId();
        holdingFire.remove(playerId);
        burstInProgress.remove(playerId);
    }

    @EventHandler
    private void onPlayerItemHeld(final PlayerItemHeldEvent event) {
        holdingFire.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onGameEnd(final GameEndEvent event) {
        for (final ZombiesPlayer player : event.getWorld().getPlayers()) {
            final UUID playerId = player.getBukkit().getUniqueId();
            holdingFire.remove(playerId);
            burstInProgress.remove(playerId);
        }
    }

    private void tryFire(final ZombiesPlayer player, final Weapon weapon) {
        final UUID playerId = player.getBukkit().getUniqueId();
        final BurstData burstData = weapon.getData().burst;
        if (burstData != null) {
            // 检查是否正在连发中
            if (burstInProgress.contains(playerId)) {
                return;
            }
            // 标记连发开始
            burstInProgress.add(playerId);
            // 连发模式：发射第一发，然后调度后续发射
            fireSingleShot(player, weapon, true);  // 第一发跳过延迟
            for (int i = 1; i < burstData.count(); i++) {
                final boolean isLastShot = (i == burstData.count() - 1);
                final int delay = burstData.interval() * i;
                Bukkit.getScheduler().runTaskLater(ZombiesPlugin.INSTANCE, () -> {
                    // 确保玩家仍然存活且武器仍然有效
                    if (!player.getWorld().isGameRunning() || !player.isAlive()) {
                        burstInProgress.remove(playerId);
                        return;
                    }
                    final Weapon currentWeapon = player.getWeapon(weapon.getSlot());
                    if (currentWeapon == null || currentWeapon.getData().shoot == null) {
                        burstInProgress.remove(playerId);
                        return;
                    }
                    fireSingleShot(player, currentWeapon, !isLastShot);
                    // 最后一发完成后解除连发标记
                    if (isLastShot) {
                        burstInProgress.remove(playerId);
                    }
                }, delay);
            }
        } else {
            // 普通模式
            fireSingleShot(player, weapon, false);
        }
    }

    private void fireSingleShot(final ZombiesPlayer player, final Weapon weapon, final boolean skipDelay) {
        final ShootData shootData = weapon.getData().shoot;
        final RandomGenerator rnd = ThreadLocalRandom.current();
        final List<WeaponShootEvent.Bullet> bulletsList = new ArrayList<>();
        for (int i = 0; i < shootData.bullets(); i++) {
            final float yaw = (float) (player.getBukkit().getYaw() +
                    (1 - shootData.precision()) * rnd.nextDouble() * 90 * (rnd.nextBoolean() ? 1 : -1));
            final float pitch = (float) (player.getBukkit().getPitch() +
                    (1 - shootData.precision()) * rnd.nextDouble() * 90 * (rnd.nextBoolean() ? 1 : -1));
            final Vector bulletDirection = VectorUtil.fromJawAndPitch(yaw, pitch);
            bulletsList.add(new WeaponShootEvent.Bullet(bulletDirection));
        }
        if (!new WeaponShootEvent(weapon, bulletsList, skipDelay).callEvent()) {
            return;
        }
        for (final WeaponShootEvent.Bullet bullet : bulletsList) {
            detectShotAtZombie(player, weapon, bullet);
        }
    }

    private void detectShotAtZombie(final ZombiesPlayer player, final Weapon weapon, final WeaponShootEvent.Bullet bullet) {
        final ShootData shootData = weapon.getData().shoot;
        final int maxTargets = shootData.piercing();
        final Set<Entity> hitEntities = new HashSet<>();
        hitEntities.add(player.getBukkit());

        final org.bukkit.World bukkitWorld = player.getWorld().getBukkit();
        final Vector direction = bullet.direction().clone();
        if (direction.lengthSquared() <= TRACE_EPSILON) {
            return;
        }
        direction.normalize();

        Location rayStart = player.getBukkit().getEyeLocation();
        final double maxDistance = 50;
        double remainingDistance = maxDistance;

        for (int i = 0; i < maxTargets && remainingDistance > 0; i++) {
            final double searchDistance = remainingDistance;
            final RayTraceResult entityRay = bukkitWorld.rayTraceEntities(
                    rayStart,
                    direction,
                    searchDistance,
                    entity -> !hitEntities.contains(entity) && !(entity instanceof Player)  // Exclude all players
            );
            final RayTraceResult blockRay = bukkitWorld.rayTraceBlocks(
                    Position.fine(rayStart),
                    direction,
                    searchDistance,
                    FluidCollisionMode.NEVER,
                    false,
                    this::isFullBlockCollider
            );

            final double entityDistance = rayDistance(rayStart, entityRay);
            final double blockDistance = rayDistance(rayStart, blockRay);

            // Nearest full block blocks the bullet before it can hit an entity.
            if (blockDistance <= entityDistance + TRACE_EPSILON) {
                break;
            }
            if (entityRay == null) {
                break;
            }

            final Entity entity = entityRay.getHitEntity();
            if (entity == null) {
                break;
            }
            hitEntities.add(entity);
            final Zombie zombie = new Zombie(entity);
            if (zombie.isZombie()) {
                // 判断是否爆头
                final Vector hitPos = entityRay.getHitPosition();
                final BoundingBox box = entity.getBoundingBox();
                final double headThreshold = box.getMinY() + (box.getHeight() * 0.75);
                final boolean isHeadshot = hitPos.getY() >= headThreshold;

                final PlayerAttackZombieEvent event = new PlayerAttackZombieEvent(weapon, zombie);
                event.setHeadshot(isHeadshot);
                Bukkit.getPluginManager().callEvent(event);
            }
            final double hitDistance = entityDistance;
            remainingDistance -= hitDistance;
            if (remainingDistance <= TRACE_EPSILON) {
                break;
            }
            rayStart = entityRay.getHitPosition().toLocation(bukkitWorld)
                    .add(direction.clone().multiply(STEP_EPSILON));
        }
    }

    private double rayDistance(final Location start, final RayTraceResult ray) {
        if (ray == null || ray.getHitPosition() == null) {
            return Double.POSITIVE_INFINITY;
        }
        return ray.getHitPosition().distance(start.toVector());
    }

    private boolean isFullBlockCollider(final Block block) {
        final Collection<BoundingBox> boxes = block.getCollisionShape().getBoundingBoxes();
        if (boxes.size() != 1) {
            return false;
        }
        final BoundingBox box = boxes.iterator().next();
        return nearlyEquals(box.getWidthX(), 1.0)
                && nearlyEquals(box.getWidthZ(), 1.0)
                && nearlyEquals(box.getHeight(), 1.0)
                && isBlockAligned(box.getMinX())
                && isBlockAligned(box.getMinY())
                && isBlockAligned(box.getMinZ());
    }

    private boolean isBlockAligned(final double value) {
        return nearlyEquals(value, Math.rint(value));
    }

    private boolean nearlyEquals(final double a, final double b) {
        return Math.abs(a - b) <= TRACE_EPSILON;
    }
}
