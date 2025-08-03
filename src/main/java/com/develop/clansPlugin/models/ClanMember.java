package com.develop.clansPlugin.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ClanMember {

    private final int id;
    private final int clanId;
    private final UUID playerUuid;
    private String playerName;
    private ClanRole role;
    private final LocalDateTime joinedAt;

    public ClanMember(int id, int clanId, UUID playerUuid, String playerName, ClanRole role, LocalDateTime joinedAt) {
        this.id = id;
        this.clanId = clanId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    // Getters
    public int getId() { return id; }
    public int getClanId() { return clanId; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public ClanRole getRole() { return role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }

    // Setters
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setRole(ClanRole role) { this.role = role; }

    // Utility methods
    public Player getPlayer() {
        return Bukkit.getPlayer(playerUuid);
    }

    public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }

    public boolean hasPermission(String permission) {
        Player player = getPlayer();
        return player != null && player.hasPermission(permission);
    }

    public boolean canInvite() {
        return role == ClanRole.LEADER || role == ClanRole.OFFICER;
    }

    public boolean canKick() {
        return role == ClanRole.LEADER || role == ClanRole.OFFICER;
    }

    public boolean canPromote() {
        return role == ClanRole.LEADER;
    }

    public boolean canManageTerritory() {
        return role == ClanRole.LEADER || role == ClanRole.OFFICER;
    }

    @Override
    public String toString() {
        return String.format("ClanMember{name='%s', role=%s, online=%s}",
                playerName, role, isOnline());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClanMember that = (ClanMember) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}