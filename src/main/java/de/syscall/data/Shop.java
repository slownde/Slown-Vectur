package de.syscall.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Shop {

    private final String name;
    private final UUID owner;
    private final Location chestLocation;
    private final ItemStack item;
    private final int amount;
    private final double price;
    private final double taxRate;
    private boolean active;

    public Shop(String name, UUID owner, Location chestLocation, ItemStack item, int amount, double price, double taxRate) {
        this.name = name;
        this.owner = owner;
        this.chestLocation = chestLocation.clone();
        this.item = item.clone();
        this.amount = amount;
        this.price = price;
        this.taxRate = taxRate;
        this.active = true;
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getChestLocation() {
        return chestLocation.clone();
    }

    public Location getHologramLocation() {
        return chestLocation.clone().add(0.5, 1.3, 0.5);
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public Material getMaterial() {
        return item.getType();
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public double getTotalPrice() {
        return price * (1 + taxRate);
    }

    public double getTaxAmount() {
        return price * taxRate;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUniqueId() {
        return chestLocation.getWorld().getName() + "_" +
                chestLocation.getBlockX() + "_" +
                chestLocation.getBlockY() + "_" +
                chestLocation.getBlockZ();
    }
}