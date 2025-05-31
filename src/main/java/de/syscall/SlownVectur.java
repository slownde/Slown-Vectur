package de.syscall;

import de.syscall.api.VecturAPI;
import de.syscall.manager.*;
import de.syscall.listener.*;
import de.syscall.command.*;
import de.syscall.data.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SlownVectur extends JavaPlugin {

    private static SlownVectur instance;
    private static VecturAPI api;

    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private PrefixManager prefixManager;
    private ChatManager chatManager;
    private ScoreboardManager scoreboardManager;
    private TablistManager tablistManager;
    private CoinManager coinManager;
    private LabyModManager labyModManager;
    private PortalManager portalManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.prefixManager = new PrefixManager(this);
        this.chatManager = new ChatManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.tablistManager = new TablistManager(this);
        this.coinManager = new CoinManager(this);
        this.labyModManager = new LabyModManager(this);
        this.portalManager = new PortalManager(this);

        api = new VecturAPI(this);

        registerListeners();
        registerCommands();

        getLogger().info("Slown-Vectur erfolgreich gestartet!");
    }

    @Override
    public void onDisable() {
        if (portalManager != null) {
            portalManager.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        if (scoreboardManager != null) {
            scoreboardManager.removeAllBoards();
        }

        getLogger().info("Slown-Vectur gestoppt!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRankChangeListener(this), this);
    }

    private void registerCommands() {
        FlyCommand flyCommand = new FlyCommand(this);
        getCommand("fly").setExecutor(flyCommand);
        getCommand("fly").setTabCompleter(flyCommand);

        GamemodeCommand gamemodeCommand = new GamemodeCommand(this);
        getCommand("gm").setExecutor(gamemodeCommand);
        getCommand("gm").setTabCompleter(gamemodeCommand);
        getCommand("gms").setExecutor(gamemodeCommand);
        getCommand("gms").setTabCompleter(gamemodeCommand);
        getCommand("gmc").setExecutor(gamemodeCommand);
        getCommand("gmc").setTabCompleter(gamemodeCommand);
        getCommand("gma").setExecutor(gamemodeCommand);
        getCommand("gma").setTabCompleter(gamemodeCommand);
        getCommand("gmsp").setExecutor(gamemodeCommand);
        getCommand("gmsp").setTabCompleter(gamemodeCommand);

        TimeCommand timeCommand = new TimeCommand(this);
        getCommand("day").setExecutor(timeCommand);
        getCommand("day").setTabCompleter(timeCommand);
        getCommand("night").setExecutor(timeCommand);
        getCommand("night").setTabCompleter(timeCommand);

        SlownVecturCommand slownVecturCommand = new SlownVecturCommand(this);
        getCommand("slownvectur").setExecutor(slownVecturCommand);
        getCommand("slownvectur").setTabCompleter(slownVecturCommand);

        CoinsCommand coinsCommand = new CoinsCommand(this);
        getCommand("coins").setExecutor(coinsCommand);
        getCommand("coins").setTabCompleter(coinsCommand);

        BankCommand bankCommand = new BankCommand(this);
        getCommand("bank").setExecutor(bankCommand);
        getCommand("bank").setTabCompleter(bankCommand);

        PortalCommand portalCommand = new PortalCommand(this);
        getCommand("portal").setExecutor(portalCommand);
        getCommand("portal").setTabCompleter(portalCommand);
    }

    public void reload() {
        reloadConfig();
        prefixManager.reload();
        chatManager.reload();
        scoreboardManager.reload();
        tablistManager.reload();
        portalManager.reloadConfig();
    }

    public static SlownVectur getInstance() {
        return instance;
    }

    public static VecturAPI getAPI() {
        return api;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public PrefixManager getPrefixManager() {
        return prefixManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public TablistManager getTablistManager() {
        return tablistManager;
    }

    public CoinManager getCoinManager() {
        return coinManager;
    }

    public LabyModManager getLabyModManager() {
        return labyModManager;
    }

    public PortalManager getPortalManager() {
        return portalManager;
    }
}