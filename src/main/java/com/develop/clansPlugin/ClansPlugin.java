package com.develop.clansPlugin;

import com.develop.clansPlugin.commands.clan.ClanCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClansPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        // Register commands
        new ClanCommand(this);
    }
}
