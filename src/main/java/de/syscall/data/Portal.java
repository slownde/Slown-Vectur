package de.syscall.data;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.BoundingBox;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class Portal {

    private final String name;
    private BoundingBox bounds;
    private Location teleportLocation;
    private String permission;
    private ActionType actionType;
    private String actionValue;
    private Particle particle;
    private boolean enabled;
    private Set<DayOfWeek> allowedDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private double particleSpacing;
    private int particleDensity;

    public Portal(String name) {
        this.name = name;
        this.enabled = true;
        this.particle = Particle.PORTAL;
        this.actionType = ActionType.TELEPORT;
        this.particleSpacing = 0.5;
        this.particleDensity = 1;
    }

    public String getName() {
        return name;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public void setBounds(Location corner1, Location corner2) {
        if (corner1 != null && corner2 != null && corner1.getWorld().equals(corner2.getWorld())) {
            double minX = Math.min(corner1.getX(), corner2.getX());
            double maxX = Math.max(corner1.getX(), corner2.getX());
            double minY = Math.min(corner1.getY(), corner2.getY());
            double maxY = Math.max(corner1.getY(), corner2.getY());
            double minZ = Math.min(corner1.getZ(), corner2.getZ());
            double maxZ = Math.max(corner1.getZ(), corner2.getZ());

            this.bounds = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    public Location getTeleportLocation() {
        return teleportLocation;
    }

    public void setTeleportLocation(Location teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getActionValue() {
        return actionValue;
    }

    public void setActionValue(String actionValue) {
        this.actionValue = actionValue;
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<DayOfWeek> getAllowedDays() {
        return allowedDays;
    }

    public void setAllowedDays(Set<DayOfWeek> allowedDays) {
        this.allowedDays = allowedDays;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public double getParticleSpacing() {
        return particleSpacing;
    }

    public void setParticleSpacing(double particleSpacing) {
        this.particleSpacing = Math.max(0.1, particleSpacing);
    }

    public int getParticleDensity() {
        return particleDensity;
    }

    public void setParticleDensity(int particleDensity) {
        this.particleDensity = Math.max(1, particleDensity);
    }

    public boolean contains(Location location) {
        return bounds != null && location.getWorld() != null &&
                bounds.contains(location.getX(), location.getY(), location.getZ());
    }

    public boolean isTimeAllowed() {
        LocalDateTime now = LocalDateTime.now();

        if (allowedDays != null && !allowedDays.contains(now.getDayOfWeek())) {
            return false;
        }

        if (startTime != null && endTime != null) {
            LocalTime currentTime = now.toLocalTime();
            if (startTime.isBefore(endTime)) {
                return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
            } else {
                return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
            }
        }

        return true;
    }

    public enum ActionType {
        TELEPORT,
        COMMAND,
        SERVER,
        WORLD
    }
}