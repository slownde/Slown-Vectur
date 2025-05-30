package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import net.kyori.adventure.text.Component;
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

        String teamName = String.format("%03d_%s", 999 - weight, player.getName().toLowerCase());
        teamNames.put(player.getUniqueId(), teamName);

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        if (!prefix.isEmpty()) {
            team.prefix(Component.text(prefix + " "));
        }

        team.addEntry(player.getName());

        sortAllPlayers();
    }

    private void updateTablistDisplay(Player player) {
        String processedHeader = header.replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        String processedFooter = footer.replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));

        player.sendPlayerListHeaderAndFooter(
                Component.text(processedHeader),
                Component.text(processedFooter)
        );
    }

    private void updateNameTag(Player player) {
        String prefix = plugin.getPrefixManager().getPrefix(player);
        if (!prefix.isEmpty()) {
            player.displayName(Component.text(prefix + " " + player.getName()));
            player.playerListName(Component.text(prefix + " " + player.getName()));
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

    private void sortAllPlayers() {
        List<Player> sortedPlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        sortedPlayers.sort((p1, p2) -> {
            int weight1 = plugin.getPrefixManager().getWeight(p1);
            int weight2 = plugin.getPrefixManager().getWeight(p2);

            if (weight1 != weight2) {
                return Integer.compare(weight2, weight1);
            }

            return p1.getName().compareToIgnoreCase(p2.getName());
        });

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            updatePlayerListName(player, i);
        }
    }

    private void updatePlayerListName(Player player, int position) {
        String prefix = plugin.getPrefixManager().getPrefix(player);
        String displayName = prefix.isEmpty() ? player.getName() : prefix + " " + player.getName();

        player.playerListName(Component.text(String.format("%02d_%s", position, ColorUtil.colorize(displayName))));
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