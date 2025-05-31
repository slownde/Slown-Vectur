package de.syscall.api;

import de.syscall.SlownVectur;
import de.syscall.data.DatabaseManager;
import de.syscall.data.PlayerData;
import de.syscall.manager.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VecturAPI {

    private static VecturAPI instance;
    private final SlownVectur plugin;

    public VecturAPI(SlownVectur plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static VecturAPI getInstance() {
        return instance;
    }

    public PlayerData getPlayerData(Player player) {
        return plugin.getPlayerDataManager().getPlayerData(player);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return plugin.getPlayerDataManager().getPlayerData(uuid);
    }

    public String getPrefix(Player player) {
        return plugin.getPrefixManager().getPrefix(player);
    }

    public String getGroup(Player player) {
        return plugin.getPrefixManager().getGroup(player);
    }

    public String getGroupDisplayName(Player player) {
        return plugin.getPrefixManager().getGroupDisplayName(player);
    }

    public int getWeight(Player player) {
        return plugin.getPrefixManager().getWeight(player);
    }

    public double getCoins(Player player) {
        return plugin.getCoinManager().getCoins(player);
    }

    public double getBankCoins(Player player) {
        return plugin.getCoinManager().getBankCoins(player);
    }

    public void addCoins(Player player, double amount) {
        plugin.getCoinManager().addCoins(player, amount);
    }

    public boolean removeCoins(Player player, double amount) {
        return plugin.getCoinManager().removeCoins(player, amount);
    }

    public void setCoins(Player player, double amount) {
        plugin.getCoinManager().setCoins(player, amount);
    }

    public boolean hasCoins(Player player, double amount) {
        return plugin.getCoinManager().hasCoins(player, amount);
    }

    public boolean depositCoins(Player player, double amount) {
        return plugin.getCoinManager().depositCoins(player, amount);
    }

    public boolean withdrawCoins(Player player, double amount) {
        return plugin.getCoinManager().withdrawCoins(player, amount);
    }

    public boolean hasBankCoins(Player player, double amount) {
        return plugin.getCoinManager().hasBankCoins(player, amount);
    }

    public long getPlayTime(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getPlayTime() : 0;
    }

    public long getFirstJoin(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getFirstJoin() : 0;
    }

    public long getLastJoin(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getLastJoin() : 0;
    }

    public PortalManager getPortalManager() {
        return plugin.getPortalManager();
    }

    public PrefixManager getPrefixManager() {
        return plugin.getPrefixManager();
    }

    public CoinManager getCoinManager() {
        return plugin.getCoinManager();
    }

    public PlayerDataManager getPlayerDataManager() {
        return plugin.getPlayerDataManager();
    }

    public ScoreboardManager getScoreboardManager() {
        return plugin.getScoreboardManager();
    }

    public DatabaseManager getDatabaseManager() {
        return plugin.getDatabaseManager();
    }
}