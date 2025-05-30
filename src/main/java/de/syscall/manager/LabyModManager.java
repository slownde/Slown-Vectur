package de.syscall.manager;

import de.syscall.SlownVectur;
import net.labymod.serverapi.core.model.display.TabListFlag;
import net.labymod.serverapi.core.model.feature.DiscordRPC;
import net.labymod.serverapi.core.packet.clientbound.game.feature.DiscordRPCPacket;
import net.labymod.serverapi.server.bukkit.LabyModPlayer;
import net.labymod.serverapi.server.bukkit.LabyModProtocolService;
import net.labymod.serverapi.server.bukkit.LabyModServerAPIPlugin;
import net.labymod.serverapi.server.bukkit.event.LabyModPlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LabyModManager implements Listener {

    private final SlownVectur plugin;
    private final Map<UUID, net.labymod.serverapi.server.bukkit.LabyModPlayer> labyModPlayers;
    private final Map<UUID, Long> joinTimes;
    private String serverName;
    private String gameMode;

    public LabyModManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.labyModPlayers = new HashMap<>();
        this.joinTimes = new HashMap<>();
        loadConfig();
        initializeLabyMod();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void initializeLabyMod() {
        try {
            LabyModProtocolService.initialize(plugin);
            plugin.getLogger().info("LabyMod 4 Server API erfolgreich initialisiert!");
        } catch (Exception e) {
            plugin.getLogger().warning("LabyMod 4 Server API konnte nicht initialisiert werden: " + e.getMessage());
        }
    }

    private void loadConfig() {
        plugin.reloadConfig();
        this.serverName = plugin.getConfig().getString("labymod.server-name", "Slown Network");
        this.gameMode = plugin.getConfig().getString("labymod.game-mode", "Survival");
    }

    @EventHandler
    public void onLabyModPlayerJoin(LabyModPlayerJoinEvent event) {
        LabyModPlayer labyModPlayer = event.labyModPlayer();
        UUID uuid = labyModPlayer.getUniqueId();

        labyModPlayers.put(uuid, labyModPlayer);

        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            plugin.getLogger().info(player.getName() + " verwendet LabyMod 4!");

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                sendLabyModFeatures(labyModPlayer, player);
            }, 20L);
        }
    }

    public void handlePlayerQuit(Player player) {
        labyModPlayers.remove(player.getUniqueId());
    }

    private void sendLabyModFeatures(LabyModPlayer labyModPlayer, Player player) {
        try {
            sendServerBanner(labyModPlayer);
            sendPlayerFlag(labyModPlayer);
            sendRichPresence(labyModPlayer, player);
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Senden der LabyMod Features: " + e.getMessage());
        }
    }

    private void sendRichPresence(LabyModPlayer labyModPlayer, Player player) {
        try {
            String groupDisplayName = plugin.getPrefixManager().getGroupDisplayName(player);
            String playerName = player.getName();

            String gameMode = groupDisplayName + " - " + playerName + " | " + this.gameMode + " auf " + serverName;

            Long joinTime = joinTimes.get(player.getUniqueId());

            DiscordRPC discordRPC;
            if (joinTime != null) {
                discordRPC = DiscordRPC.createWithStart(gameMode, joinTime);
            } else {
                discordRPC = DiscordRPC.create(gameMode);
            }

            labyModPlayer.sendPacket(new DiscordRPCPacket(discordRPC));

            plugin.getLogger().info("Discord RPC für " + player.getName() + " gesendet: " + gameMode);
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Senden der Discord RPC für " + player.getName() + ": " + e.getMessage());
        }
    }

    private void sendServerBanner(LabyModPlayer labyModPlayer) {
        labyModPlayer.sendTabListBanner("https://docs.labymod.net/img/Header.png");
    }

    private void sendPlayerFlag(LabyModPlayer labyModPlayer) {
        labyModPlayer.setTabListFlag(TabListFlag.TabListFlagCountryCode.DE);
    }

    public void updateRichPresence(Player player) {
        net.labymod.serverapi.server.bukkit.LabyModPlayer labyModPlayer = labyModPlayers.get(player.getUniqueId());
        if (labyModPlayer != null) {
            sendRichPresence(labyModPlayer, player);
        }
    }

    public void updateAllRichPresence() {
        for (UUID uuid : labyModPlayers.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                updateRichPresence(player);
            }
        }
    }

    public boolean isLabyModPlayer(Player player) {
        return labyModPlayers.containsKey(player.getUniqueId());
    }

    public net.labymod.serverapi.server.bukkit.LabyModPlayer getLabyModPlayer(Player player) {
        return labyModPlayers.get(player.getUniqueId());
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
        updateAllRichPresence();
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
        updateAllRichPresence();
    }

    public String getServerName() {
        return serverName;
    }

    public String getGameMode() {
        return gameMode;
    }
}