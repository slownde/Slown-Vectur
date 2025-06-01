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

    public double getCoins(Player player) {
        return getCoins(player.getUniqueId());
    }

    public double getCoins(UUID uuid) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        return data != null ? data.getCoins() : 0.0;
    }

    public void setCoins(Player player, double amount) {
        setCoins(player.getUniqueId(), amount);
    }

    public void setCoins(UUID uuid, double amount) {
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

    public void addCoins(Player player, double amount) {
        addCoins(player.getUniqueId(), amount);
    }

    public void addCoins(UUID uuid, double amount) {
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

    public boolean removeCoins(Player player, double amount) {
        return removeCoins(player.getUniqueId(), amount);
    }

    public boolean removeCoins(UUID uuid, double amount) {
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

    public boolean hasCoins(Player player, double amount) {
        return hasCoins(player.getUniqueId(), amount);
    }

    public boolean hasCoins(UUID uuid, double amount) {
        return getCoins(uuid) >= amount;
    }

    public double getBankCoins(Player player) {
        return getBankCoins(player.getUniqueId());
    }

    public double getBankCoins(UUID uuid) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        return data != null ? data.getBankCoins() : 0.0;
    }

    public void setBankCoins(Player player, double amount) {
        setBankCoins(player.getUniqueId(), amount);
    }

    public void setBankCoins(UUID uuid, double amount) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null) {
            data.setBankCoins(Math.max(0, amount));
            plugin.getDatabaseManager().savePlayerData(data);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                plugin.getScoreboardManager().updateBoard(player);
            }
        }
    }

    public void addBankCoins(Player player, double amount) {
        addBankCoins(player.getUniqueId(), amount);
    }

    public void addBankCoins(UUID uuid, double amount) {
        if (amount <= 0) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null) {
            data.addBankCoins(amount);
            plugin.getDatabaseManager().savePlayerData(data);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                plugin.getScoreboardManager().updateBoard(player);
            }
        }
    }

    public boolean removeBankCoins(Player player, double amount) {
        return removeBankCoins(player.getUniqueId(), amount);
    }

    public boolean removeBankCoins(UUID uuid, double amount) {
        if (amount <= 0) return false;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null && data.getBankCoins() >= amount) {
            data.removeBankCoins(amount);
            plugin.getDatabaseManager().savePlayerData(data);

            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                plugin.getScoreboardManager().updateBoard(player);
            }
            return true;
        }
        return false;
    }

    public boolean depositCoins(Player player, double amount) {
        return depositCoins(player.getUniqueId(), amount);
    }

    public boolean depositCoins(UUID uuid, double amount) {
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

    public boolean withdrawCoins(Player player, double amount) {
        return withdrawCoins(player.getUniqueId(), amount);
    }

    public boolean withdrawCoins(UUID uuid, double amount) {
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

    public boolean hasBankCoins(Player player, double amount) {
        return hasBankCoins(player.getUniqueId(), amount);
    }

    public boolean hasBankCoins(UUID uuid, double amount) {
        return getBankCoins(uuid) >= amount;
    }
}