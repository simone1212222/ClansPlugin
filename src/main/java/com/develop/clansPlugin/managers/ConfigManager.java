package com.develop.clansPlugin.managers;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.logging.PluginLogger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ConfigManager {

    private final ClansPlugin plugin;
    private FileConfiguration config;
    private final PluginLogger logger;

    public ConfigManager(ClansPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
    }

    // Carica il config.yml e lo salva in config
    public void loadConfig() {
        // Trova il config e se non esiste lo crea
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        // Carica il config di default (quello dentro il jar) per usare le key di default
        InputStream defaultStream = plugin.getResource("config.yml");
        // Carica il config default in memoria per usarlo come fallback
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);
        } else {
                logger.error("Impossibile caricare il config.yml");
            }
        }

        // Ricarica il config dal file config.yml
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }


    // Database settings

    public String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }

    public int getDatabasePort() {
        return config.getInt("database.port", 3306);
    }

    public String getDatabaseName() {
        return config.getString("database.database", "clanplugin");
    }

    public String getDatabaseUsername() {
        return config.getString("database.username", "clanplugin");
    }

    public String getDatabasePassword() {
        return config.getString("database.password", "");
    }


    // Clan settings
    public int getMaxNameLength() {
        return config.getInt("clan.max-name-length", 16);
    }

    public int getMaxTagLength() {
        return config.getInt("clan.max-tag-length", 6);
    }


    // Territory settings
    public boolean isTerritoryEnabled() {
        return !config.getBoolean("territory.enabled", true);
    }

    public int getMaxTerritories() {
        return config.getInt("territory.max-territories", 5);
    }

    public int getTerritorySize() {
        return config.getInt("territory.territory-size", 50);
    }

    public int getMinDistance() {
        return config.getInt("territory.min-distance", 100);
    }

    public boolean isAutoProtect() {
        return !config.getBoolean("territory.auto-protect", true);
    }

    public List<String> getAllowedWorlds() {
        return config.getStringList("territory.allowed-worlds");
    }

    // Chat settings
    public String getChatPrefix() {
        return colorize(config.getString("chat.prefix", "&6[CLAN] &r"));
    }

    public String getChatFormat() {
        return config.getString("chat.format", "&7[%clans_player_tag%&7] &f%player%&7: &f%message%");
    }

    public boolean isSpyEnabled() {
        return config.getBoolean("chat.spy-enabled", true);
    }

    // Home settings
    public int getTeleportDelay() {
        return config.getInt("home.teleport-delay", 3);
    }

    // Messages
    public String getMessage(String key) {
        String path = "messages." + key;

        assert config.getRoot() != null;
        if (config.getRoot().get(path, null) != null) {
            return colorize(config.getString(path));
        }

        if (config.getDefaults() != null && config.getDefaults().contains(path)) {
            String defValue = config.getDefaults().getString(path);
            logger.warn("Chiave mancante nel config.yml: '" + path + "' → imposto il valore di default: '" + defValue + "'");
            return colorize(defValue);
        }

        logger.warn("Chiave '" + path + "' non trovata né nel config né nei defaults");
        return "Messaggio non trovato: " + key;
    }



    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        message = message.replace("{", "").replace("}", "");
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}

