package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanInvite;
import com.develop.clansPlugin.models.ClanMember;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InviteCommand extends BaseCommand{

    protected InviteCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "Usage: /clan invite <player>");
            return true;
        }

        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (!member.canInvite()) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        if (plugin.getClanManager().getPlayerClan(target.getUniqueId()) != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-in-clan"));
            return true;
        }

        CompletableFuture<ClanInvite> future = plugin.getInviteManager()
                .createInvite(clan.getId(), player.getUniqueId(), target.getUniqueId(), target.getName());

        future.thenAccept(invite -> {
            if (invite == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("already-invited"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("invite-sent",
                        "%player%", target.getName()));
                target.sendMessage(plugin.getConfigManager().getMessage("invite-received",
                        "%clan%", clan.getName()));
            }
        });
        return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        return List.of();
    }
}


