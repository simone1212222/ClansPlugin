package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.admin.ReloadCommand;
import com.develop.clansPlugin.commands.base.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanCommand extends BaseCommand {

    private final Map<String, BaseCommand> subCommands = new HashMap<>();

    public ClanCommand(ClansPlugin plugin) {
        super(plugin);
        register("clan");

        // Register subcommands
        subCommands.put("create", new CreateCommand(plugin));
        subCommands.put("disband", new DisbandCommand(plugin));
        subCommands.put("sethome", new SetHomeCommand(plugin));
        subCommands.put("home", new HomeCommand(plugin));
        subCommands.put("chat", new ClanChatCommand(plugin));
        subCommands.put("info", new ClanInfoCommand(plugin));
        subCommands.put("invite", new InviteCommand(plugin));
        subCommands.put("accept", new AcceptCommand(plugin));
        subCommands.put("kick", new KickCommand(plugin));
        subCommands.put("promote", new PromoteCommand(plugin));
        subCommands.put("demote", new DemoteCommand(plugin));
        subCommands.put("leave", new LeaveCommand(plugin));
        subCommands.put("claim", new ClaimCommand(plugin));
        subCommands.put("unclaim", new UnclaimCommand(plugin));
        subCommands.put("reload", new ReloadCommand(plugin));
        subCommands.put("settings", new SettingsCommand(plugin));
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Questo comando può essere eseguito solo da un giocatore.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6=== Comandi Clan ===");
            sender.sendMessage("§7/clan create <nome> <tag> §f- Crea un nuovo clan");
            sender.sendMessage("§7/clan disband §f- Sciogli il tuo clan (solo leader)");
            sender.sendMessage("§7/clan invite <giocatore> §f- Invita un giocatore nel tuo clan");
            sender.sendMessage("§7/clan accept §f- Accetta l'invito del clan");
            sender.sendMessage("§7/clan kick <giocatore> §f- Espelli un giocatore dal tuo clan");
            sender.sendMessage("§7/clan promote/demote <giocatore> §f- Gestisci i ruoli dei membri");
            sender.sendMessage("§7/clan chat <messaggio> §f- Invia un messaggio nella chat del clan");
            sender.sendMessage("§7/clan claim §f- Rivendica un territorio nella tua posizione");
            sender.sendMessage("§7/clan home §f- Teletrasportati alla casa del clan");
            sender.sendMessage("§7/clan sethome §f- Imposta la posizione della casa del clan");
            sender.sendMessage("§7/clan info [clan] §f- Visualizza le informazioni del clan");
            sender.sendMessage("§7/clan leave §f- Lascia il clan corrente");
            sender.sendMessage("§7/clan settings [build/pvp/mobs] [true/false] §f- Imposta i settings del clan");


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
