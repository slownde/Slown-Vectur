package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.data.Portal;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PortalManager {

    private final SlownVectur plugin;
    private final Map<String, Portal> portals;
    private final Map<UUID, Long> cooldowns;
    private final Set<UUID> playersInPortals;
    private File portalsFile;
    private FileConfiguration config;
    private BukkitTask particleTask;
    private BukkitTask detectionTask;

    public PortalManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.portals = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.playersInPortals = ConcurrentHashMap.newKeySet();

        initializeConfig();
        loadPortals();
        startTasks();
    }

    private void initializeConfig() {
        portalsFile = new File(plugin.getDataFolder(), "portals.yml");
        if (!portalsFile.exists()) {
            try {
                portalsFile.getParentFile().mkdirs();
                portalsFile.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().severe("Could not create portals.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(portalsFile);
    }

    private void loadPortals() {
        portals.clear();
        ConfigurationSection section = config.getConfigurationSection("portals");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            try {
                Portal portal = loadPortal(name, section.getConfigurationSection(name));
                if (portal != null) {
                    portals.put(name, portal);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load portal " + name + ": " + e.getMessage());
            }
        }
    }

    private Portal loadPortal(String name, ConfigurationSection section) {
        if (section == null) return null;

        Portal portal = new Portal(name);

        Location corner1 = loadLocation(section.getConfigurationSection("corner1"));
        Location corner2 = loadLocation(section.getConfigurationSection("corner2"));
        if (corner1 != null && corner2 != null) {
            portal.setBounds(corner1, corner2);
        }

        Location teleport = loadLocation(section.getConfigurationSection("teleport"));
        if (teleport != null) {
            portal.setTeleportLocation(teleport);
        }

        portal.setPermission(section.getString("permission"));
        portal.setEnabled(section.getBoolean("enabled", true));
        portal.setActionValue(section.getString("action.value"));

        try {
            portal.setActionType(Portal.ActionType.valueOf(section.getString("action.type", "TELEPORT")));
        } catch (IllegalArgumentException ignored) {}

        try {
            portal.setParticle(Particle.valueOf(section.getString("particle", "PORTAL")));
        } catch (IllegalArgumentException ignored) {}

        portal.setParticleSpacing(section.getDouble("spacing", 0.5));
        portal.setParticleDensity(section.getInt("density", 1));

        loadSchedule(portal, section.getConfigurationSection("schedule"));

        return portal;
    }

    private void loadSchedule(Portal portal, ConfigurationSection section) {
        if (section == null) return;

        List<String> daysList = section.getStringList("days");
        if (!daysList.isEmpty()) {
            Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
            for (String day : daysList) {
                try {
                    days.add(DayOfWeek.valueOf(day.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
            portal.setAllowedDays(days);
        }

        String start = section.getString("start");
        String end = section.getString("end");
        if (start != null && end != null) {
            try {
                portal.setStartTime(LocalTime.parse(start));
                portal.setEndTime(LocalTime.parse(end));
            } catch (Exception ignored) {}
        }
    }

    private Location loadLocation(ConfigurationSection section) {
        if (section == null) return null;

        World world = plugin.getServer().getWorld(section.getString("world"));
        if (world == null) return null;

        return new Location(
                world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw", 0),
                (float) section.getDouble("pitch", 0)
        );
    }

    public void createPortal(String name, Location corner1, Location corner2) {
        Portal portal = new Portal(name);
        portal.setBounds(corner1, corner2);
        portals.put(name, portal);
        savePortal(portal);
    }

    public void deletePortal(String name) {
        portals.remove(name);
        config.set("portals." + name, null);
        saveConfig();
    }

    public Portal getPortal(String name) {
        return portals.get(name);
    }

    public Collection<Portal> getAllPortals() {
        return portals.values();
    }

    public Portal getPortalAt(Location location) {
        return portals.values().stream()
                .filter(Portal::isEnabled)
                .filter(portal -> portal.contains(location))
                .findFirst()
                .orElse(null);
    }

    public void updatePortal(Portal portal) {
        portals.put(portal.getName(), portal);
        savePortal(portal);
    }

    public boolean hasPortal(String name) {
        return portals.containsKey(name);
    }

    private void savePortal(Portal portal) {
        String path = "portals." + portal.getName();

        BoundingBox bounds = portal.getBounds();
        if (bounds != null) {
            saveLocation(path + ".corner1", new Location(null, bounds.getMinX(), bounds.getMinY(), bounds.getMinZ()));
            saveLocation(path + ".corner2", new Location(null, bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ()));
        }

        Location teleport = portal.getTeleportLocation();
        if (teleport != null) {
            saveLocation(path + ".teleport", teleport);
        }

        config.set(path + ".permission", portal.getPermission());
        config.set(path + ".enabled", portal.isEnabled());
        config.set(path + ".action.type", portal.getActionType().name());
        config.set(path + ".action.value", portal.getActionValue());
        config.set(path + ".particle", portal.getParticle().name());
        config.set(path + ".spacing", portal.getParticleSpacing());
        config.set(path + ".density", portal.getParticleDensity());

        if (portal.getAllowedDays() != null) {
            List<String> days = new ArrayList<>();
            portal.getAllowedDays().forEach(day -> days.add(day.name()));
            config.set(path + ".schedule.days", days);
        }

        if (portal.getStartTime() != null) {
            config.set(path + ".schedule.start", portal.getStartTime().toString());
        }
        if (portal.getEndTime() != null) {
            config.set(path + ".schedule.end", portal.getEndTime().toString());
        }

        saveConfig();
    }

    private void saveLocation(String path, Location location) {
        if (location.getWorld() != null) {
            config.set(path + ".world", location.getWorld().getName());
        }
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }

    private void saveConfig() {
        try {
            config.save(portalsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save portals.yml: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(portalsFile);
        loadPortals();
    }

    private void startTasks() {
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnParticles();
            }
        }.runTaskTimer(plugin, 0L, 8L);

        detectionTask = new BukkitRunnable() {
            @Override
            public void run() {
                detectPlayers();
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void spawnParticles() {
        for (Portal portal : portals.values()) {
            if (!portal.isEnabled() || portal.getBounds() == null) continue;

            BoundingBox bounds = portal.getBounds();
            World world = getWorldFromBounds(bounds);
            if (world == null || !hasNearbyPlayers(world, bounds)) continue;

            spawnPortalParticles(world, bounds, portal);
        }
    }

    private World getWorldFromBounds(BoundingBox bounds) {
        for (World world : plugin.getServer().getWorlds()) {
            for (Portal portal : portals.values()) {
                if (portal.getBounds() == bounds && portal.getTeleportLocation() != null) {
                    return portal.getTeleportLocation().getWorld();
                }
            }
        }
        return plugin.getServer().getWorlds().getFirst();
    }

    private boolean hasNearbyPlayers(World world, BoundingBox bounds) {
        Location center = new Location(world,
                (bounds.getMinX() + bounds.getMaxX()) / 2,
                (bounds.getMinY() + bounds.getMaxY()) / 2,
                (bounds.getMinZ() + bounds.getMaxZ()) / 2);

        return world.getPlayers().stream()
                .anyMatch(player -> player.getLocation().distanceSquared(center) < 2500);
    }

    private void spawnPortalParticles(World world, BoundingBox bounds, Portal portal) {
        double spacing = portal.getParticleSpacing();
        int density = portal.getParticleDensity();

        for (double x = bounds.getMinX(); x <= bounds.getMaxX(); x += spacing) {
            for (double y = bounds.getMinY(); y <= bounds.getMaxY(); y += spacing) {
                for (double z = bounds.getMinZ(); z <= bounds.getMaxZ(); z += spacing) {
                    if (isEdge(x, y, z, bounds, spacing)) {
                        Location loc = new Location(world, x, y, z);
                        world.spawnParticle(portal.getParticle(), loc, density, 0.1, 0.1, 0.1, 0.01);
                    }
                }
            }
        }
    }

    private boolean isEdge(double x, double y, double z, BoundingBox bounds, double spacing) {
        return Math.abs(x - bounds.getMinX()) < spacing || Math.abs(x - bounds.getMaxX()) < spacing ||
                Math.abs(y - bounds.getMinY()) < spacing || Math.abs(y - bounds.getMaxY()) < spacing ||
                Math.abs(z - bounds.getMinZ()) < spacing || Math.abs(z - bounds.getMaxZ()) < spacing;
    }

    private void detectPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Portal portal = getPortalAt(player.getLocation());

            if (portal != null) {
                if (!playersInPortals.contains(uuid)) {
                    playersInPortals.add(uuid);
                    handlePortalEntry(player, portal);
                }
            } else {
                playersInPortals.remove(uuid);
            }
        }
    }

    private void handlePortalEntry(Player player, Portal portal) {
        if (!portal.isTimeAllowed()) return;
        if (portal.getPermission() != null && !player.hasPermission(portal.getPermission())) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - cooldowns.getOrDefault(uuid, 0L) < 2000) return;

        cooldowns.put(uuid, now);

        switch (portal.getActionType()) {
            case TELEPORT:
                if (portal.getTeleportLocation() != null) {
                    player.teleport(portal.getTeleportLocation());
                }
                break;
            case COMMAND:
                if (portal.getActionValue() != null) {
                    String command = portal.getActionValue().replace("{player}", player.getName());
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                }
                break;
            case WORLD:
                if (portal.getActionValue() != null) {
                    World world = plugin.getServer().getWorld(portal.getActionValue());
                    if (world != null) {
                        player.teleport(world.getSpawnLocation());
                    }
                }
                break;
        }
    }

    public void shutdown() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        if (detectionTask != null) {
            detectionTask.cancel();
        }
    }
}