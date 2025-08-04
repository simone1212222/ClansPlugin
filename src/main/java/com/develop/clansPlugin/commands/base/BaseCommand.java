package com.develop.clansPlugin.commands.base;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.logging.PluginLogger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final ClansPlugin plugin;
    protected final PluginLogger logger;

    protected BaseCommand(ClansPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
    }

    // Register Commands
    public void register(String label) {
        var command = Objects.requireNonNull(plugin.getCommand(label),
            () -> "Il comando '" + label + "' non è definito nel plugin.yml.");

        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
            sender.sendMessage(ChatColor.RED + "Non hai i permessi per eseguire questo comando.");
            return true;
        }

        try {
            return executeCommand(sender, args);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Si è verificato un errore durante l'esecuzione del comando.");
            logger.error("Errore nel comando /" + command.getName(), e);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        try {
            return onTabCompleteCommand(sender, args);
        } catch (Exception e) {
            logger.error("Errore nel tab complete del comando /" + command.getName(), e);
            return Collections.emptyList();
        }
    }

    public abstract boolean executeCommand(CommandSender sender, String[] args);

    public abstract List<String> onTabCompleteCommand(CommandSender sender, String[] args);
}
