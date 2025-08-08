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

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan accept");
            return true;
        }

        // Verifica se il giocatore è già in un clan
        if (plugin.getClanManager().getPlayerClan(player.getUniqueId()) != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-in-clan"));
            return true;
        }

        // Recupera gli inviti ricevuti dal giocatore
        var invites = plugin.getInviteManager().getPlayerInvites(player.getUniqueId());
        if (invites.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-invites"));
            return true;
        }

        var invite = invites.getFirst();
        // Il player accetta l'invito
        CompletableFuture<Boolean> future = plugin.getInviteManager().acceptInvite(player.getUniqueId(), invite.clanId());

        future.thenAccept(success -> {
            if (success) {
                Clan clan = plugin.getClanManager().getClan(invite.clanId());
                player.sendMessage(plugin.getConfigManager().getMessage("player-joined",
                        "%player%", player.getName()));

                // Se il clan non e' nullo notifica i membri del clan
                if (clan != null) {
                    for (ClanMember clanMember : clan.getOnlineMembers()) {
                        Player memberPlayer = clanMember.getPlayer();
                        if (memberPlayer != null && !memberPlayer.equals(player)) {
                            memberPlayer.sendMessage(plugin.getConfigManager().getMessage("player-joined",
                                    "%player%", player.getName()));
                        }
                    }
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
