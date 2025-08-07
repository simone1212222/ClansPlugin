package com.develop.clansPlugin.models;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record ClanInvite(int id, int clanId, UUID inviterUuid, UUID invitedUuid, String invitedName,
                         LocalDateTime createdAt, LocalDateTime expiresAt) {

    // Utility methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    @Override @NotNull
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