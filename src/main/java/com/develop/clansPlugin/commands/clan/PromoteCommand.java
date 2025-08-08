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

public class PromoteCommand extends BaseCommand {

    protected PromoteCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan promote <player>");
            return true;
        } else if (args.length > 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan promote <player>");
            return true;
        }

        // Controlla che il player sia nel clan
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        // Controlla che il player sia il leader del clan
        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-leader"));
            return true;
        }

        // Prende il targetMember
        ClanMember targetMember = clan.getMembers().values().stream()
                .filter(m -> m.getPlayerName().equalsIgnoreCase(args[0]))
                .findFirst()
                .orElse(null);

        // Controlla se non e' nullo
        if (targetMember == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        // Controlla che il player non ha selezionato se stesso
        if (targetMember.getPlayerUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§cSei già il leader del clan.");
            return true;
        }

        // Controlla che il player non ha selezionato un officer
        if (targetMember.getRole() == ClanRole.OFFICER) {
            player.sendMessage(plugin.getConfigManager().getMessage("already-officer",
                    "%role%", ClanRole.OFFICER.getDisplayName(),
                    "%player%", targetMember.getPlayerName()));
            return true;
        }

        // Promuove il targetMember
        CompletableFuture<Boolean> future = plugin.getClanManager()
                .promoteMember(clan.getId(), targetMember.getPlayerUuid(), ClanRole.OFFICER);

        future.thenAccept(success -> {
            if (success) {
                // Notifica i membri del player promosso
                for (ClanMember clanMember : clan.getOnlineMembers()) {
                    Player memberPlayer = clanMember.getPlayer();
                    if (memberPlayer != null) {
                        memberPlayer.sendMessage(plugin.getConfigManager().getMessage("player-promoted",
                                "%player%", targetMember.getPlayerName(),
                                "%role%", ClanRole.OFFICER.getDisplayName()));
                    }
                }
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§cErrore durante la promozione del membro del clan.");            }
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
                    .filter(m -> m.getRole() == ClanRole.MEMBER)
                    .map(ClanMember::getPlayerName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}
