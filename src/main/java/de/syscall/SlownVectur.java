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
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
    }

    private void registerCommands() {
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("gm").setExecutor(new GamemodeCommand(this));
        getCommand("gms").setExecutor(new GamemodeCommand(this));
        getCommand("gmc").setExecutor(new GamemodeCommand(this));
        getCommand("gma").setExecutor(new GamemodeCommand(this));
        getCommand("gmsp").setExecutor(new GamemodeCommand(this));
        getCommand("day").setExecutor(new TimeCommand(this));
        getCommand("night").setExecutor(new TimeCommand(this));
        getCommand("slownvectur").setExecutor(new SlownVecturCommand(this));
        getCommand("coins").setExecutor(new CoinsCommand(this));
        getCommand("bank").setExecutor(new BankCommand(this));
        getCommand("portal").setExecutor(new PortalCommand(this));
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