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
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "player_clan":
                return clan != null ? clan.getName() : "";

            case "player_tag":
                return clan != null ? clan.getTag() : "";

            case "player_role":
                if (clan != null) {
                    ClanMember member = clan.getMember(player.getUniqueId());
                    return member != null ? member.getRole().getDisplayName() : "";
                }
                return "";

        }
        return null;
    }
}
