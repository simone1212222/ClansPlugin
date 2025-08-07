package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LeaveCommand extends BaseCommand {

    protected LeaveCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan leave");
            return true;
        }

        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        if (clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix"));
            return true;
        }

        CompletableFuture<Boolean> future = plugin.getClanManager()
                .kickMember(clan.getId(), player.getUniqueId());

        future.thenAccept(success -> {
            if (success) {
                player.sendMessage(plugin.getConfigManager().getMessage("player-left",
                        "%player%", player.getName()));

                for (ClanMember member : clan.getOnlineMembers()) {
                    Player memberPlayer = member.getPlayer();
                    if (memberPlayer != null && !memberPlayer.equals(player)) {
                        memberPlayer.sendMessage(plugin.getConfigManager().getMessage("player-left",
                                "%player%", player.getName()));
                    }
                }
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§cImpossibile uscire dal clan.");
            }
        });
        return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        return List.of();
    }
}
