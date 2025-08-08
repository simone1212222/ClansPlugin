package com.develop.clansPlugin;

import com.develop.clansPlugin.commands.clan.ClanCommand;
import com.develop.clansPlugin.database.DatabaseManager;
import com.develop.clansPlugin.listeners.clan.ClaimListener;
import com.develop.clansPlugin.logging.PluginLogger;
import com.develop.clansPlugin.managers.*;
import com.develop.clansPlugin.placeholders.ClanPlaceholders;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClansPlugin extends JavaPlugin {

    private PluginLogger logger;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ClanManager clanManager;
    private ChatManager chatManager;
    private InviteManager inviteManager;
    private TerritoryManager territoryManager;
    private SettingsManager settingsManager;

    @Override
    public void onEnable() {

        initializeLogger(); // Registra il logger
        loadConfig(); // Carica il config
        if (!initializeDatabase()) return; // Carica inizializza database
        initializeManagers(); // Inizializza managers
        registerCommands(); // Registra i comandi
        registerPlaceholders(); // Registra i placeholders
        registerListeners(); // Registra i listeners

        logger.info("Plugin caricato con successo.");
    }

    @Override
    public void onDisable() {
        databaseShutdown();
    }

    // Carica il config se non esiste
    private void loadConfig() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        logger.info("Config caricato con successo.");
    }

    // Carica inizializza database, stoppa il plugin se fallisce
    private boolean initializeDatabase() {
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Impossibile inizializzare il database.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        } else {
            logger.info("Database inizializzato");
        }
        return true;
    }

    // Carica inizializza i managers
    private void initializeManagers() {
        clanManager = new ClanManager(this);
        chatManager = new ChatManager(this);
        inviteManager = new InviteManager(this);
        territoryManager = new TerritoryManager(this);
        clanManager = new ClanManager(this);
        settingsManager = new SettingsManager(this);
        logger.info("Managers inizializzati.");
    }

    // Registra i comandi
    private void registerCommands() {
        new ClanCommand(this);
        logger.info("Comandi registrati.");
    }

    // Inizializza il logger
    private void initializeLogger() {
        this.logger = new PluginLogger(getLogger());
        logger.info("Logger inizializzato.");
    }

    // Registra i placeholders se trova PlaceHolderAPI
    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholders(this).register();
            getLogger().info("Placeholders registrati.");
        }
    }

    // Registra i listeners
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ClaimListener(this), this);
        getLogger().info("Listener registrati.");
    }
    private void databaseShutdown() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    // Getters
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public PluginLogger getPluginLogger() {
        return logger;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public InviteManager getInviteManager() {
        return inviteManager;
    }

    public TerritoryManager getTerritoryManager() {
        return territoryManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }
}
