package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.data.Portal;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PortalManager {

    private final SlownVectur plugin;
    private final Map<String, Portal> portals;
    private final Map<UUID, Long> portalCooldowns;
    private final Set<UUID> playersInPortals;
    private File portalsFile;
    private FileConfiguration portalsConfig;
    private BukkitTask particleTask;
    private BukkitTask portalCheckTask;

    public PortalManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.portals = new ConcurrentHashMap<>();
        this.portalCooldowns = new ConcurrentHashMap<>();
        this.playersInPortals = ConcurrentHashMap.newKeySet();
        initializeConfig();
        loadPortals();
        startTasks();
    }

    private void initializeConfig() {
        portalsFile = new File(plugin.getDataFolder(), "portals.yml");
        if (!portalsFile.exists()) {
            try {
                plugin.saveResource("portals.yml", false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().info("Creating default portals.yml...");
                try {
                    portalsFile.getParentFile().mkdirs();
                    portalsFile.createNewFile();
                } catch (Exception ex) {
                    plugin.getLogger().severe("Could not create portals.yml: " + ex.getMessage());
                }
            }
        }
        portalsConfig = YamlConfiguration.loadConfiguration(portalsFile);
    }

    public void saveConfig() {
        try {
            portalsConfig.save(portalsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save portals.yml: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        portalsConfig = YamlConfiguration.loadConfiguration(portalsFile);
        loadPortals();
    }

    private void loadPortals() {
        portals.clear();
        ConfigurationSection portalsSection = portalsConfig.getConfigurationSection("portals");
        if (portalsSection == null) return;

        for (String portalName : portalsSection.getKeys(false)) {
            try {
                Portal portal = loadPortalFromConfig(portalName, portalsSection.getConfigurationSection(portalName));
                if (portal != null) {
                    portals.put(portalName, portal);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load portal " + portalName + ": " + e.getMessage());
            }
        }
    }

    private Portal loadPortalFromConfig(String name, ConfigurationSection section) {
        if (section == null) return null;

        Portal portal = new Portal(name);

        if (section.contains("corner1")) {
            portal.setCorner1(deserializeLocation(section.getConfigurationSection("corner1")));
        }
        if (section.contains("corner2")) {
            portal.setCorner2(deserializeLocation(section.getConfigurationSection("corner2")));
        }
        if (section.contains("teleport-location")) {
            portal.setTeleportLocation(deserializeLocation(section.getConfigurationSection("teleport-location")));
        }

        portal.setPermission(section.getString("permission"));
        portal.setEnabled(section.getBoolean("enabled", true));

        String actionTypeStr = section.getString("action.type", "TELEPORT");
        try {
            portal.setActionType(Portal.ActionType.valueOf(actionTypeStr));
        } catch (IllegalArgumentException e) {
            portal.setActionType(Portal.ActionType.TELEPORT);
        }
        portal.setActionValue(section.getString("action.value"));

        String frameParticleStr = section.getString("particles.frame", "PORTAL");
        String innerParticleStr = section.getString("particles.inner", "NAUTILUS");
        try {
            portal.setFrameParticle(Particle.valueOf(frameParticleStr));
            portal.setInnerParticle(Particle.valueOf(innerParticleStr));
        } catch (IllegalArgumentException e) {
            portal.setFrameParticle(Particle.PORTAL);
            portal.setInnerParticle(Particle.NAUTILUS);
        }

        portal.setParticleSpeed(section.getInt("particles.speed", 1));
        portal.setParticleCount(section.getInt("particles.count", 1));
        portal.setParticleSpacing(section.getDouble("particles.spacing", 0.3));

        if (section.contains("schedule")) {
            ConfigurationSection scheduleSection = section.getConfigurationSection("schedule");

            List<String> daysList = scheduleSection.getStringList("days");
            if (!daysList.isEmpty()) {
                Set<DayOfWeek> allowedDays = EnumSet.noneOf(DayOfWeek.class);
                for (String day : daysList) {
                    try {
                        allowedDays.add(DayOfWeek.valueOf(day.toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                }
                portal.setAllowedDays(allowedDays);
            }

            String startTimeStr = scheduleSection.getString("start-time");
            String endTimeStr = scheduleSection.getString("end-time");
            if (startTimeStr != null && endTimeStr != null) {
                try {
                    portal.setStartTime(LocalTime.parse(startTimeStr));
                    portal.setEndTime(LocalTime.parse(endTimeStr));
                } catch (Exception ignored) {}
            }
        }

        return portal;
    }

    private void savePortalToConfig(Portal portal) {
        String path = "portals." + portal.getName();

        if (portal.getCorner1() != null) {
            serializeLocation(portal.getCorner1(), portalsConfig.createSection(path + ".corner1"));
        }
        if (portal.getCorner2() != null) {
            serializeLocation(portal.getCorner2(), portalsConfig.createSection(path + ".corner2"));
        }
        if (portal.getTeleportLocation() != null) {
            serializeLocation(portal.getTeleportLocation(), portalsConfig.createSection(path + ".teleport-location"));
        }

        portalsConfig.set(path + ".permission", portal.getPermission());
        portalsConfig.set(path + ".enabled", portal.isEnabled());
        portalsConfig.set(path + ".action.type", portal.getActionType().name());
        portalsConfig.set(path + ".action.value", portal.getActionValue());

        portalsConfig.set(path + ".particles.frame", portal.getFrameParticle().name());
        portalsConfig.set(path + ".particles.inner", portal.getInnerParticle().name());
        portalsConfig.set(path + ".particles.speed", portal.getParticleSpeed());
        portalsConfig.set(path + ".particles.count", portal.getParticleCount());
        portalsConfig.set(path + ".particles.spacing", portal.getParticleSpacing());

        if (portal.getAllowedDays() != null) {
            List<String> daysList = new ArrayList<>();
            for (DayOfWeek day : portal.getAllowedDays()) {
                daysList.add(day.name());
            }
            portalsConfig.set(path + ".schedule.days", daysList);
        }

        if (portal.getStartTime() != null) {
            portalsConfig.set(path + ".schedule.start-time", portal.getStartTime().toString());
        }
        if (portal.getEndTime() != null) {
            portalsConfig.set(path + ".schedule.end-time", portal.getEndTime().toString());
        }

        saveConfig();
    }

    private void serializeLocation(Location location, ConfigurationSection section) {
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }

    private Location deserializeLocation(ConfigurationSection section) {
        if (section == null) return null;

        return new Location(
                plugin.getServer().getWorld(section.getString("world")),
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch")
        );
    }

    public void createPortal(String name, Location corner1, Location corner2) {
        Portal portal = new Portal(name);
        portal.setCorner1(corner1);
        portal.setCorner2(corner2);
        portals.put(name, portal);
        savePortalToConfig(portal);
    }

    public void deletePortal(String name) {
        portals.remove(name);
        portalsConfig.set("portals." + name, null);
        saveConfig();
    }

    public Portal getPortal(String name) {
        return portals.get(name);
    }

    public Collection<Portal> getAllPortals() {
        return portals.values();
    }

    public Portal getPortalAtLocation(Location location) {
        for (Portal portal : portals.values()) {
            if (portal.isInPortal(location)) {
                return portal;
            }
        }
        return null;
    }

    public void updatePortal(Portal portal) {
        portals.put(portal.getName(), portal);
        savePortalToConfig(portal);
    }

    public boolean hasPortal(String name) {
        return portals.containsKey(name);
    }

    private void startTasks() {
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Portal portal : portals.values()) {
                    if (portal.isEnabled() && portal.getCorner1() != null && portal.getCorner2() != null) {
                        boolean hasNearbyPlayers = false;
                        for (Player player : plugin.getServer().getOnlinePlayers()) {
                            if (player.getWorld() == portal.getCorner1().getWorld()) {
                                double distance = player.getLocation().distance(portal.getCorner1());
                                if (distance < 50) {
                                    hasNearbyPlayers = true;
                                    break;
                                }
                            }
                        }

                        if (hasNearbyPlayers) {
                            spawnPortalParticles(portal);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);

        portalCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    checkPlayerPortalInteraction(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void spawnPortalParticles(Portal portal) {
        Location corner1 = portal.getCorner1();
        Location corner2 = portal.getCorner2();

        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        double spacing = portal.getParticleSpacing();
        
        for (double x = minX; x <= maxX; x += spacing) {
            for (double y = minY; y <= maxY; y += spacing) {
                for (double z = minZ; z <= maxZ; z += spacing) {
                    boolean isFrame = false;

                    if (Math.abs(x - minX) < 0.1 || Math.abs(x - maxX) < 0.1 ||
                            Math.abs(y - minY) < 0.1 || Math.abs(y - maxY) < 0.1 ||
                            Math.abs(z - minZ) < 0.1 || Math.abs(z - maxZ) < 0.1) {
                        isFrame = true;
                    }

                    Location particleLocation = new Location(corner1.getWorld(), x, y, z);

                    if (isFrame) {
                        corner1.getWorld().spawnParticle(portal.getFrameParticle(), particleLocation,
                                portal.getParticleCount(), 0, 0, 0, portal.getParticleSpeed() * 0.01);
                    } else {
                        if (Math.random() < 0.3) {
                            corner1.getWorld().spawnParticle(portal.getInnerParticle(), particleLocation,
                                    1, 0.2, 0.2, 0.2, portal.getParticleSpeed() * 0.01);
                        }
                    }
                }
            }
        }
    }

    private void checkPlayerPortalInteraction(Player player) {
        Portal portal = getPortalAtLocation(player.getLocation());
        UUID playerId = player.getUniqueId();

        if (portal != null) {
            if (!playersInPortals.contains(playerId)) {
                playersInPortals.add(playerId);
                handlePortalEntry(player, portal);
            }
        } else {
            playersInPortals.remove(playerId);
        }
    }

    private void handlePortalEntry(Player player, Portal portal) {
        if (!portal.isEnabled() || !portal.isTimeAllowed()) {
            return;
        }

        if (portal.getPermission() != null && !player.hasPermission(portal.getPermission())) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long cooldownTime = portalCooldowns.getOrDefault(playerId, 0L);
        if (System.currentTimeMillis() - cooldownTime < 3000) {
            return;
        }

        portalCooldowns.put(playerId, System.currentTimeMillis());

        switch (portal.getActionType()) {
            case TELEPORT -> {
                if (portal.getTeleportLocation() != null) {
                    player.teleport(portal.getTeleportLocation());
                }
            }
            case COMMAND -> {
                if (portal.getActionValue() != null) {
                    String command = portal.getActionValue().replace("{player}", player.getName());
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                }
            }
            case SERVER -> {
                if (portal.getActionValue() != null) {

                }
            }
            case WORLD -> {
                if (portal.getActionValue() != null) {
                    org.bukkit.World world = plugin.getServer().getWorld(portal.getActionValue());
                    if (world != null) {
                        player.teleport(world.getSpawnLocation());
                    }
                }
            }
        }
    }

    public void shutdown() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        if (portalCheckTask != null) {
            portalCheckTask.cancel();
        }
    }
}