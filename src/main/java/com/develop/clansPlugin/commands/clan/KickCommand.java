package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class KickCommand extends BaseCommand {

    protected KickCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan kick <player>");
            return true;
        } else if (args.length > 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan kick <player>");
            return true;
        }

        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (!member.canKick()) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
            return true;
        }

        String targetNameInput = args[0];

        ClanMember targetMember = clan.getMembers().values().stream()
                .filter(m -> m.getPlayerName().equalsIgnoreCase(targetNameInput))
                .findFirst()
                .orElse(null);

        if (targetMember == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return true;
            }

        UUID targetUuid = targetMember.getPlayerUuid();
        String targetName = targetMember.getPlayerName();

        if (!clan.hasMember(targetUuid)) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-in-your-clan"));
            return true;
        }

        if (!member.getRole().canKick(targetMember.getRole())) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-enough-rights"));
            return true;
        }

        final Player targetPlayer = plugin.getServer().getPlayer(targetUuid);

        CompletableFuture<Boolean> future = plugin.getClanManager()
                .kickMember(clan.getId(), targetUuid);


        future.thenAccept(success -> {
            if (success) {
                for (ClanMember clanMember : clan.getOnlineMembers()) {
                    Player memberPlayer = clanMember.getPlayer();
                    if (memberPlayer != null) {
                        memberPlayer.sendMessage(plugin.getConfigManager().getMessage("player-kicked",
                                "%player%", targetName));
                    }
                }

                if (targetPlayer != null) {
                    targetPlayer.sendMessage(plugin.getConfigManager().getMessage("player-got-kicked",
                            "%clan%", clan.getName()));
                }
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§cImpossibile kickare il membro del clan.");
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

            return clan.getMembers().values().stream()
                    .map(ClanMember::getPlayerName)
                    .filter(name -> !name.equalsIgnoreCase(player.getName()))
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
