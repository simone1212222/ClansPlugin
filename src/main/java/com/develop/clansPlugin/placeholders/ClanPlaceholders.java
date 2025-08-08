package com.develop.clansPlugin.placeholders;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.models.Clan;
import com.develop.clansPlugin.models.ClanMember;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClanPlaceholders extends PlaceholderExpansion {

    private final ClansPlugin plugin;

    public ClanPlaceholders(ClansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clans";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // Indica a PlaceholderAPI di mantenere registrata l'espansione anche se il plugin viene ricaricato
        return true;
    }


    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());

        // Gestione placeholders con switch case
        return switch (params.toLowerCase()) {
            case "player_clan" -> clan != null ? clan.getName() : "";
            case "player_tag" -> clan != null ? clan.getTag() : "";
            case "player_role" -> {
                if (clan != null) {
                    ClanMember member = clan.getMember(player.getUniqueId());
                    yield member != null ? member.getRole().getDisplayName() : "";
                }
                yield "";
            }
            case "clan_members_online" -> clan != null ? String.valueOf(clan.getOnlineMemberCount()) : "";
            default -> null;
        };
    }
}
