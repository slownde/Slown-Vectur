﻿package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.data.Shop;
import de.syscall.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopManager {

    private final SlownVectur plugin;
    private final Map<String, Shop> shops;
    private final Map<String, Location> chestToShop;
    private File shopsFile;
    private FileConfiguration config;
    private double defaultTaxRate;

    public ShopManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.shops = new ConcurrentHashMap<>();
        this.chestToShop = new ConcurrentHashMap<>();
        initializeConfig();
        loadShops();
        loadConfig();
    }

    private void initializeConfig() {
        shopsFile = new File(plugin.getDataFolder(), "shops.yml");
        if (!shopsFile.exists()) {
            try {
                shopsFile.getParentFile().mkdirs();
                shopsFile.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().severe("Could not create shops.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(shopsFile);
    }

    private void loadConfig() {
        plugin.reloadConfig();
        this.defaultTaxRate = plugin.getConfig().getDouble("shop.default-tax-rate", 0.03);
    }

    private void loadShops() {
        shops.clear();
        chestToShop.clear();

        ConfigurationSection section = config.getConfigurationSection("shops");
        if (section == null) return;

        for (String shopId : section.getKeys(false)) {
            try {
                Shop shop = loadShop(shopId, section.getConfigurationSection(shopId));
                if (shop != null) {
                    shops.put(shopId, shop);
                    chestToShop.put(getLocationKey(shop.getChestLocation()), shop.getChestLocation());
                    plugin.getHologramManager().createShopHologram(shop);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load shop " + shopId + ": " + e.getMessage());
            }
        }
    }

    private Shop loadShop(String shopId, ConfigurationSection section) {
        if (section == null) return null;

        String name = section.getString("name");
        UUID owner = UUID.fromString(section.getString("owner"));
        Location chestLocation = loadLocation(section.getConfigurationSection("chest"));

        if (chestLocation == null) return null;

        Material material = Material.valueOf(section.getString("item.material"));
        int itemAmount = section.getInt("item.amount", 1);
        ItemStack item = new ItemStack(material, itemAmount);

        int amount = section.getInt("amount");
        double price = section.getDouble("price");
        double taxRate = section.getDouble("tax-rate", defaultTaxRate);

        Shop shop = new Shop(name, owner, chestLocation, item, amount, price, taxRate);
        shop.setActive(section.getBoolean("active", true));

        return shop;
    }

    private Location loadLocation(ConfigurationSection section) {
        if (section == null) return null;

        return new Location(
                plugin.getServer().getWorld(section.getString("world")),
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z")
        );
    }

    public boolean createShop(Player player, String name, int amount, double price) {
        Block targetBlock = player.getTargetBlockExact(5);

        if (targetBlock == null || !isChest(targetBlock)) {
            player.sendMessage(ColorUtil.component("&cDu musst auf eine Kiste schauen!"));
            return false;
        }

        Location chestLocation = targetBlock.getLocation();
        String locationKey = getLocationKey(chestLocation);

        if (chestToShop.containsKey(locationKey)) {
            player.sendMessage(ColorUtil.component("&cHier existiert bereits ein Shop!"));
            return false;
        }

        Inventory chestInventory = getChestInventory(targetBlock);
        if (chestInventory == null) {
            player.sendMessage(ColorUtil.component("&cFehler beim Zugriff auf die Kiste!"));
            return false;
        }

        ItemStack firstItem = getFirstValidItem(chestInventory);
        if (firstItem == null) {
            player.sendMessage(ColorUtil.component("&cDie Kiste muss mindestens ein Item enthalten!"));
            return false;
        }

        if (!hasEnoughItems(chestInventory, firstItem, amount)) {
            player.sendMessage(ColorUtil.component("&cNicht genug Items in der Kiste! Benötigt: " + amount));
            return false;
        }

        Shop shop = new Shop(name, player.getUniqueId(), chestLocation, firstItem, amount, price, defaultTaxRate);
        String shopId = shop.getUniqueId();

        shops.put(shopId, shop);
        chestToShop.put(locationKey, chestLocation);

        saveShop(shop);
        plugin.getHologramManager().createShopHologram(shop);

        player.sendMessage(ColorUtil.component("&aShop erstellt! Preis mit Steuern: &6" +
                String.format("%.2f", shop.getTotalPrice()) + " Coins"));

        return true;
    }

    public boolean deleteShop(String shopId) {
        Shop shop = shops.remove(shopId);
        if (shop == null) return false;

        chestToShop.remove(getLocationKey(shop.getChestLocation()));
        plugin.getHologramManager().removeShopHologram(shopId);

        config.set("shops." + shopId, null);
        saveConfig();

        return true;
    }

    public boolean buyFromShop(Player buyer, Shop shop) {
        if (!shop.isActive()) {
            buyer.sendMessage(ColorUtil.component("&cDieser Shop ist nicht aktiv!"));
            return false;
        }

        double totalPrice = shop.getTotalPrice();
        if (!plugin.getCoinManager().hasCoins(buyer, (int) totalPrice)) {
            buyer.sendMessage(ColorUtil.component("&cDu hast nicht genug Coins! Benötigt: " +
                    String.format("%.2f", totalPrice)));
            return false;
        }

        Block chestBlock = shop.getChestLocation().getBlock();
        if (!isChest(chestBlock)) {
            buyer.sendMessage(ColorUtil.component("&cShop-Kiste nicht gefunden!"));
            return false;
        }

        Inventory chestInventory = getChestInventory(chestBlock);
        if (chestInventory == null || !hasEnoughItems(chestInventory, shop.getItem(), shop.getAmount())) {
            buyer.sendMessage(ColorUtil.component("&cNicht genug Items im Shop verfügbar!"));
            return false;
        }

        if (buyer.getInventory().firstEmpty() == -1) {
            buyer.sendMessage(ColorUtil.component("&cDein Inventar ist voll!"));
            return false;
        }

        removeItemsFromInventory(chestInventory, shop.getItem(), shop.getAmount());

        ItemStack purchasedItems = shop.getItem().clone();
        purchasedItems.setAmount(shop.getAmount());
        buyer.getInventory().addItem(purchasedItems);

        plugin.getCoinManager().removeCoins(buyer, (int) totalPrice);
        plugin.getCoinManager().addCoins(shop.getOwner(), (int) shop.getPrice());
        plugin.getStateBankManager().addToStateBank((int) shop.getTaxAmount());

        buyer.sendMessage(ColorUtil.component("&aKauf erfolgreich! &7Bezahlt: &6" +
                String.format("%.2f", totalPrice) + " Coins"));

        Player owner = plugin.getServer().getPlayer(shop.getOwner());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(ColorUtil.component("&a" + buyer.getName() + " &7hat in deinem Shop &6" +
                    shop.getName() + " &7eingekauft! &aErhalten: &6" + String.format("%.2f", shop.getPrice()) + " Coins"));
        }

        return true;
    }

    public Shop getShopAtLocation(Location location) {
        String locationKey = getLocationKey(location);
        Location chestLocation = chestToShop.get(locationKey);
        if (chestLocation == null) return null;

        return shops.values().stream()
                .filter(shop -> shop.getChestLocation().equals(chestLocation))
                .findFirst()
                .orElse(null);
    }

    public Shop getShop(String shopId) {
        return shops.get(shopId);
    }

    public Collection<Shop> getAllShops() {
        return shops.values();
    }

    public Collection<Shop> getShopsByOwner(UUID owner) {
        return shops.values().stream()
                .filter(shop -> shop.getOwner().equals(owner))
                .toList();
    }

    private boolean isChest(Block block) {
        return block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST;
    }

    private Inventory getChestInventory(Block chestBlock) {
        if (chestBlock.getState() instanceof Chest chest) {
            InventoryHolder holder = chest.getInventory().getHolder();
            if (holder instanceof DoubleChest doubleChest) {
                return doubleChest.getInventory();
            }
            return chest.getInventory();
        }
        return null;
    }

    private ItemStack getFirstValidItem(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return item;
            }
        }
        return null;
    }

    private boolean hasEnoughItems(Inventory inventory, ItemStack targetItem, int requiredAmount) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.isSimilar(targetItem)) {
                count += item.getAmount();
                if (count >= requiredAmount) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeItemsFromInventory(Inventory inventory, ItemStack targetItem, int amountToRemove) {
        int remaining = amountToRemove;

        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.isSimilar(targetItem)) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    inventory.setItem(i, null);
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
    }

    private String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" +
                location.getBlockX() + "_" +
                location.getBlockY() + "_" +
                location.getBlockZ();
    }

    private void saveShop(Shop shop) {
        String path = "shops." + shop.getUniqueId();

        config.set(path + ".name", shop.getName());
        config.set(path + ".owner", shop.getOwner().toString());
        saveLocation(path + ".chest", shop.getChestLocation());
        config.set(path + ".item.material", shop.getMaterial().name());
        config.set(path + ".item.amount", shop.getItem().getAmount());
        config.set(path + ".amount", shop.getAmount());
        config.set(path + ".price", shop.getPrice());
        config.set(path + ".tax-rate", shop.getTaxRate());
        config.set(path + ".active", shop.isActive());

        saveConfig();
    }

    private void saveLocation(String path, Location location) {
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
    }

    private void saveConfig() {
        try {
            config.save(shopsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save shops.yml: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(shopsFile);
        loadConfig();
        loadShops();
    }

    public double getDefaultTaxRate() {
        return defaultTaxRate;
    }

    public void setDefaultTaxRate(double rate) {
        this.defaultTaxRate = Math.max(0, Math.min(1, rate));
        plugin.getConfig().set("shop.default-tax-rate", defaultTaxRate);
        plugin.saveConfig();
    }
}