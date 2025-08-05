package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SetHomeCommand extends BaseCommand {

    protected SetHomeCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-leader"));
            return true;
        }

        CompletableFuture<Boolean> future = plugin.getClanManager()
                .setHome(clan.getId(), player.getLocation());

        future.thenAccept(success -> {
            if (success) {
                player.sendMessage(plugin.getConfigManager().getMessage("home-set"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§cImpossibile impostare la home del clan.");
            }
        });
        return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        return List.of();
    }
}
