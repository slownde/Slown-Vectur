package de.syscall.data;

import org.bukkit.Location;
import org.bukkit.Particle;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Portal {

    private final String name;
    private Location corner1;
    private Location corner2;
    private Location teleportLocation;
    private String permission;
    private ActionType actionType;
    private String actionValue;
    private Particle frameParticle;
    private Particle innerParticle;
    private boolean enabled;
    private Set<DayOfWeek> allowedDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private int particleSpeed;
    private int particleCount;
    private double particleSpacing;

    public Portal(String name) {
        this.name = name;
        this.enabled = true;
        this.frameParticle = Particle.PORTAL;
        this.innerParticle = Particle.ENCHANT;
        this.actionType = ActionType.TELEPORT;
        this.particleSpeed = 1;
        this.particleCount = 5;
        this.particleSpacing = 0.5;
    }

    public Portal(String name, Location corner1, Location corner2, Location teleportLocation,
                  String permission, ActionType actionType, String actionValue,
                  Particle frameParticle, Particle innerParticle, boolean enabled,
                  Set<DayOfWeek> allowedDays, LocalTime startTime, LocalTime endTime,
                  int particleSpeed, int particleCount, double particleSpacing) {
        this.name = name;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.teleportLocation = teleportLocation;
        this.permission = permission;
        this.actionType = actionType;
        this.actionValue = actionValue;
        this.frameParticle = frameParticle;
        this.innerParticle = innerParticle;
        this.enabled = enabled;
        this.allowedDays = allowedDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.particleSpeed = particleSpeed;
        this.particleCount = particleCount;
        this.particleSpacing = particleSpacing;
    }

    public String getName() {
        return name;
    }

    public Location getCorner1() {
        return corner1;
    }

    public void setCorner1(Location corner1) {
        this.corner1 = corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public void setCorner2(Location corner2) {
        this.corner2 = corner2;
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

    public Particle getFrameParticle() {
        return frameParticle;
    }

    public void setFrameParticle(Particle frameParticle) {
        this.frameParticle = frameParticle;
    }

    public Particle getInnerParticle() {
        return innerParticle;
    }

    public void setInnerParticle(Particle innerParticle) {
        this.innerParticle = innerParticle;
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

    public int getParticleSpeed() {
        return particleSpeed;
    }

    public void setParticleSpeed(int particleSpeed) {
        this.particleSpeed = particleSpeed;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public void setParticleCount(int particleCount) {
        this.particleCount = particleCount;
    }

    public double getParticleSpacing() {
        return particleSpacing;
    }

    public void setParticleSpacing(double particleSpacing) {
        this.particleSpacing = particleSpacing;
    }

    public boolean isInPortal(Location location) {
        if (corner1 == null || corner2 == null || location.getWorld() != corner1.getWorld()) {
            return false;
        }

        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public boolean isTimeAllowed() {
        if (allowedDays == null && startTime == null && endTime == null) {
            return true;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (allowedDays != null && !allowedDays.contains(now.getDayOfWeek())) {
            return false;
        }

        if (startTime != null && endTime != null) {
            java.time.LocalTime currentTime = now.toLocalTime();
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