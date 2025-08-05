package com.develop.clansPlugin.managers;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.logging.PluginLogger;
import com.develop.clansPlugin.models.ClanInvite;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class InviteManager {

    private final ClansPlugin plugin;
    private final PluginLogger logger;
    private final Map<UUID, List<ClanInvite>> playerInvites;
    private final Map<Integer, ClanInvite> inviteCache;

    public InviteManager(ClansPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
        this.playerInvites = new ConcurrentHashMap<>();
        this.inviteCache = new ConcurrentHashMap<>();

        startCleanupTask();
    }

    public CompletableFuture<ClanInvite> createInvite(int clanId, UUID inviterUuid, UUID invitedUuid, String invitedName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (hasActiveInvite(clanId, invitedUuid)) {
                    return null;
                }

                LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10); // 10 minute expiry

                String insertQuery = """
                    INSERT INTO clan_invites (clan_id, inviter_uuid, invited_uuid, invited_name, expires_at)
                    VALUES (?, ?, ?, ?, ?)
                    """;

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

                    stmt.setInt(1, clanId);
                    stmt.setString(2, inviterUuid.toString());
                    stmt.setString(3, invitedUuid.toString());
                    stmt.setString(4, invitedName);
                    stmt.setTimestamp(5, Timestamp.valueOf(expiresAt));

                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            int inviteId = keys.getInt(1);

                            ClanInvite invite = new ClanInvite(
                                    inviteId, clanId, inviterUuid, invitedUuid,
                                    invitedName, LocalDateTime.now(), expiresAt
                            );

                            // Update cache
                            inviteCache.put(inviteId, invite);
                            playerInvites.computeIfAbsent(invitedUuid, k -> new ArrayList<>()).add(invite);

                            return invite;
                        }
                    }
                }

                return null;

            } catch (SQLException e) {
                logger.error("Errore durante la creazione dell'invito: " + e.getMessage());
                return null;
            }
        });
    }

    public CompletableFuture<Boolean> acceptInvite(UUID playerUuid, int clanId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ClanInvite invite = getActiveInvite(playerUuid, clanId);
                if (invite == null || invite.isExpired()) {
                    return false;
                }

                String insertMemberQuery = """
                    INSERT INTO clan_members (clan_id, player_uuid, player_name, role)
                    VALUES (?, ?, ?, 'MEMBER')
                    """;

                String deleteInviteQuery = "DELETE FROM clan_invites WHERE id = ?";

                try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                    conn.setAutoCommit(false);

                    try (PreparedStatement stmt = conn.prepareStatement(insertMemberQuery)) {
                        stmt.setInt(1, clanId);
                        stmt.setString(2, playerUuid.toString());
                        stmt.setString(3, invite.getInvitedName());
                        stmt.executeUpdate();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(deleteInviteQuery)) {
                        stmt.setInt(1, invite.getId());
                        stmt.executeUpdate();
                    }

                    conn.commit();

                    // Update caches
                    removeInviteFromCache(invite);

                    // Reload clan data
                    plugin.getClanManager().reloadClan(clanId);

                    return true;

                } catch (SQLException e) {
                    logger.error("Errore durante l'accettazione del invito: " + e.getMessage());
                    return false;
                }

            } catch (Exception e) {
                logger.error("Errore durante l'accettazione del invito: " + e.getMessage());
                return false;
            }
        });
    }

    public List<ClanInvite> getPlayerInvites(UUID playerUuid) {
        return playerInvites.getOrDefault(playerUuid, new ArrayList<>())
                .stream()
                .filter(invite -> !invite.isExpired())
                .toList();
    }

    public ClanInvite getActiveInvite(UUID playerUuid, int clanId) {
        return getPlayerInvites(playerUuid).stream()
                .filter(invite -> invite.getClanId() == clanId)
                .findFirst()
                .orElse(null);
    }

    public boolean hasActiveInvite(int clanId, UUID playerUuid) {
        return getActiveInvite(playerUuid, clanId) != null;
    }

    private void removeInviteFromCache(ClanInvite invite) {
        inviteCache.remove(invite.getId());
        List<ClanInvite> invites = playerInvites.get(invite.getInvitedUuid());
        if (invites != null) {
            invites.remove(invite);
            if (invites.isEmpty()) {
                playerInvites.remove(invite.getInvitedUuid());
            }
        }
    }

    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                cleanupExpiredInvites();
            } catch (Exception e) {
                logger.warn("Errore durante la pulizia dei inviti: " + e.getMessage());
            }
        }, 20 * 60, 20 * 60);
    }

    private void cleanupExpiredInvites() {
        try {
            String deleteQuery = "DELETE FROM clan_invites WHERE expires_at < NOW()";

            try (Connection conn = plugin.getDatabaseManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

                int deleted = stmt.executeUpdate();
                if (deleted > 0) {
                    logger.info("Puliti " + deleted + " inviti scaduti");

                    inviteCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
                    playerInvites.values().forEach(list -> list.removeIf(ClanInvite::isExpired));
                    playerInvites.entrySet().removeIf(entry -> entry.getValue().isEmpty());
                }
            }

        } catch (SQLException e) {
            logger.error("Errore durante la pulizia dei inviti: " + e.getMessage());
        }
    }

    public void shutdown() {
        inviteCache.clear();
        playerInvites.clear();
    }
}