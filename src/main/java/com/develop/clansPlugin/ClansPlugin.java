package com.develop.clansPlugin;

import com.develop.clansPlugin.commands.clan.ClanCommand;
import com.develop.clansPlugin.database.DatabaseManager;
import com.develop.clansPlugin.logging.PluginLogger;
import com.develop.clansPlugin.managers.ChatManager;
import com.develop.clansPlugin.managers.ClanManager;
import com.develop.clansPlugin.managers.ConfigManager;
import com.develop.clansPlugin.managers.InviteManager;
import com.develop.clansPlugin.placeholders.ClanPlaceholders;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClansPlugin extends JavaPlugin {

    private PluginLogger logger;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ClanManager clanManager;
    private ChatManager chatManager;
    private InviteManager inviteManager;

    @Override
    public void onEnable() {

        initializeLogger();
        loadConfig();
        if (!initializeDatabase()) return;
        initializeManagers();
        registerCommands();
        registerPlaceholders();

        logger.info("Plugin caricato con successo.");
    }

    @Override
    public void onDisable() {
        databaseShutdown();
    }


    private void loadConfig() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        logger.info("Config caricato con successo.");
    }

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

    private void initializeManagers() {
        clanManager = new ClanManager(this);
        chatManager = new ChatManager(this);
        inviteManager = new InviteManager(this);
        logger.info("Managers inizializzati.");
    }

    private void registerCommands() {
        new ClanCommand(this);
        logger.info("Comandi registrati.");
    }

    private void initializeLogger() {
        this.logger = new PluginLogger(getLogger());
        logger.info("Logger inizializzato.");
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholders(this).register();
            getLogger().info("Placeholders registrati.");
        }
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
}
