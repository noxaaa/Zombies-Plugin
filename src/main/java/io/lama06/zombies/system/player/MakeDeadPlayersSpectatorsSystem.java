package io.lama06.zombies.system.player;

import org.bukkit.event.Listener;

/**
 * Previously handled PlayerDeathEvent.
 * Now death is intercepted at EntityDamageEvent level in SpawnPlayerCorpseSystem
 * to prevent the client from showing the death screen.
 */
public final class MakeDeadPlayersSpectatorsSystem implements Listener {
    // No longer needed - death is now handled by intercepting fatal damage
    // in SpawnPlayerCorpseSystem before the player actually dies
}
