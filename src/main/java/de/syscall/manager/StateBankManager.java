package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.data.PlayerData;
import de.syscall.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateBankManager {

    private final SlownVectur plugin;
    private final Map<UUID, Long> lastTaxCollection;
    private int stateBankBalance;
    private double taxRate;
    private int richPlayerThreshold;

    public StateBankManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.lastTaxCollection = new HashMap<>();
        loadConfig();
        startTaxCollection();
    }

    private void loadConfig() {
        plugin.reloadConfig();
        this.stateBankBalance = plugin.getConfig().getInt("statebank.balance", 0);
        this.taxRate = plugin.getConfig().getDouble("statebank.tax-rate", 0.05);
        this.richPlayerThreshold = plugin.getConfig().getInt("statebank.rich-threshold", 10000);
    }

    private void startTaxCollection() {
        new BukkitRunnable() {
            @Override
            public void run() {
                collectTaxes();
            }
        }.runTaskTimer(plugin, 0L, 72000L);
    }

    private void collectTaxes() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);

            if (data == null) continue;

            int totalWealth = data.getCoins() + data.getBankCoins();
            if (totalWealth >= richPlayerThreshold) {
                long lastCollection = lastTaxCollection.getOrDefault(uuid, 0L);
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastCollection >= 3600000) {
                    int taxAmount = (int) (totalWealth * taxRate);

                    if (plugin.getCoinManager().removeCoins(player, taxAmount)) {
                        addToStateBank(taxAmount);
                        lastTaxCollection.put(uuid, currentTime);
                        player.sendMessage(ColorUtil.component("&7Es wurden &c" + taxAmount + " Coins &7als Steuer eingezogen."));
                    } else if (plugin.getCoinManager().getBankCoins(player) >= taxAmount) {
                        data.removeBankCoins(taxAmount);
                        addToStateBank(taxAmount);
                        lastTaxCollection.put(uuid, currentTime);
                        player.sendMessage(ColorUtil.component("&7Es wurden &c" + taxAmount + " Coins &7aus der Bank als Steuer eingezogen."));
                    }
                }
            }
        }
    }

    public void addToStateBank(int amount) {
        stateBankBalance += amount;
        saveStateBankBalance();
    }

    public boolean removeFromStateBank(int amount) {
        if (stateBankBalance >= amount) {
            stateBankBalance -= amount;
            saveStateBankBalance();
            return true;
        }
        return false;
    }

    public int getStateBankBalance() {
        return stateBankBalance;
    }

    public void setStateBankBalance(int amount) {
        stateBankBalance = Math.max(0, amount);
        saveStateBankBalance();
    }

    private void saveStateBankBalance() {
        plugin.getConfig().set("statebank.balance", stateBankBalance);
        plugin.saveConfig();
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double rate) {
        this.taxRate = Math.max(0, Math.min(1, rate));
        plugin.getConfig().set("statebank.tax-rate", taxRate);
        plugin.saveConfig();
    }

    public int getRichPlayerThreshold() {
        return richPlayerThreshold;
    }

    public void setRichPlayerThreshold(int threshold) {
        this.richPlayerThreshold = Math.max(0, threshold);
        plugin.getConfig().set("statebank.rich-threshold", richPlayerThreshold);
        plugin.saveConfig();
    }

    public void reload() {
        loadConfig();
    }
}