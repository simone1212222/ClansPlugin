package com.develop.clansPlugin.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ClanInvite {

    private final int id;
    private final int clanId;
    private final UUID inviterUuid;
    private final UUID invitedUuid;
    private final String invitedName;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    public ClanInvite(int id, int clanId, UUID inviterUuid, UUID invitedUuid,
                      String invitedName, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.id = id;
        this.clanId = clanId;
        this.inviterUuid = inviterUuid;
        this.invitedUuid = invitedUuid;
        this.invitedName = invitedName;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // Getters
    public int getId() { return id; }
    public int getClanId() { return clanId; }
    public UUID getInviterUuid() { return inviterUuid; }
    public UUID getInvitedUuid() { return invitedUuid; }
    public String getInvitedName() { return invitedName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    // Utility methods
    public Player getInviter() {
        return Bukkit.getPlayer(inviterUuid);
    }

    public Player getInvited() {
        return Bukkit.getPlayer(invitedUuid);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired();
    }

    @Override
    public String toString() {
        return String.format("ClanInvite{id=%d, clanId=%d, invited='%s', expires=%s}",
                id, clanId, invitedName, expiresAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClanInvite that = (ClanInvite) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}