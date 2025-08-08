package com.develop.clansPlugin.managers;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.logging.PluginLogger;
import com.develop.clansPlugin.models.Territory;
import org.bukkit.Location;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TerritoryManager {

    private final ClansPlugin plugin;
    private final PluginLogger logger;
    private final Map<Integer, Territory> territoryCache;
    private final Map<Integer, List<Territory>> clanTerritories;
    private final Map<String, List<Territory>> worldTerritories;

    public TerritoryManager(ClansPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
        this.territoryCache = new ConcurrentHashMap<>();
        this.clanTerritories = new ConcurrentHashMap<>();
        this.worldTerritories = new ConcurrentHashMap<>();

        loadTerritoriesAsync();
    }

    private void loadTerritoriesAsync() {
        CompletableFuture.runAsync(() -> {
            loadAllTerritories();
            logger.info("Caricati " + territoryCache.size() + " territori dal Database.");
        });
    }

    // Carica tutti i territori dal database e li inserisce nella cache
    private void loadAllTerritories() {
        String query = """
            SELECT * FROM territories ORDER BY clan_id, id
            """;

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Pulizia della cache
            territoryCache.clear();
            clanTerritories.clear();
            worldTerritories.clear();

            while (rs.next()) {
                Territory territory = new Territory(
                        rs.getInt("id"),
                        rs.getInt("clan_id"),
                        rs.getString("world"),
                        rs.getInt("center_x"),
                        rs.getInt("center_z"),
                        rs.getInt("radius"),
                        rs.getTimestamp("claimed_at").toLocalDateTime(),
                        UUID.fromString(rs.getString("claimed_by"))
                );
                // Inserisce la cache nel DB
                territoryCache.put(territory.id(), territory);

                clanTerritories.computeIfAbsent(territory.clanId(), k -> new ArrayList<>()).add(territory);

                worldTerritories.computeIfAbsent(territory.worldName(), k -> new ArrayList<>()).add(territory);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il caricamento dei territori", e);
        }
    }

    // Metodo per claimare i territori
    public CompletableFuture<Territory> claimTerritory(int clanId, Location location, UUID claimedBy) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                // Controlla il limite dei territori del clan
                List<Territory> clanTerritoryList = getClanTerritories(clanId);
                if (clanTerritoryList.size() >= plugin.getConfigManager().getMaxTerritories()) {
                    return null;
                }

                int radius = plugin.getConfigManager().getTerritorySize();
                int minDistance = plugin.getConfigManager().getMinDistance();

                // Controllo distanza minima da territori esistenti nello stesso mondo
                for (Territory existing : getTerritoriesInWorld(Objects.requireNonNull(location.getWorld()).getName())) {
                    double distance = Math.sqrt(Math.pow(location.getX() - existing.centerX(), 2) +
                            Math.pow(location.getZ() - existing.centerZ(), 2));

                    if (distance < minDistance) {
                        return null;
                    }
                }
                // Query da inserire nel DB
                String insertQuery = """
                        INSERT INTO territories (clan_id, world, center_x, center_z, radius, claimed_by)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

                    stmt.setInt(1, clanId);
                    stmt.setString(2, location.getWorld().getName());
                    stmt.setInt(3, location.getBlockX());
                    stmt.setInt(4, location.getBlockZ());
                    stmt.setInt(5, radius);
                    stmt.setString(6, claimedBy.toString());

                    // Updata i dati
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            int territoryId = keys.getInt(1);

                            Territory territory = new Territory(
                                    territoryId, clanId, location.getWorld().getName(),
                                    location.getBlockX(), location.getBlockZ(), radius,
                                    LocalDateTime.now(), claimedBy
                            );
                            // Inserisce la cache nel DB
                            territoryCache.put(territoryId, territory);
                            clanTerritories.computeIfAbsent(clanId, k -> new ArrayList<>()).add(territory);
                            worldTerritories.computeIfAbsent(location.getWorld().getName(), k -> new ArrayList<>()).add(territory);

                            return territory;
                        }
                    }
                }
                return null;
            } catch (SQLException e) {
                logger.error("Errore durante il claim del territorio: " + e.getMessage());
                return null;
            }
        });
    }

    // Metodo per unclaiamre il territorio
    public CompletableFuture<Boolean> unclaimTerritory(int territoryId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Query da rimuovere dal DB
                String deleteQuery = "DELETE FROM territories WHERE id = ?";

                try (Connection conn = plugin.getDatabaseManager().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

                    stmt.setInt(1, territoryId);
                    // Update i dati
                    boolean success = stmt.executeUpdate() > 0;

                    if (success) {
                        // Rimuove dalla cache
                        Territory territory = territoryCache.remove(territoryId);
                        if (territory != null) {
                            List<Territory> clanList = clanTerritories.get(territory.clanId());
                            if (clanList != null) {
                                clanList.remove(territory);
                            }

                            List<Territory> worldList = worldTerritories.get(territory.worldName());
                            if (worldList != null) {
                                worldList.remove(territory);
                            }
                        }
                    }

                    return success;
                }

            } catch (SQLException e) {
                logger.error("Errore durante il unclaim del territorio: " + e.getMessage());
                return false;
            }
        });
    }

    public Territory getTerritoryAt(Location location) {
        if (location == null || location.getWorld() == null) return null;

        List<Territory> worldTerritoryList = worldTerritories.get(location.getWorld().getName());
        if (worldTerritoryList == null) return null;

        for (Territory territory : worldTerritoryList) {
            if (territory.contains(location)) {
                return territory;
            }
        }

        return null;
    }

    public void removeAllClanTerritories(int clanId) {
        List<Territory> toRemove = getClanTerritories(clanId);

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM territories WHERE clan_id = ?")) {

            stmt.setInt(1, clanId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Errore durante la rimozione dei territori del clan ID=" + clanId, e);
        }

        for (Territory territory : toRemove) {
            territoryCache.remove(territory.id());

            List<Territory> worldList = worldTerritories.get(territory.worldName());
            if (worldList != null) {
                worldList.remove(territory);
            }
        }

        clanTerritories.remove(clanId);
    }


    public List<Territory> getClanTerritories(int clanId) {
        return clanTerritories.getOrDefault(clanId, new ArrayList<>());
    }

    public List<Territory> getTerritoriesInWorld(String worldName) {
        return worldTerritories.getOrDefault(worldName, new ArrayList<>());
    }

}
