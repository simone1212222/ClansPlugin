package com.develop.clansPlugin.managers;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.logging.PluginLogger;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import com.develop.clansPlugin.models.ClanRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.sql.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class ClanManager {

    private final ClansPlugin plugin;
    private final PluginLogger logger;
    private final Map<Integer, Clan> clanCache;
    private final Map<UUID, Integer> playerClanCache;
    private final Map<String, Integer> clanNameCache;
    private final Map<String, Integer> clanTagCache;

    public ClanManager(ClansPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
        this.clanCache = new ConcurrentHashMap<>();
        this.playerClanCache = new ConcurrentHashMap<>();
        this.clanNameCache = new ConcurrentHashMap<>();
        this.clanTagCache = new ConcurrentHashMap<>();

        loadClansAsync();
    }

    private void loadClansAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                loadAllClans();
                logger.info("Caricati " + clanCache.size() + " clans dal database");
            } catch (SQLException e) {
                logger.error("Errore durante il caricamento dei clan", e);
            }
        });
    }

    private void loadAllClans() throws SQLException {
        String query = """
            SELECT c.*, cm.id as member_id, cm.player_uuid, cm.player_name, cm.role, cm.joined_at
            FROM clans c
            LEFT JOIN clan_members cm ON c.id = cm.clan_id
            ORDER BY c.id, cm.id
            """;

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Map<Integer, Clan> clans = new HashMap<>();

            while (rs.next()) {
                int clanId = rs.getInt("id");

                // Crea il clan se non esiste
                if (!clans.containsKey(clanId)) {
                    Clan clan = new Clan(
                            clanId,
                            rs.getString("name"),
                            rs.getString("tag"),
                            UUID.fromString(rs.getString("leader_uuid")),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );

                    // Set home se esiste
                    String homeWorld = rs.getString("home_world");
                    if (homeWorld != null) {
                        Location home = new Location(
                                plugin.getServer().getWorld(homeWorld),
                                rs.getDouble("home_x"),
                                rs.getDouble("home_y"),
                                rs.getDouble("home_z"),
                                rs.getFloat("home_yaw"),
                                rs.getFloat("home_pitch")
                        );
                        clan.setHome(home);
                    }

                    clans.put(clanId, clan);
                }

                // Aggiungi i membri se esiste
                int memberId = rs.getInt("member_id");
                if (memberId > 0) {
                    ClanMember member = new ClanMember(
                            memberId,
                            clanId,
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("player_name"),
                            ClanRole.fromString(rs.getString("role")),
                            rs.getTimestamp("joined_at").toLocalDateTime()
                    );
                    clans.get(clanId).addMember(member);
                }
            }

            // Aggiorna la cache
            clanCache.clear();
            playerClanCache.clear();
            clanNameCache.clear();
            clanTagCache.clear();

            for (Clan clan : clans.values()) {
                clanCache.put(clan.getId(), clan);
                clanNameCache.put(clan.getName().toLowerCase(), clan.getId());
                clanTagCache.put(clan.getTag().toLowerCase(), clan.getId());

                for (ClanMember member : clan.getMembers().values()) {
                    playerClanCache.put(member.getPlayerUuid(), clan.getId());
                }
            }
        }
    }

    public CompletableFuture<Clan> createClan(String name, String tag, Player leader) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Verifica se esiste: il nome, il tag e il leader del clan
                if (clanNameExists(name)) {
                    return null;
                }
                if (clanTagExists(tag)) {
                    return null;
                }
                if (getPlayerClan(leader.getUniqueId()) != null) {
                    return null;
                }

                String insertClanQuery = """
                    INSERT INTO clans (name, tag, leader_uuid) VALUES (?, ?, ?)
                    """;

                String insertMemberQuery = """
                    INSERT INTO clan_members (clan_id, player_uuid, player_name, role) VALUES (?, ?, ?, 'LEADER')
                    """;

                try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                    conn.setAutoCommit(false);

                    int clanId;
                    try (PreparedStatement stmt = conn.prepareStatement(insertClanQuery, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, name);
                        stmt.setString(2, tag);
                        stmt.setString(3, leader.getUniqueId().toString());
                        stmt.executeUpdate();

                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            if (keys.next()) {
                                clanId = keys.getInt(1);
                            } else {
                                conn.rollback();
                                return null;
                            }
                        }
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(insertMemberQuery)) {
                        stmt.setInt(1, clanId);
                        stmt.setString(2, leader.getUniqueId().toString());
                        stmt.setString(3, leader.getName());
                        stmt.executeUpdate();
                    }

                    conn.commit();

                    // Create clan object and update cache
                    Clan clan = new Clan(clanId, name, tag, leader.getUniqueId(), LocalDateTime.now());
                    ClanMember leaderMember = new ClanMember(
                            0, clanId, leader.getUniqueId(), leader.getName(),
                            ClanRole.LEADER, LocalDateTime.now()
                    );
                    clan.addMember(leaderMember);

                    clanCache.put(clanId, clan);
                    clanNameCache.put(name.toLowerCase(), clanId);
                    clanTagCache.put(tag.toLowerCase(), clanId);
                    playerClanCache.put(leader.getUniqueId(), clanId);

                    plugin.getSettingsManager().createDefaultSettings(clanId, leader.getUniqueId());

                    return clan;

                }
            } catch (Exception e) {
                logger.error("Errore nella creazione del clan", e);
                return null;
            }
        });
    }

    public CompletableFuture<Boolean> disbandClan(int clanId) {
        // Rimuove completamente il clan dal DB e aggiorna le cache interne
        return CompletableFuture.supplyAsync(() -> {
            try {
                String query = "DELETE FROM clans WHERE id = ?";

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setInt(1, clanId);
                    boolean success = stmt.executeUpdate() > 0;

                    if (success) {
                        Clan clan = clanCache.remove(clanId);
                        if (clan != null) {
                            plugin.getTerritoryManager().removeAllClanTerritories(clanId);
                            plugin.getSettingsManager().removeClanSettings(clanId);
                            clanNameCache.remove(clan.getName().toLowerCase()   );
                            clanTagCache.remove(clan.getTag().toLowerCase());
                            for (ClanMember member : clan.getMembers().values()) {
                                playerClanCache.remove(member.getPlayerUuid());
                            }
                        }
                    }

                    return success;
                }

            } catch (SQLException e) {
                logger.error("Errore nello scioglimento del clan", e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> setHome(int clanId, Location home) {
        // Aggiorna la home nel DB e aggiorna la cache interna
        return CompletableFuture.supplyAsync(() -> {
            try {
                String updateQuery = """
                    UPDATE clans SET home_world = ?, home_x = ?, home_y = ?, home_z = ?, home_yaw = ?, home_pitch = ?
                    WHERE id = ?
                    """;

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

                    stmt.setString(1, Objects.requireNonNull(home.getWorld()).getName());
                    stmt.setDouble(2, home.getX());
                    stmt.setDouble(3, home.getY());
                    stmt.setDouble(4, home.getZ());
                    stmt.setFloat(5, home.getYaw());
                    stmt.setFloat(6, home.getPitch());
                    stmt.setInt(7, clanId);

                    boolean successs = stmt.executeUpdate() > 0;

                    if (successs) {
                        Clan clan = clanCache.get(clanId);
                        if (clan != null) {
                            clan.setHome(home);
                        }
                    }
                    return successs;
                }
            } catch (SQLException e) {
                logger.error("Errore durante l'aggiornamento della home del clan", e);
                return false;
            }
        });
    }

    public void reloadClan(int clanId) {
        // Ricarica i dati del clan dal DB e aggiorna la cache interna
        CompletableFuture.runAsync(() -> {
            try {
                String query = """
                    SELECT c.*, cm.id as member_id, cm.player_uuid, cm.player_name, cm.role, cm.joined_at
                    FROM clans c
                    LEFT JOIN clan_members cm ON c.id = cm.clan_id
                    WHERE c.id = ?
                    ORDER BY cm.id
                    """;

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setInt(1, clanId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        Clan clan = null;

                        while (rs.next()) {
                            if (clan == null) {
                                // Remove old clan from caches
                                Clan oldClan = clanCache.get(clanId);
                                if (oldClan != null) {
                                    clanNameCache.remove(oldClan.getName().toLowerCase());
                                    clanTagCache.remove(oldClan.getTag().toLowerCase());
                                    for (ClanMember member : oldClan.getMembers().values()) {
                                        playerClanCache.remove(member.getPlayerUuid());
                                    }
                                }

                                clan = new Clan(
                                        clanId,
                                        rs.getString("name"),
                                        rs.getString("tag"),
                                        UUID.fromString(rs.getString("leader_uuid")),
                                        rs.getTimestamp("created_at").toLocalDateTime()
                                );

                                String homeWorld = rs.getString("home_world");
                                if (homeWorld != null) {
                                    Location home = new Location(
                                            plugin.getServer().getWorld(homeWorld),
                                            rs.getDouble("home_x"),
                                            rs.getDouble("home_y"),
                                            rs.getDouble("home_z"),
                                            rs.getFloat("home_yaw"),
                                            rs.getFloat("home_pitch")
                                    );
                                    clan.setHome(home);
                                }

                                clanCache.put(clanId, clan);
                                clanNameCache.put(clan.getName().toLowerCase(), clanId);
                                clanTagCache.put(clan.getTag().toLowerCase(), clanId);
                            }

                            int memberId = rs.getInt("member_id");
                            if (memberId > 0) {
                                ClanMember member = new ClanMember(
                                        memberId,
                                        clanId,
                                        UUID.fromString(rs.getString("player_uuid")),
                                        rs.getString("player_name"),
                                        ClanRole.fromString(rs.getString("role")),
                                        rs.getTimestamp("joined_at").toLocalDateTime()
                                );
                                clan.addMember(member);
                                playerClanCache.put(member.getPlayerUuid(), clanId);
                            }
                        }
                    }
                }

            } catch (SQLException e) {
                logger.error("Errore durante il caricamento del clan: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Boolean> kickMember(int clanId, UUID playerUuid) {
        // Rimuove il membro dal DB e aggiorna le cache interne
        return CompletableFuture.supplyAsync(() -> {
            try {
                String deleteQuery = "DELETE FROM clan_members WHERE clan_id = ? AND player_uuid = ?";

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

                    stmt.setInt(1, clanId);
                    stmt.setString(2, playerUuid.toString());
                    boolean success = stmt.executeUpdate() > 0;

                    if (success) {
                        // Update cache
                        Clan clan = clanCache.get(clanId);
                        if (clan != null) {
                            clan.removeMember(playerUuid);
                            playerClanCache.remove(playerUuid);
                        }
                    }

                    return success;
                }

            } catch (SQLException e) {
                logger.error("Errore durante il kick di un membro dal clan: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> promoteMember(int clanId, UUID playerUuid, ClanRole newRole) {
        // Aggiorna il ruolo del membro nel database e nella cache
        return CompletableFuture.supplyAsync(() -> {
            try {
                String updateQuery = "UPDATE clan_members SET role = ? WHERE clan_id = ? AND player_uuid = ?";

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

                    stmt.setString(1, newRole.name());
                    stmt.setInt(2, clanId);
                    stmt.setString(3, playerUuid.toString());
                    boolean success = stmt.executeUpdate() > 0;

                    if (success) {
                        // Update cache
                        Clan clan = clanCache.get(clanId);
                        if (clan != null) {
                            ClanMember member = clan.getMember(playerUuid);
                            if (member != null) {
                                member.setRole(newRole);
                            }
                        }
                    }

                    return success;
                }

            } catch (SQLException e) {
                logger.error("Errore durante la promozione di un membro: " + e.getMessage());
                return false;
            }
        });
    }


    // Getter methods
    public Clan getClan(int clanId) {
        return clanCache.get(clanId);
    }

    public Clan getClanByName(String name) {
        Integer clanId = clanNameCache.get(name.toLowerCase());
        return clanId != null ? clanCache.get(clanId) : null;
    }

    public Clan getPlayerClan(UUID playerUuid) {
        Integer clanId = playerClanCache.get(playerUuid);
        return clanId != null ? clanCache.get(clanId) : null;
    }

    public boolean clanNameExists(String name) {
        return clanNameCache.containsKey(name.toLowerCase());
    }

    public boolean clanTagExists(String tag) {
        return clanTagCache.containsKey(tag.toLowerCase());
    }

    public Collection<Clan> getAllClans() {
        return new ArrayList<>(clanCache.values());
    }

}