package de.syscall.data;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String name;
    private long firstJoin;
    private long lastJoin;
    private long playTime;
    private int coins;
    private int bankCoins;
    private boolean dataLoaded;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.firstJoin = System.currentTimeMillis();
        this.lastJoin = System.currentTimeMillis();
        this.playTime = 0;
        this.coins = 0;
        this.bankCoins = 0;
        this.dataLoaded = false;
    }

    public PlayerData(UUID uuid, String name, long firstJoin, long lastJoin, long playTime, int coins, int bankCoins) {
        this.uuid = uuid;
        this.name = name;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.playTime = playTime;
        this.coins = coins;
        this.bankCoins = bankCoins;
        this.dataLoaded = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(long firstJoin) {
        this.firstJoin = firstJoin;
    }

    public long getLastJoin() {
        return lastJoin;
    }

    public void setLastJoin(long lastJoin) {
        this.lastJoin = lastJoin;
    }

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    public void addPlayTime(long time) {
        this.playTime += time;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void addCoins(int coins) {
        this.coins += coins;
    }

    public void removeCoins(int coins) {
        this.coins = Math.max(0, this.coins - coins);
    }

    public int getBankCoins() {
        return bankCoins;
    }

    public void setBankCoins(int bankCoins) {
        this.bankCoins = bankCoins;
    }

    public void addBankCoins(int coins) {
        this.bankCoins += coins;
    }

    public void removeBankCoins(int coins) {
        this.bankCoins = Math.max(0, this.bankCoins - coins);
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public void setDataLoaded(boolean dataLoaded) {
        this.dataLoaded = dataLoaded;
    }
}