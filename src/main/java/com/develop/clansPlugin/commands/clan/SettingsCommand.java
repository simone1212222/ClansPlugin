package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SettingsCommand extends BaseCommand {

    protected SettingsCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

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

        if (args.length == 0) {
            var settings = plugin.getSettingsManager().getClanSettings(clan.getId());
            if (settings == null) {
                player.sendMessage("§cClan settings non trovati!");
                return true;
            }

            player.sendMessage("§6=== Clan Settings ===");
            player.sendMessage("§7Build: " + (settings.isAllowBuild() ? "§aAbilitato" : "§cDisabilitato"));
            player.sendMessage("§7PvP: " + (settings.isAllowPvp() ? "§aAbilitato" : "§cDisabilitato"));
            player.sendMessage("§7Mob Spawning: " + (settings.isAllowMobSpawning() ? "§aAbilitato" : "§cDisabilitato"));
            player.sendMessage("§7Last updated: §f" + settings.getUpdatedAt().toLocalDate());
            player.sendMessage("§7Usa: §f/clan settings <build/pvp/mobs> <true/false>");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /clan settings <build/pvp/mobs> <true/false>");
            return true;
        }

        String setting = args[0].toLowerCase();
        String valueStr = args[1].toLowerCase();

        if (!setting.matches("build|pvp|mobs|mobspawning")) {
            player.sendMessage("§cImpostazione invalida! Usa: build, pvp, or mobs");
            return true;
        }

        if (!valueStr.matches("true|false|on|off|enable|disable")) {
            player.sendMessage("§cImpostazione invalida! Usa: true/false, on/off, or enable/disable");
            return true;
        }

        boolean value = valueStr.matches("true|on|enable");

        CompletableFuture<Boolean> future = plugin.getSettingsManager()
                .updateSetting(clan.getId(), setting, value, player.getUniqueId());

        future.thenAccept(success -> {
            if (success) {
                String settingName = switch (setting) {
                    case "build" -> "Build";
                    case "pvp" -> "PvP";
                    case "mobs", "mobspawning" -> "Mob Spawning";
                    default -> setting;
                };

                String status = value ? "§aabilitato" : "§cdisabilitato";
                player.sendMessage("§6[Clan] §f" + settingName + " è stato " + status + "§f!");

                for (ClanMember clanMember : clan.getOnlineMembers()) {
                    Player memberPlayer = clanMember.getPlayer();
                    if (memberPlayer != null && !memberPlayer.equals(player)) {
                        memberPlayer.sendMessage("§6[Clan] §7" + player.getName() +
                                " ha " + (value ? "abilitato" : "disabilitato") + " " + settingName.toLowerCase());
                    }
                }
            } else {
                player.sendMessage("§cErrore durante l'aggiornamento degli settings dei clan.");
            }
        });
        return true;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {

        if (args.length == 1) {
            return List.of("build", "pvp", "mobs");
        }

        if (args.length == 2) {
            String setting = args[0].toLowerCase();
            if (setting.matches("build|pvp|mobs|mobspawning")) {
                return List.of("true", "false");
            }
        }
        return List.of();
    }
}
