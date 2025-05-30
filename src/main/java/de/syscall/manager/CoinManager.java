package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CoinManager {

    private final SlownVectur plugin;

    public CoinManager(SlownVectur plugin) {
        this.plugin = plugin;
    }

    public int getCoins(Player player) {
        return getCoins(player.getUniqueId());
    }

    public int getCoins(UUID uuid) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        return data != null ? data.getCoins() : 0;
    }

    public void setCoins(Player player, int amount) {
        setCoins(player.getUniqueId(), amount);
    }

    public void setCoins(UUID uuid, int amount) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null) {
            data.setCoins(Math.max(0, amount));
            plugin.getDatabaseManager().savePlayerData(data);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                plugin.getScoreboardManager().updateBoard(player);
            }
        }
    }

    public void addCoins(Player player, int amount) {
        addCoins(player.getUniqueId(), amount);
    }

    public void addCoins(UUID uuid, int amount) {
        if (amount <= 0) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null) {
            data.addCoins(amount);
            plugin.getDatabaseManager().savePlayerData(data);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                plugin.getScoreboardManager().updateBoard(player);
            }
        }
    }

    public boolean removeCoins(Player player, int amount) {
        return removeCoins(player.getUniqueId(), amount);
    }

    public boolean removeCoins(UUID uuid, int amount) {
        if (amount <= 0) return false;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null && data.getCoins() >= amount) {
            data.removeCoins(amount);
            plugin.getDatabaseManager().savePlayerData(data);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                plugin.getScoreboardManager().updateBoard(player);
            }
            return true;
        }
        return false;
    }

    public boolean hasCoins(Player player, int amount) {
        return hasCoins(player.getUniqueId(), amount);
    }

    public boolean hasCoins(UUID uuid, int amount) {
        return getCoins(uuid) >= amount;
    }

    public int getBankCoins(Player player) {
        return getBankCoins(player.getUniqueId());
    }

    public int getBankCoins(UUID uuid) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        return data != null ? data.getBankCoins() : 0;
    }

    public boolean depositCoins(Player player, int amount) {
        return depositCoins(player.getUniqueId(), amount);
    }

    public boolean depositCoins(UUID uuid, int amount) {
        if (amount <= 0) return false;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null && data.getCoins() >= amount) {
            data.removeCoins(amount);
            data.addBankCoins(amount);
            plugin.getDatabaseManager().savePlayerData(data);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                plugin.getScoreboardManager().updateBoard(player);
            }
            return true;
        }
        return false;
    }

    public boolean withdrawCoins(Player player, int amount) {
        return withdrawCoins(player.getUniqueId(), amount);
    }

    public boolean withdrawCoins(UUID uuid, int amount) {
        if (amount <= 0) return false;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null && data.getBankCoins() >= amount) {
            data.removeBankCoins(amount);
            data.addCoins(amount);
            plugin.getDatabaseManager().savePlayerData(data);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                plugin.getScoreboardManager().updateBoard(player);
            }
            return true;
        }
        return false;
    }

    public boolean hasBankCoins(Player player, int amount) {
        return hasBankCoins(player.getUniqueId(), amount);
    }

    public boolean hasBankCoins(UUID uuid, int amount) {
        return getBankCoins(uuid) >= amount;
    }
}