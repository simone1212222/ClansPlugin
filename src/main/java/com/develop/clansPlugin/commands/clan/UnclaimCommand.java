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

        // Controlla l'utilizzo corretto degli args del comando
        if (args.length > 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§8Usa: /clan claim");
            return true;
        }

        // Controlla se il sistema dei territori sia abilitato
        if (plugin.getConfigManager().isTerritoryEnabled()) {
            player.sendMessage(plugin.getConfigManager().getMessage("territory-system-disabled"));
        }

        // Controlla se il player e' in un clan
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return true;
        }

        // Controlla se il player e' almeno officer
        ClanMember member = clan.getMember(player.getUniqueId());
        if (member.canManageTerritory()) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
            return true;
        }

        // Controlla se il player ha claimato il territorio presente
        Territory territory = plugin.getTerritoryManager().getTerritoryAt(player.getLocation());
        if (territory == null || territory.clanId() != clan.getId()) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-territory"));
            return true;
        }

        // Unclaima il territorio
        CompletableFuture<Boolean> future = plugin.getTerritoryManager()
                .unclaimTerritory(territory.id());

        future.thenAccept(success -> {
            if (success) {
                // Notifica il player che ha unclaimato il territorio
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
