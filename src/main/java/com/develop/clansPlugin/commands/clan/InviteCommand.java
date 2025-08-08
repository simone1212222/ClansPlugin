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

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length > 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usage: /clan invite <player>");
            return true;
        } else if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usage: /clan invite <player>");
            return true;
        }

        // Controlla se il player e' nel clan
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        // Contro se il player e' almeno officer
        ClanMember member = clan.getMember(player.getUniqueId());
        if (!member.canInvite()) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
        }

        // Controlla se il player esiste
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        // Contro se non e' gia' in un clan
        if (plugin.getClanManager().getPlayerClan(target.getUniqueId()) != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-in-clan"));
            return true;
        }

        // Invita il player
        CompletableFuture<ClanInvite> future = plugin.getInviteManager()
                .createInvite(clan.getId(), player.getUniqueId(), target.getUniqueId(), target.getName());

        future.thenAccept(invite -> {
            if (invite == null) {
                // Se l'invito e' stato creato, invia un messaggio di errore al player
                player.sendMessage(plugin.getConfigManager().getMessage("already-invited"));
            } else {
                // Senno invia il messaggio al player che ha inviato il membro al clan
                player.sendMessage(plugin.getConfigManager().getMessage("invite-sent",
                        "%player%", target.getName()));
                // E lo invia anche al membro invitato
                target.sendMessage(plugin.getConfigManager().getMessage("invite-received",
                        "%clan%", clan.getName()));
            }
        });
        return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        if (args.length == 1) {
            Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
            if (clan == null) return List.of();

            return plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                    .filter(p -> plugin.getClanManager().getPlayerClan(p.getUniqueId()) == null)
                    .filter(p -> !plugin.getInviteManager().hasActiveInvite(clan.getId(), p.getUniqueId()))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}


