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

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan leave");
            return true;
        }

        // Controlla se il giocatore e' in un clan
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        // Controlla se il giocatore non e' il leader del clan'
        if (clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("cant-leave-as-leader"));
            return true;
        }

        // Caccia il player che ha eseguito il comando
        CompletableFuture<Boolean> future = plugin.getClanManager()
                .kickMember(clan.getId(), player.getUniqueId());

        future.thenAccept(success -> {
            if (success) {
                // Notifica al player che ha lasciato il clan
                player.sendMessage(plugin.getConfigManager().getMessage("player-left",
                        "%player%", player.getName()));

                // Notifica i membri del clan che il player ha lasciato il clan
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
