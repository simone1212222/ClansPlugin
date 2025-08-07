package com.develop.clansPlugin.models;

import org.bukkit.Location;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record Territory(int id, int clanId, String worldName, int centerX, int centerZ, int radius,
                        LocalDateTime claimedAt, UUID claimedBy) {

    public boolean contains(Location location) {
        if (location == null || !worldName.equals(Objects.requireNonNull(location.getWorld()).getName())) {
            return false;
        }
        double dx = location.getX() - centerX;
        double dz = location.getZ() - centerZ;
        return (dx * dx + dz * dz) <= (radius * radius);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Territory territory = (Territory) obj;
        return id == territory.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
