package com.develop.clansPlugin.managers;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {

    private final ClansPlugin plugin;
    private final Set<UUID> spyPlayers;

    public ChatManager(ClansPlugin plugin) {
        this.plugin = plugin;
        this.spyPlayers = ConcurrentHashMap.newKeySet();
    }

    public void sendClanMessage(Player sender, String message) {
        Clan clan = plugin.getClanManager().getPlayerClan(sender.getUniqueId());
        if (clan == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return;
        }

        String format = plugin.getConfigManager().getChatFormat();
        String prefix = plugin.getConfigManager().getChatPrefix();

        format = format.replace("%player%", sender.getName());
        format = format.replace("%message%", message);
        format = format.replace("%clans_player_tag%", clan.getTag());

        String finalMessage = ChatColor.translateAlternateColorCodes('&', prefix + format);

        for (ClanMember member : clan.getMembers().values()) {
            Player memberPlayer = member.getPlayer();
            if (memberPlayer != null && memberPlayer.isOnline()) {
                memberPlayer.sendMessage(finalMessage);
            }
        }

        if (!plugin.getConfigManager().isSpyEnabled()) {
            for (UUID spyUuid : spyPlayers) {
                Player spyPlayer = plugin.getServer().getPlayer(spyUuid);
                if (spyPlayer != null && spyPlayer.isOnline() && !clan.hasMember(spyUuid)) {
                    spyPlayer.sendMessage("§8[SPY] " + finalMessage);
                }
            }
        }
    }

}