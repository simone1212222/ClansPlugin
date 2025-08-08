package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import com.develop.clansPlugin.models.ClanRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClanInfoCommand extends BaseCommand {

    protected ClanInfoCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan;

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length > 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan info <clan>");
            return true;
        }

        // Se viene specificato un nome di clan, lo cerca per nome
        if (args.length > 0) {
            // Se il clan non esiste, invia un messaggio di errore
            clan = plugin.getClanManager().getClanByName(args[0]);
            if (clan == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("clan-not-found"));
                return true;
            }
        } else {
            // Se il player non e' in un clan, invia un messaggio di errore
            clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
            if (clan == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
                return true;
            }
        }

        // Invia le informazioni base del clan
        player.sendMessage("§6=== Clan Info: " + clan.getName() + " ===");
        player.sendMessage("§7Tag: §f[" + clan.getTag() + "]");
        player.sendMessage("§7Members: §f" + clan.getTotalMemberCount() +
                " (§a" + clan.getOnlineMemberCount() + " online§f)");
        player.sendMessage("§7Created: §f" + clan.getCreatedAt().toLocalDate());

        player.sendMessage("§7Leader: §f" + clan.getMembersByRole(ClanRole.LEADER)
                .stream().findFirst().map(ClanMember::getPlayerName).orElse("None"));

        List<ClanMember> officers = clan.getMembersByRole(ClanRole.OFFICER);
        if (!officers.isEmpty()) {
            player.sendMessage("§7Officers: §f" +
                    String.join(", ", officers.stream().map(ClanMember::getPlayerName).toList()));
        }
        return true;
    }


    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return plugin.getClanManager().getAllClans().stream()
                    .map(Clan::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .toList();
        }
        return List.of();
    }
}
