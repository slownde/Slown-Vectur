package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDataManager {

    private final SlownVectur plugin;
    private final Map<UUID, PlayerData> playerDataCache;
    private final Map<UUID, Long> joinTimes;

    public PlayerDataManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.playerDataCache = new HashMap<>();
        this.joinTimes = new HashMap<>();
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
        UUID uuid = player.getUniqueId();
        PlayerData data = playerDataCache.get(uuid);

        if (data != null) {
            if (joinTimes.containsKey(uuid)) {
                long sessionTime = System.currentTimeMillis() - joinTimes.get(uuid);
                data.addPlayTime(sessionTime);
                joinTimes.remove(uuid);
            }

            plugin.getDatabaseManager().savePlayerData(data);
        }
    }

    public void unloadPlayerData(UUID uuid) {
        playerDataCache.remove(uuid);
        joinTimes.remove(uuid);
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
        for (UUID uuid : playerDataCache.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                savePlayerData(player);
            }
        }
    }
}