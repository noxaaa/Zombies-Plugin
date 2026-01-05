package io.lama06.zombies;

import io.lama06.zombies.system.player.revive.CorpseData;
import io.lama06.zombies.weapon.Weapon;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ZombiesPlugin extends JavaPlugin implements Listener {
    public static ZombiesPlugin INSTANCE;

    private ConfigManager configManager;
    private final Map<UUID, CorpseData> corpses = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        INSTANCE = this;

        configManager = new ConfigManager(this);
        try {
            configManager.backupConfig();
        } catch (final IOException e) {
            getSLF4JLogger().error("failed to backup the config file", e);
        }
        try {
            configManager.loadConfig();
        } catch (final IOException e) {
            getSLF4JLogger().error("failed to load the config file", e);
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        for (final Listener system : Systems.SYSTEMS) {
            Bukkit.getPluginManager().registerEvents(system, this);
        }

        final PluginCommand zombiesCommand = getCommand("zombies");
        if (zombiesCommand == null) {
            throw new IllegalStateException();
        }
        final ZombiesCommandExecutor zombiesCommandExecutor = new ZombiesCommandExecutor();
        zombiesCommand.setExecutor(zombiesCommandExecutor);
        zombiesCommand.setTabCompleter(zombiesCommandExecutor);
    }

    @Override
    public void onDisable() {
        try {
            configManager.saveConfig();
        } catch (final IOException e) {
            getSLF4JLogger().error("failed to save the config file", e);
        }
    }

    public WorldConfig getWorldConfig(final ZombiesWorld world) {
        return getGlobalConfig().worlds.get(world.getBukkit().getName());
    }

    public ZombiesConfig getGlobalConfig() {
        final ZombiesConfig config = configManager.getConfig();
        if (config == null) {
            return new ZombiesConfig();
        }
        return config;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isZombiesWorld(final ZombiesWorld world) {
        return getWorldConfig(world) != null;
    }

    public List<ZombiesWorld> getWorlds() {
        return Bukkit.getWorlds().stream().map(ZombiesWorld::new).filter(ZombiesWorld::isZombiesWorld).toList();
    }

    public List<ZombiesWorld> getGameWorlds() {
        return getWorlds().stream().filter(ZombiesWorld::isGameRunning).toList();
    }

    public List<ZombiesPlayer> getAlivePlayers() {
        return getGameWorlds().stream().map(ZombiesWorld::getAlivePlayers).flatMap(Collection::stream).toList();
    }

    public List<Weapon> getWeapons() {
        return getAlivePlayers().stream().map(ZombiesPlayer::getWeapons).flatMap(Collection::stream).toList();
    }

    public List<Zombie> getZombies() {
        return getGameWorlds().stream().map(ZombiesWorld::getZombies).flatMap(Collection::stream).toList();
    }

    // Corpse management
    public void addCorpse(final UUID deadPlayerUUID, final CorpseData corpse) {
        corpses.put(deadPlayerUUID, corpse);
    }

    public void removeCorpse(final UUID deadPlayerUUID) {
        corpses.remove(deadPlayerUUID);
    }

    public CorpseData getCorpse(final UUID deadPlayerUUID) {
        return corpses.get(deadPlayerUUID);
    }

    public Collection<CorpseData> getCorpses() {
        return corpses.values();
    }

    public CorpseData getCorpseByArmorStand(final org.bukkit.entity.ArmorStand armorStand) {
        for (final CorpseData corpse : corpses.values()) {
            if (corpse.getArmorStand() != null && corpse.getArmorStand().equals(armorStand)) {
                return corpse;
            }
        }
        return null;
    }

    public void clearCorpses() {
        corpses.clear();
    }
}
