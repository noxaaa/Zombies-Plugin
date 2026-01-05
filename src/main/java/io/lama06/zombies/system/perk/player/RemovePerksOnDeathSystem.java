package io.lama06.zombies.system.perk.player;

import org.bukkit.event.Listener;

/**
 * Previously removed perks on death.
 * Now perks are only removed when the downed player times out (25 seconds without rescue).
 * This is handled by TickPlayerCorpseSystem instead.
 */
public final class RemovePerksOnDeathSystem implements Listener {
    // Perks are no longer removed on initial death (downed state)
    // Perks are only removed when the 25-second timer expires (true death)
    // See TickPlayerCorpseSystem for the implementation
}
