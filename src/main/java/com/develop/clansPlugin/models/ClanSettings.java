package com.develop.clansPlugin.models;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ClanSettings {

    private final int clanId;
    private boolean allowBuild;
    private boolean allowPvp;
    private boolean allowMobSpawning;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    public ClanSettings(int clanId, boolean allowBuild, boolean allowPvp, boolean allowMobSpawning,
                        LocalDateTime updatedAt, UUID updatedBy) {
        this.clanId = clanId;
        this.allowBuild = allowBuild;
        this.allowPvp = allowPvp;
        this.allowMobSpawning = allowMobSpawning;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public ClanSettings(int clanId, UUID updatedBy) {
        this(clanId, true, false, true, LocalDateTime.now(), updatedBy);
    }

    public int getClanId() { return clanId; }
    public boolean isAllowBuild() { return allowBuild; }
    public boolean isAllowPvp() { return allowPvp; }
    public boolean isAllowMobSpawning() { return allowMobSpawning; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setAllowBuild(boolean allowBuild) {
        this.allowBuild = allowBuild;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAllowPvp(boolean allowPvp) {
        this.allowPvp = allowPvp;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAllowMobSpawning(boolean allowMobSpawning) {
        this.allowMobSpawning = allowMobSpawning;
        this.updatedAt = LocalDateTime.now();
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("ClanSettings{clanId=%d, build=%s, pvp=%s, mobs=%s}",
                clanId, allowBuild, allowPvp, allowMobSpawning);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClanSettings that = (ClanSettings) obj;
        return clanId == that.clanId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clanId);
    }
}