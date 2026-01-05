package io.lama06.zombies.system.player.revive;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.lama06.zombies.ZombiesPlugin;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Pose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public final class PlayerCorpseNPC implements Listener {

    public static CorpseData createCorpse(final Player deadPlayer, final Location location) {
        final ServerPlayer npc = createNPC(deadPlayer, location);

        // Spawn NPC for all online players
        for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            spawnNPCForPlayer(npc, onlinePlayer);
        }

        return new CorpseData(
                npc,
                location.clone(),
                deadPlayer.getUniqueId(),
                PlayerCorpse.TIME,
                PlayerCorpse.REVIVE_TIME
        );
    }

    private static ServerPlayer createNPC(final Player deadPlayer, final Location location) {
        final MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        final ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();

        // Create GameProfile with player's skin
        final GameProfile profile = new GameProfile(UUID.randomUUID(), deadPlayer.getName());
        final GameProfile realProfile = ((CraftPlayer) deadPlayer).getProfile();
        for (final Property prop : realProfile.getProperties().get("textures")) {
            profile.getProperties().put("textures", prop);
        }

        // Get client information from dead player
        final ClientInformation clientInfo = ((CraftPlayer) deadPlayer).getHandle().clientInformation();

        // Create the ServerPlayer
        final ServerPlayer npc = new ServerPlayer(server, level, profile, clientInfo);
        npc.setPos(location.getX(), location.getY(), location.getZ());
        npc.setYRot(location.getYaw());
        npc.setXRot(location.getPitch());

        // Set sleeping pose
        npc.setPose(Pose.SLEEPING);

        // Set up fake connection (prevents adding to player list)
        final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
        final CommonListenerCookie cookie = new CommonListenerCookie(profile, 0, clientInfo, false);
        npc.connection = new ServerGamePacketListenerImpl(server, connection, npc, cookie);

        return npc;
    }

    public static void spawnNPCForPlayer(final ServerPlayer npc, final Player viewer) {
        final ServerPlayer viewerHandle = ((CraftPlayer) viewer).getHandle();
        final var connection = viewerHandle.connection;

        // Add to player list (required for client to show skin)
        connection.send(new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                List.of(npc)
        ));

        // Spawn the entity
        connection.send(new ClientboundAddEntityPacket(npc, 0, npc.blockPosition()));

        // Send rotation
        connection.send(new ClientboundRotateHeadPacket(npc, (byte) ((npc.getYRot() * 256) / 360)));

        // Send metadata (pose, etc.)
        final List<?> entityData = npc.getEntityData().getNonDefaultValues();
        if (entityData != null && !entityData.isEmpty()) {
            connection.send(new ClientboundSetEntityDataPacket(npc.getId(), (List) entityData));
        }
    }

    public static void despawnCorpse(final CorpseData corpse) {
        final ServerPlayer npc = corpse.getNpc();
        if (npc != null) {
            for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                despawnNPCForPlayer(npc, onlinePlayer);
            }
        }
    }

    public static void despawnNPCForPlayer(final ServerPlayer npc, final Player viewer) {
        final ServerPlayer viewerHandle = ((CraftPlayer) viewer).getHandle();
        final var connection = viewerHandle.connection;

        // Remove entity
        connection.send(new ClientboundRemoveEntitiesPacket(npc.getId()));

        // Remove from player list
        connection.send(new ClientboundPlayerInfoRemovePacket(List.of(npc.getUUID())));
    }

    // Handle new players joining - spawn existing corpses for them
    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // Delay by 1 tick to ensure player is fully loaded
        Bukkit.getScheduler().runTaskLater(ZombiesPlugin.INSTANCE, () -> {
            for (final CorpseData corpse : ZombiesPlugin.INSTANCE.getCorpses()) {
                spawnNPCForPlayer(corpse.getNpc(), player);
            }
        }, 1L);
    }

    public static CorpseData getCorpseByEntityId(final int entityId) {
        for (final CorpseData corpse : ZombiesPlugin.INSTANCE.getCorpses()) {
            if (corpse.getNpc() != null && corpse.getNpc().getId() == entityId) {
                return corpse;
            }
        }
        return null;
    }
}
