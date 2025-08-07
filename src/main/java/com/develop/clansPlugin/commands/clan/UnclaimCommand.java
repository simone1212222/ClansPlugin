package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import com.develop.clansPlugin.models.Territory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UnclaimCommand extends BaseCommand {

    protected UnclaimCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan claim");
            return true;
        }

        if (plugin.getConfigManager().isTerritoryEnabled()) {
            player.sendMessage(plugin.getConfigManager().getMessage("territory-system-disabled"));
        }

        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member.canManageTerritory()) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
            return true;
        }

        Territory territory = plugin.getTerritoryManager().getTerritoryAt(player.getLocation());
        if (territory == null || territory.clanId() != clan.getId()) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-territory"));
            return true;
        }

        CompletableFuture<Boolean> future = plugin.getTerritoryManager()
                .unclaimTerritory(territory.id());

        future.thenAccept(success -> {
            if (success) {
                player.sendMessage(plugin.getConfigManager().getMessage("territory-unclaimed"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§cErrore durante l'eliminazione del territorio.");
            }
        });
        return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        return List.of();
    }
}
