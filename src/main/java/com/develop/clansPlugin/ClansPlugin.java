package com.develop.clansPlugin;

import com.develop.clansPlugin.commands.clan.ClanCommand;
import com.develop.clansPlugin.database.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClansPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        // Initialize config
        saveDefaultConfig();
        // Register commands
        new ClanCommand(this);
        // Initialize database
        DatabaseManager databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Impossibile inizializzare il database.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
