package de.syscall.listener;

import de.syscall.SlownVectur;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final SlownVectur plugin;

    public PlayerQuitListener(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.quitMessage(plugin.getChatManager().getLeaveMessage(player));

        plugin.getPlayerDataManager().savePlayerData(player);

        plugin.getScoreboardManager().removeBoard(player);
        plugin.getTablistManager().removePlayer(player);
        plugin.getPrefixManager().removePlayer(player.getUniqueId());
        plugin.getLabyModManager().handlePlayerQuit(player);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerDataManager().unloadPlayerData(player.getUniqueId());
        }, 20L);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getTablistManager().updateAllPlayers();
            plugin.getScoreboardManager().updateAllBoards();
        }, 5L);
    }
}