package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import com.develop.clansPlugin.models.ClanRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DemoteCommand extends BaseCommand {

    protected DemoteCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan demote <player>");
            return true;
        } else if (args.length > 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan demote <player>");
            return true;
        }

        // Il player deve far parte di un clan
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        // Controlla se il player e' il leader del clan
        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-leader"));
            return true;
        }

        // Prende il targetMember dal clan
        ClanMember targetMember = clan.getMembers().values().stream()
                .filter(m -> m.getPlayerName().equalsIgnoreCase(args[0]))
                .findFirst()
                .orElse(null);

        // Controlla se il targetMember esiste
        if (targetMember == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        // Controlla se il giocatore che sta eseguendo il comando e' diverso dal targetMember'
        if (targetMember.getPlayerUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§cNon puoi declassare te stesso.");
            return true;
        }

        // Controlla che il targetMember e' officer
        if (targetMember.getRole() != ClanRole.OFFICER) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix" +
                    "§cQuesto giocatore è già un membro del clan."));
            return true;
        }

        // Retrocede il member
        CompletableFuture<Boolean> future = plugin.getClanManager()
                .promoteMember(clan.getId(), targetMember.getPlayerUuid(), ClanRole.MEMBER);

        future.thenAccept(success -> {
            if (success) {
                // Invia un messaggio di notifica ai membri del clan
                for (ClanMember clanMember : clan.getOnlineMembers()) {
                    Player memberPlayer = clanMember.getPlayer();
                    if (memberPlayer != null) {
                        memberPlayer.sendMessage(plugin.getConfigManager().getMessage("player-demoted",
                                "%player%", targetMember.getPlayerName(),
                                "%role%", ClanRole.MEMBER.getDisplayName()));
                    }
                }
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§cErrore durante la retrocessione del membro del clan.");
            }
        });
       return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
            if (clan == null || !clan.isLeader(player.getUniqueId())) return List.of();

            return clan.getMembers().values().stream()
                    .filter(m -> !m.getPlayerUuid().equals(player.getUniqueId()))
                    .filter(m -> m.getRole() == ClanRole.OFFICER)
                    .map(ClanMember::getPlayerName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return List.of();
    }

}
