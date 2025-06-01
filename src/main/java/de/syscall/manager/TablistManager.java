package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class TablistManager {

    private final SlownVectur plugin;
    private String header;
    private String footer;
    private final Map<UUID, String> teamNames;

    public TablistManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.teamNames = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        plugin.reloadConfig();
        this.header = ColorUtil.colorize(plugin.getConfig().getString("tablist.header", "&6&lSlown Network"));
        this.footer = ColorUtil.colorize(plugin.getConfig().getString("tablist.footer", "&7Online: {online}"));
    }

    public void updatePlayer(Player player) {
        setupPlayerTeam(player);
        updateTablistDisplay(player);
        updateNameTag(player);
    }

    private void setupPlayerTeam(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == plugin.getServer().getScoreboardManager().getMainScoreboard()) {
            scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        String prefix = plugin.getPrefixManager().getPrefix(player);
        int weight = plugin.getPrefixManager().getWeight(player);

        String oldTeamName = teamNames.get(player.getUniqueId());
        if (oldTeamName != null) {
            Team oldTeam = scoreboard.getTeam(oldTeamName);
            if (oldTeam != null) {
                oldTeam.unregister();
            }
        }

        String teamName = String.format("%04d_%s", 9999 - weight, player.getName().toLowerCase());
        teamNames.put(player.getUniqueId(), teamName);

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        if (!prefix.isEmpty()) {
            team.prefix(ColorUtil.component(prefix + " "));
        }

        team.addEntry(player.getName());
    }

    private void updateTablistDisplay(Player player) {
        String processedHeader = header.replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        String processedFooter = footer.replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));

        player.sendPlayerListHeaderAndFooter(
                ColorUtil.component(processedHeader),
                ColorUtil.component(processedFooter)
        );
    }

    private void updateNameTag(Player player) {
        String prefix = plugin.getPrefixManager().getPrefix(player);
        if (!prefix.isEmpty()) {
            player.displayName(ColorUtil.component(prefix + " " + player.getName()));
            player.playerListName(ColorUtil.component(prefix + " " + player.getName()));
        } else {
            player.displayName(ColorUtil.component(player.getName()));
            player.playerListName(ColorUtil.component(player.getName()));
        }
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        String teamName = teamNames.remove(uuid);

        if (teamName != null) {
            Scoreboard scoreboard = player.getScoreboard();
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                team.unregister();
            }
        }
    }

    public void updateAllPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    public void reload() {
        loadConfig();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            removePlayer(player);
            updatePlayer(player);
        }
    }
}