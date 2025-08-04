package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ClanCommand extends BaseCommand {

    private final Map<String, BaseCommand> subCommands = new HashMap<>();

    public ClanCommand(ClansPlugin plugin) {
        super(plugin);
        register("clan");

        // Register subcommands
        subCommands.put("create", new CreateCommand(plugin));
        subCommands.put("disband", new DisbandCommand(plugin));
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "========== Comandi Clan ==========");
            sender.sendMessage(ChatColor.YELLOW + "/clan create <nome>" + ChatColor.GRAY + " - Crea un nuovo clan");
            sender.sendMessage(ChatColor.YELLOW + "/clan info" + ChatColor.GRAY + " - Info sul tuo clan");
            sender.sendMessage(ChatColor.YELLOW + "/clan invite <giocatore>" + ChatColor.GRAY + " - Invita qualcuno");
            sender.sendMessage(ChatColor.GOLD + "==================================");
            return true;
        }

        String sub = args[0].toLowerCase();
        BaseCommand subCommand = subCommands.get(sub);

        if (subCommand == null) {
            sender.sendMessage("Comando non trovato");
            return true;
        }

        return subCommand.executeCommand(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length > 1) {
            BaseCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.onTabCompleteCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }
}
