package io.lama06.zombies.system;

import io.lama06.zombies.*;
import io.lama06.zombies.data.Component;
import io.lama06.zombies.event.GameStartEvent;
import io.lama06.zombies.event.StartRoundEvent;
import io.lama06.zombies.perk.GlobalPerk;
import io.lama06.zombies.weapon.WeaponType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.List;

public final class PrepareWorldAtGameStartSystem implements Listener {
    @EventHandler
    private void onGameStart(final GameStartEvent event) {
        final ZombiesWorld world = event.getWorld();
        final WorldConfig config = world.getConfig();
        final Integer lastGameId = world.get(ZombiesWorld.GAME_ID);
        final int gameId = lastGameId == null ? 1 : lastGameId + 1;

        world.getBukkit().setGameRule(GameRule.DO_FIRE_TICK, false);
        world.getBukkit().setGameRule(GameRule.MOB_GRIEFING, false);
        world.getBukkit().setGameRule(GameRule.DISABLE_RAIDS, true);

        world.set(ZombiesWorld.GAME_ID, gameId);
        world.set(ZombiesWorld.ROUND, 1);
        world.set(ZombiesWorld.OPEN_DOORS, List.of());
        world.set(ZombiesWorld.REACHABLE_AREAS, List.of(config.startArea));
        world.set(ZombiesWorld.POWER_SWITCH, false);
        world.set(ZombiesWorld.ROUND_START_TIME, world.getBukkit().getGameTime());
        world.set(ZombiesWorld.TRIGGERED_WAVES, 0);
        world.set(ZombiesWorld.DRAGONS_WRATH_USED, 0);

        final Component perksComponent = world.addComponent(ZombiesWorld.PERKS_COMPONENT);
        for (final GlobalPerk perk : GlobalPerk.values()) {
            perksComponent.set(perk.getRemainingTimeAttribute(), 0);
        }

        for (final Window window : config.windows) {
            window.close(event.getWorld());
        }
        for (final Door door : config.doors) {
            door.setOpen(world, false);
        }
        if (config.powerSwitch != null) {
            config.powerSwitch.setActive(world, false);
        }

        for (final ZombiesPlayer player : world.getPlayers()) {
            player.set(ZombiesPlayer.GAME_ID, gameId);
            player.set(ZombiesPlayer.KILLS, 0);
            player.set(ZombiesPlayer.GOLD, 0);
            final Player bukkit = player.getBukkit();
            bukkit.getInventory().clear();
            bukkit.teleport(world.getBukkit().getSpawnLocation());
            bukkit.setFoodLevel(20);
            bukkit.setHealth(20);
            bukkit.setGameMode(GameMode.ADVENTURE);
            bukkit.setLevel(0);
            bukkit.setExp(0);
            bukkit.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));
            player.giveWeapon(0, WeaponType.KNIFE);
            player.giveWeapon(1, WeaponType.PISTOL);
        }

        // 显示回合1的 Title 和音效
        final Title title = Title.title(
                net.kyori.adventure.text.Component.text("Round").color(NamedTextColor.RED),
                net.kyori.adventure.text.Component.text("1").color(NamedTextColor.RED),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500))
        );
        for (final ZombiesPlayer player : world.getPlayers()) {
            player.getBukkit().showTitle(title);
            player.getBukkit().playSound(player.getBukkit().getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
        }

        Bukkit.getPluginManager().callEvent(new StartRoundEvent(world, 1));
    }
}
