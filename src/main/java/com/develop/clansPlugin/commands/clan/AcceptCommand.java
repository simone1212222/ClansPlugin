package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AcceptCommand extends BaseCommand {

    protected AcceptCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        if (plugin.getClanManager().getPlayerClan(player.getUniqueId()) != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-in-clan"));
            return true;
        }

        var invites = plugin.getInviteManager().getPlayerInvites(player.getUniqueId());
        if (invites.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-invites"));
            return true;
        }

        var invite = invites.getFirst();
        CompletableFuture<Boolean> future = plugin.getInviteManager().acceptInvite(player.getUniqueId(), invite.getClanId());

        future.thenAccept(success -> {
            if (success) {
                Clan clan = plugin.getClanManager().getClan(invite.getClanId());
                player.sendMessage(plugin.getConfigManager().getMessage("player-joined",
                        "%player%", player.getName()));

                if (clan != null) {
                    for (ClanMember clanMember : clan.getOnlineMembers()) {
                        Player memberPlayer = clanMember.getPlayer();
                        if (memberPlayer != null && !memberPlayer.equals(player)) {
                            memberPlayer.sendMessage(plugin.getConfigManager().getMessage("player-joined",
                                    "%player%", player.getName()));
                        }
                    }
                } else {
                    // TODO: expired clan invite...
                }
            }
        });
        return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        return List.of();
    }
}
