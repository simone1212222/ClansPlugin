package com.develop.clansPlugin.database;

import com.develop.clansPlugin.ClansPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseManager {

    private final ClansPlugin plugin;
    private final Logger logger;
    private HikariDataSource dataSource;

    public DatabaseManager(ClansPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public boolean initialize() {
        try {
            setupDataSource();
            createTables();
            logger.info("Connessione al database riuscita.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Errore durante la connessione al database:" + e);

        }
        return false;
    }

    private void setupDataSource() {
        HikariConfig hikariConfig = getHikariConfig();

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    private @NotNull HikariConfig getHikariConfig() {
        FileConfiguration config = plugin.getConfig();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s",
                config.getString("database.host", "localhost"),
                config.getInt("database.port", 3306),
                config.getString("database.database", "clansplugin")));

        hikariConfig.setUsername(config.getString("database.username", "root"));
        hikariConfig.setPassword(config.getString("database.password", ""));
        hikariConfig.setMaximumPoolSize(config.getInt("database.max_pool_size", 10));
        hikariConfig.setConnectionTimeout(config.getLong("database.connection_timeout", 30000));
        hikariConfig.setIdleTimeout(config.getLong("database.idle-timeout", 600000));
        hikariConfig.setMaxLifetime(config.getLong("database.max-lifetime", 1800000));
        return hikariConfig;
    }

    private void createTables() throws SQLException {
        String[] queries = {
                // Clan table
                """
            CREATE TABLE IF NOT EXISTS clans (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(32) NOT NULL UNIQUE,
                tag VARCHAR(10) NOT NULL UNIQUE,
                leader_uuid CHAR(36) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                home_world VARCHAR(64) DEFAULT NULL,
                home_x DOUBLE DEFAULT NULL,
                home_y DOUBLE DEFAULT NULL,
                home_z DOUBLE DEFAULT NULL,
                home_yaw FLOAT DEFAULT NULL,
                home_pitch FLOAT DEFAULT NULL,
                INDEX idx_leader (leader_uuid),
                INDEX idx_name (name),
                INDEX idx_tag (tag)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

                // Clan members table
                """
            CREATE TABLE IF NOT EXISTS clan_members (
                id INT AUTO_INCREMENT PRIMARY KEY,
                clan_id INT NOT NULL,
                player_uuid CHAR(36) NOT NULL,
                player_name VARCHAR(16) NOT NULL,
                role ENUM('LEADER', 'OFFICER', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
                joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE,
                UNIQUE KEY unique_member (clan_id, player_uuid),
                INDEX idx_player (player_uuid),
                INDEX idx_clan (clan_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

                // Clan invites table
                """
            CREATE TABLE IF NOT EXISTS clan_invites (
                id INT AUTO_INCREMENT PRIMARY KEY,
                clan_id INT NOT NULL,
                inviter_uuid CHAR(36) NOT NULL,
                invited_uuid CHAR(36) NOT NULL,
                invited_name VARCHAR(16) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP NOT NULL,
                FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE,
                UNIQUE KEY unique_invite (clan_id, invited_uuid),
                INDEX idx_invited (invited_uuid),
                INDEX idx_expires (expires_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

                // Territory table
                """
            CREATE TABLE IF NOT EXISTS territories (
                id INT AUTO_INCREMENT PRIMARY KEY,
                clan_id INT NOT NULL,
                world VARCHAR(64) NOT NULL,
                center_x INT NOT NULL,
                center_z INT NOT NULL,
                radius INT NOT NULL DEFAULT 50,
                claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                claimed_by CHAR(36) NOT NULL,
                FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE,
                INDEX idx_clan (clan_id),
                INDEX idx_location (world, center_x, center_z)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """,

                // Player data table
                """
            CREATE TABLE IF NOT EXISTS player_data (
                player_uuid CHAR(36) PRIMARY KEY,
                player_name VARCHAR(16) NOT NULL,
                clan_id INT DEFAULT NULL,
                last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE SET NULL,
                INDEX idx_clan (clan_id),
                INDEX idx_name (player_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """
        };

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            for (String query : queries) {
                statement.executeUpdate(query);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource non inizializzato");
        }
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Connessione al database chiusa correttamente.");
        }
    }
}
