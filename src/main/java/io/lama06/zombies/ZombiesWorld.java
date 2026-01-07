package io.lama06.zombies;

import io.lama06.zombies.data.*;
import io.lama06.zombies.event.GameEndEvent;
import io.lama06.zombies.event.GameStartEvent;
import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.perk.GlobalPerk;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class ZombiesWorld extends Storage implements ForwardingAudience {
    public static final AttributeId<Boolean> GAME_RUNNING = new AttributeId<>("game_running", PersistentDataType.BOOLEAN);

    public static final AttributeId<Integer> START_TIMER = new AttributeId<>("start_timer", PersistentDataType.INTEGER);
    public static final AttributeId<Integer> GAME_ID = new AttributeId<>("game_id", PersistentDataType.INTEGER);
    public static final AttributeId<Integer> ROUND = new AttributeId<>("round", PersistentDataType.INTEGER);
    public static final AttributeId<Integer> REMAINING_ZOMBIES = new AttributeId<>("remaining_zombies", PersistentDataType.INTEGER);
    public static final AttributeId<Integer> NEXT_ZOMBIE_TIME = new AttributeId<>("next_zombie", PersistentDataType.INTEGER);
    public static final AttributeId<Boolean> BOSS_SPAWNED = new AttributeId<>("boss_spawned", PersistentDataType.BOOLEAN);
    public static final AttributeId<Boolean> POWER_SWITCH = new AttributeId<>("power_switch", PersistentDataType.BOOLEAN);
    public static final AttributeId<List<String>> REACHABLE_AREAS = new AttributeId<>("reachable_areas", PersistentDataType.LIST.strings());
    public static final AttributeId<List<Integer>> OPEN_DOORS = new AttributeId<>("open_doors", PersistentDataType.LIST.integers());
    public static final AttributeId<Integer> DRAGONS_WRATH_USED = new AttributeId<>("dragons_wrath_used", PersistentDataType.INTEGER);
    public static final AttributeId<Long> ROUND_START_TIME = new AttributeId<>("round_start_time", PersistentDataType.LONG);
    public static final AttributeId<Integer> TRIGGERED_WAVES = new AttributeId<>("triggered_waves", PersistentDataType.INTEGER);
    public static final AttributeId<Integer> ACTIVE_LUCKY_CHEST = new AttributeId<>("active_lucky_chest", PersistentDataType.INTEGER);
    public static final AttributeId<Integer> LUCKY_CHEST_USES = new AttributeId<>("lucky_chest_uses", PersistentDataType.INTEGER);
    public static final AttributeId<List<String>> DROPPED_PERKS = new AttributeId<>("dropped_perks", PersistentDataType.LIST.strings());

    public static final ComponentId PERKS_COMPONENT = new ComponentId("perks");

    private final World world;

    public ZombiesWorld(final World world) {
        this.world = world;
    }

    public boolean isGameRunning() {
        return Objects.requireNonNullElse(get(GAME_RUNNING), false);
    }

    public boolean isZombiesWorld() {
        return ZombiesPlugin.INSTANCE.isZombiesWorld(this);
    }

    public WorldConfig getConfig() {
        return ZombiesPlugin.INSTANCE.getWorldConfig(this);
    }

    public void startGame() {
        set(GAME_RUNNING, true);
        Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
    }

    public void endGame() {
        endGame(false);
    }

    public void endGame(final boolean victory) {
        set(GAME_RUNNING, false);
        Bukkit.getPluginManager().callEvent(new GameEndEvent(this, victory));
    }

    public List<Zombie> getZombies() {
        return world.getEntities().stream().map(Zombie::new).filter(Zombie::isZombie).toList();
    }

    public Zombie spawnZombie(final Location location, final ZombieType type) {
        return spawnZombie(location, type, 0, 0, 0);
    }

    public Zombie spawnZombie(final Location location, final ZombieType type, final int healthOverride) {
        return spawnZombie(location, type, healthOverride, 0, 0);
    }

    public Zombie spawnZombie(final Location location, final ZombieType type, final int healthOverride,
                              final double speedOverride, final double damageOverride) {
        final Entity entity = world.spawnEntity(location, type.data.entity, false);
        if (type.data.initializer != null) {
            type.data.initializer.accept(entity);
        }
        if (entity instanceof final LivingEntity living) {
            living.setRemoveWhenFarAway(false);
        }
        final Zombie zombie = new Zombie(entity);
        zombie.set(Zombie.IS_ZOMBIE, true);
        zombie.set(Zombie.TYPE, type);
        Bukkit.getPluginManager().callEvent(new ZombieSpawnEvent(zombie, type.data, healthOverride, speedOverride, damageOverride));
        return zombie;
    }

    public List<ZombiesPlayer> getAlivePlayers() {
        return world.getPlayers().stream()
                .filter(player -> player.getGameMode() == GameMode.ADVENTURE)
                .filter(player -> ZombiesPlugin.INSTANCE.getCorpse(player.getUniqueId()) == null) // Exclude downed players
                .map(ZombiesPlayer::new)
                .toList();
    }

    public List<ZombiesPlayer> getPlayers() {
        return world.getPlayers().stream().map(ZombiesPlayer::new).toList();
    }

    public boolean isPerkEnabled(final GlobalPerk perk) {
        final Component perksComponent = getComponent(PERKS_COMPONENT);
        if (perksComponent == null) {
            return false;
        }
        return perksComponent.get(perk.getRemainingTimeAttribute()) != 0;
    }

    public boolean hasPerkDropped(final GlobalPerk perk) {
        final List<String> droppedPerks = get(DROPPED_PERKS);
        if (droppedPerks == null) {
            return false;
        }
        return droppedPerks.contains(perk.name());
    }

    public void markPerkAsDropped(final GlobalPerk perk) {
        List<String> droppedPerks = get(DROPPED_PERKS);
        if (droppedPerks == null) {
            droppedPerks = new ArrayList<>();
        } else {
            droppedPerks = new ArrayList<>(droppedPerks);
        }
        if (!droppedPerks.contains(perk.name())) {
            droppedPerks.add(perk.name());
            set(DROPPED_PERKS, droppedPerks);
        }
    }

    public List<Window> getAvailableWindows() {
        final WorldConfig config = getConfig();
        if (config == null) {
            return List.of();
        }
        final List<String> areas = get(REACHABLE_AREAS);
        return config.windows.stream().filter(window -> areas.contains(window.area)).toList();
    }

    public String getAreaAt(final Location loc) {
        final WorldConfig config = getConfig();
        if (config == null) {
            return null;
        }
        for (final Area area : config.areas) {
            if (area.contains(loc)) {
                return area.name;
            }
        }
        return null;
    }

    public String getPlayerArea(final ZombiesPlayer player) {
        return getAreaAt(player.getBukkit().getLocation());
    }

    public Location getNextZombieSpawnPoint() {
        final RandomGenerator rnd = ThreadLocalRandom.current();
        final List<ZombiesPlayer> players = getPlayers();
        if (players.isEmpty()) {
            return null;
        }
        final ZombiesPlayer player = players.get(rnd.nextInt(players.size()));
        final Window window = getWindowForSpawn(player);
        if (window == null) {
            return null;
        }
        return window.spawnLocation.toBukkit(getBukkit());
    }

    private Window getWindowForSpawn(final ZombiesPlayer player) {
        final List<Window> available = getAvailableWindows();
        if (available.isEmpty()) {
            return null;
        }
        final WorldConfig config = getConfig();
        final double spawnRange = config != null ? config.spawnRange : 0;
        final RandomGenerator rnd = ThreadLocalRandom.current();

        if (spawnRange <= 0) {
            // Unlimited - random from all available windows
            return available.get(rnd.nextInt(available.size()));
        }

        // Filter windows within range
        final Vector playerVector = player.getBukkit().getLocation().toVector();
        final List<Window> candidates = available.stream()
            .filter(window -> {
                final Vector windowVector = window.spawnLocation.toBukkit(player.getBukkit().getWorld()).toVector();
                final double xDiff = windowVector.getX() - playerVector.getX();
                final double zDiff = windowVector.getZ() - playerVector.getZ();
                final double yDiff = (windowVector.getY() - playerVector.getY()) * 6;
                final double distance = Math.sqrt(xDiff*xDiff + yDiff*yDiff + zDiff*zDiff);
                return distance <= spawnRange;
            })
            .toList();

        if (candidates.isEmpty()) {
            // No windows in range, use all available
            return available.get(rnd.nextInt(available.size()));
        }
        return candidates.get(rnd.nextInt(candidates.size()));
    }

    @Override
    protected StorageSession startSession() {
        return world::getPersistentDataContainer;
    }

    @Override
    public Iterable<? extends Audience> audiences() {
        return Set.of(world);
    }

    public World getBukkit() {
        return world;
    }
}
