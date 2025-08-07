package com.develop.clansPlugin.models;

import org.bukkit.Location;
import java.time.LocalDateTime;
import java.util.*;

public class Clan {

    private final int id;
    private final String name;
    private final String tag;
    private UUID leaderUuid;
    private final LocalDateTime createdAt;
    private Location home;
    private final Map<UUID, ClanMember> members;

    public Clan(int id, String name, String tag, UUID leaderUuid, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.leaderUuid = leaderUuid;
        this.createdAt = createdAt;
        this.members = new HashMap<>();
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getTag() { return tag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Location getHome() { return home; }
    public Map<UUID, ClanMember> getMembers() { return members; }

    // Setters
    public void setLeaderUuid(UUID leaderUuid) { this.leaderUuid = leaderUuid; }
    public void setHome(Location home) { this.home = home; }

    // Member management
    public void addMember(ClanMember member) {
        members.put(member.getPlayerUuid(), member);
    }

    public void removeMember(UUID playerUuid) {
        members.remove(playerUuid);
    }

    public ClanMember getMember(UUID playerUuid) {
        return members.get(playerUuid);
    }

    public boolean hasMember(UUID playerUuid) {
        return members.containsKey(playerUuid);
    }

    public List<ClanMember> getMembersByRole(ClanRole role) {
        return members.values().stream()
                .filter(member -> member.getRole() == role)
                .toList();
    }

    public List<ClanMember> getOnlineMembers() {
        return members.values().stream()
                .filter(member -> member.getPlayer() != null && member.getPlayer().isOnline())
                .toList();
    }

    public int getOnlineMemberCount() {
        return getOnlineMembers().size();
    }

    public int getTotalMemberCount() {
        return members.size();
    }

    public boolean isLeader(UUID playerUuid) {
        return leaderUuid.equals(playerUuid);
    }

    @Override
    public String toString() {
        return String.format("Clan{id=%d, name='%s', tag='%s', members=%d}",
                id, name, tag, members.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Clan clan = (Clan) obj;
        return id == clan.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}