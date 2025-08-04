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

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Questo comando può essere eseguito solo da un giocatore.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "Usage: /clan create <name> <tag>");
            return true;
        }

        String name = args[0];
        String tag = args[1];

        // Validation
        if (name.length() > plugin.getConfigManager().getMaxNameLength()) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-name"));
            return true;
        }

        if (tag.length() > plugin.getConfigManager().getMaxTagLength()) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-tag"));
            return true;
        }

        if (!name.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-name"));
            return true;
        }

        if (!tag.matches("[a-zA-Z0-9]+")) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-tag"));
            return true;
        }

        // Check if the player is already in a clan
        if (plugin.getClanManager().getPlayerClan(player.getUniqueId()) != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-in-clan"));
            return true;
        }

        CompletableFuture<Clan> future = plugin.getClanManager().createClan(name, tag, player);
        future.thenAccept(clan -> {
            if (clan == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("clan-name-taken"));
            } else {
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
