package com.develop.clansPlugin.models;

public enum ClanRole {
    LEADER("Leader", 3),
    OFFICER("Officer", 2),
    MEMBER("Member", 1);

    private final String displayName;
    private final int priority;

    ClanRole(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canKick(ClanRole targetRole) {
        return this.priority > targetRole.priority;
    }

    public static ClanRole fromString(String role) {
        try {
            return ClanRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEMBER;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}