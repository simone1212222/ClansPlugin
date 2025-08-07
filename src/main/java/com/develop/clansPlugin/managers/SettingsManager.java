package com.develop.clansPlugin.managers;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.logging.PluginLogger;
import com.develop.clansPlugin.models.ClanSettings;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsManager {

    private final ClansPlugin plugin;
    private final PluginLogger logger;
    private final Map<Integer, ClanSettings> settingsCache;

    public SettingsManager(ClansPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
        this.settingsCache = new ConcurrentHashMap<>();

        loadSettingsAsync();
    }

    private void loadSettingsAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                loadAllSettings();
                logger.info("Caricati " + settingsCache.size() + " clan settings dal database");
            } catch (SQLException e) {
                logger.error("Errore durante il caricamento degli settings dei clan: " + e.getMessage());
            }
        });
    }

    private void loadAllSettings() throws SQLException {
        String query = "SELECT * FROM clan_settings";

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            settingsCache.clear();

            while (rs.next()) {
                ClanSettings settings = new ClanSettings(
                        rs.getInt("clan_id"),
                        rs.getBoolean("allow_build"),
                        rs.getBoolean("allow_pvp"),
                        rs.getBoolean("allow_mob_spawning"),
                        rs.getTimestamp("updated_at").toLocalDateTime(),
                        UUID.fromString(rs.getString("updated_by"))
                );

                settingsCache.put(settings.getClanId(), settings);
            }
        }
    }

    public CompletableFuture<Boolean> updateSetting(int clanId, String setting, boolean value, UUID updatedBy) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String column = switch (setting.toLowerCase()) {
                    case "build" -> "allow_build";
                    case "pvp" -> "allow_pvp";
                    case "mobs", "mobspawning" -> "allow_mob_spawning";
                    default -> null;
                };

                if (column == null) return false;

                String updateQuery = String.format(
                        "UPDATE clan_settings SET %s = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE clan_id = ?",
                        column
                );

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

                    stmt.setBoolean(1, value);
                    stmt.setString(2, updatedBy.toString());
                    stmt.setInt(3, clanId);

                    boolean success = stmt.executeUpdate() > 0;

                    if (success) {
                        ClanSettings settings = settingsCache.get(clanId);
                        if (settings != null) {
                            switch (setting.toLowerCase()) {
                                case "build" -> settings.setAllowBuild(value);
                                case "pvp" -> settings.setAllowPvp(value);
                                case "mobs", "mobspawning" -> settings.setAllowMobSpawning(value);
                            }
                            settings.setUpdatedBy(updatedBy);
                        }
                    }

                    return success;
                }

            } catch (SQLException e) {
                logger.error("Errore durante l'aggiornamento dei settings del clan: " + e.getMessage());
                return false;
            }
        });
    }

    public void createDefaultSettings(int clanId, UUID createdBy) {
        CompletableFuture.supplyAsync(() -> {
            try {
                String insertQuery = """
                        INSERT INTO clan_settings (clan_id, updated_by) VALUES (?, ?)
                        ON DUPLICATE KEY UPDATE updated_by = VALUES(updated_by)
                        """;

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

                    stmt.setInt(1, clanId);
                    stmt.setString(2, createdBy.toString());

                    boolean success = stmt.executeUpdate() > 0;

                    if (success) {
                        ClanSettings settings = new ClanSettings(clanId, createdBy);
                        settingsCache.put(clanId, settings);
                    }

                    return success;
                }

            } catch (SQLException e) {
                logger.error("Errore durante la creazione delle impostazione degli clan default: " + e.getMessage());
                return false;
            }
        });
    }

    public ClanSettings getClanSettings(int clanId) {
        return settingsCache.get(clanId);
    }

    public boolean canBuild(int clanId) {
        ClanSettings settings = getClanSettings(clanId);
        return settings != null && !settings.isAllowBuild();
    }

    public boolean canPvp(int clanId) {
        ClanSettings settings = getClanSettings(clanId);
        return settings != null && settings.isAllowPvp();
    }

    public boolean canMobSpawn(int clanId) {
        ClanSettings settings = getClanSettings(clanId);
        return settings == null || settings.isAllowMobSpawning();
    }

    public void removeClanSettings(int clanId) {
        settingsCache.remove(clanId);
    }

}