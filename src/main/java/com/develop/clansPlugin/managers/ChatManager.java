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

    public ChatManager(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendClanMessage(Player sender, String message) {
        // Controlla se il player che ha inviato il comando e' in un clan
        Clan clan = plugin.getClanManager().getPlayerClan(sender.getUniqueId());
        if (clan == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("not-in-clan"));
            return;
        }

        // Ottiene il formato e il prefix del messaaggio
        String format = plugin.getConfigManager().getChatFormat();
        String prefix = plugin.getConfigManager().getChatPrefix();

        // Formatta per inviare il messaggio al clan
        format = format.replace("%player%", sender.getName());
        format = format.replace("%message%", message);
        format = format.replace("%clans_player_tag%", clan.getTag());

        // Traduce i colori
        String finalMessage = ChatColor.translateAlternateColorCodes('&', prefix + format);

        // Invia il messaggio a tutti i membri online del clan
        for (ClanMember member : clan.getMembers().values()) {
            Player memberPlayer = member.getPlayer();
            if (memberPlayer != null && memberPlayer.isOnline()) {
                memberPlayer.sendMessage(finalMessage);
            }
        }

    }

}