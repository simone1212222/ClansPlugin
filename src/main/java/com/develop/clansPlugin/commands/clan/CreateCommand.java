package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateCommand extends BaseCommand {

    public CreateCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length > 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan create <name> <tag>");
            return true;
        }

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan create <name> <tag>");
            return true;
        }

        String name = args[0];
        String tag = args[1];

        // Controlla lunghezza nome clan
        if (name.length() > plugin.getConfigManager().getMaxNameLength()) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-name"));
            return true;
        }

        // Controllo lunghezza tag
        if (tag.length() > plugin.getConfigManager().getMaxTagLength()) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-tag"));
            return true;
        }

        // Controlla se ci sono solo lettere, numeri o underscore
        if (!name.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-name"));
            return true;
        }

        // Controlla se ci sono solo lettere, numeri o underscore
        if (!tag.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-tag"));
            return true;
        }

        // Controlla che il giocatore non sia già in un clan
        if (plugin.getClanManager().getPlayerClan(player.getUniqueId()) != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-in-clan"));
            return true;
        }
        // Crea il clan
        CompletableFuture<Clan> future = plugin.getClanManager().createClan(name, tag, player);
        future.thenAccept(clan -> {
            if (clan == null) {
                // Se il clan e' nullo, invia messaggio dell'errore
                player.sendMessage(plugin.getConfigManager().getMessage("clan-name-or-tag-taken"));
            } else {
                // Invia messaggio del clan created
                player.sendMessage(plugin.getConfigManager().getMessage("clan-created",
                        "%clan%", clan.getName()));
            }
        });
        return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
