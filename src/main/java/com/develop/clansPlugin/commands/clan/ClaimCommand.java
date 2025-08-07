package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import com.develop.clansPlugin.models.Territory;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ClaimCommand extends BaseCommand {
    protected ClaimCommand(ClansPlugin plugin) {
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
            sender.sendMessage(plugin.getConfigManager().getMessage("territory-system-disabled"));
            return true;
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

        Location location = player.getLocation();
        if (!plugin.getConfigManager().getAllowedWorlds().contains(Objects.requireNonNull(location.getWorld()).getName())) {
            player.sendMessage(plugin.getConfigManager().getMessage("territory-not-allowed"));
            return true;
        }

        CompletableFuture<Territory> future = plugin.getTerritoryManager()
                .claimTerritory(clan.getId(), location, player.getUniqueId());

        future.thenAccept(territory -> {
            if (territory == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("territory-already-claimed"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("territory-claimed"));
            }
        });
        return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        return List.of();
    }
}
