package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final SlownVectur plugin;
    private final Map<UUID, PlayerData> playerDataCache;
    private final Map<UUID, Long> joinTimes;
    private final Set<UUID> dirtyPlayers;
    private BukkitTask saveTask;

    public PlayerDataManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<>();
        this.joinTimes = new ConcurrentHashMap<>();
        this.dirtyPlayers = ConcurrentHashMap.newKeySet();
        startPeriodicSave();
    }

    private void startPeriodicSave() {
        saveTask = new BukkitRunnable() {
            @Override
            public void run() {
                saveAllDirtyPlayerData();
            }
        }.runTaskTimerAsynchronously(plugin, 6000L, 6000L);
    }

    public CompletableFuture<Void> loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        joinTimes.put(uuid, System.currentTimeMillis());

        return plugin.getDatabaseManager().loadPlayerData(uuid, player.getName())
                .thenAccept(data -> {
                    data.setLastJoin(System.currentTimeMillis());
                    data.setDataLoaded(true);
                    playerDataCache.put(uuid, data);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getPrefixManager().updatePlayer(player);
                            plugin.getTablistManager().updatePlayer(player);
                            plugin.getScoreboardManager().updateBoard(player);
                        }
                    }.runTask(plugin);
                });
    }

    public void savePlayerData(Player player) {
        savePlayerData(player.getUniqueId());
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data == null) return;

        Long joinTime = joinTimes.get(uuid);
        if (joinTime != null) {
            long sessionTime = System.currentTimeMillis() - joinTime;
            data.addPlayTime(sessionTime);
            joinTimes.put(uuid, System.currentTimeMillis());
        }

        plugin.getDatabaseManager().savePlayerData(data);
        dirtyPlayers.remove(uuid);
    }

    private void saveAllDirtyPlayerData() {
        if (dirtyPlayers.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        for (UUID uuid : dirtyPlayers) {
            PlayerData data = playerDataCache.get(uuid);
            if (data == null) continue;

            Long joinTime = joinTimes.get(uuid);
            if (joinTime != null) {
                long sessionTime = currentTime - joinTime;
                data.addPlayTime(sessionTime);
                joinTimes.put(uuid, currentTime);
            }

            plugin.getDatabaseManager().savePlayerData(data);
        }

        dirtyPlayers.clear();
    }

    public void markDirty(UUID uuid) {
        dirtyPlayers.add(uuid);
    }

    public void unloadPlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            Long joinTime = joinTimes.get(uuid);
            if (joinTime != null) {
                long sessionTime = System.currentTimeMillis() - joinTime;
                data.addPlayTime(sessionTime);
                plugin.getDatabaseManager().savePlayerData(data);
            }
        }

        playerDataCache.remove(uuid);
        joinTimes.remove(uuid);
        dirtyPlayers.remove(uuid);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public boolean hasPlayerData(UUID uuid) {
        return playerDataCache.containsKey(uuid);
    }

    public boolean isDataLoaded(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        return data != null && data.isDataLoaded();
    }

    public void saveAllPlayerData() {
        long currentTime = System.currentTimeMillis();

        for (UUID uuid : playerDataCache.keySet()) {
            PlayerData data = playerDataCache.get(uuid);
            if (data == null) continue;

            Long joinTime = joinTimes.get(uuid);
            if (joinTime != null) {
                long sessionTime = currentTime - joinTime;
                data.addPlayTime(sessionTime);
                joinTimes.put(uuid, currentTime);
            }

            plugin.getDatabaseManager().savePlayerData(data);
        }

        dirtyPlayers.clear();
    }

    public void shutdown() {
        if (saveTask != null) {
            saveTask.cancel();
        }
        saveAllPlayerData();
    }
}