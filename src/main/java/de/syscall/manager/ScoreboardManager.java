package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.data.PlayerData;
import de.syscall.util.ColorUtil;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class ScoreboardManager {

    private final SlownVectur plugin;
    private final Map<UUID, FastBoard> boards;
    private List<String> lines;
    private String title;

    public ScoreboardManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.boards = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        plugin.reloadConfig();
        this.title = ColorUtil.colorize(plugin.getConfig().getString("scoreboard.title", "&6&lSlown Network"));
        this.lines = plugin.getConfig().getStringList("scoreboard.lines");

        if (lines.isEmpty()) {
            lines = Arrays.asList(
                    "&7&m─────────────────",
                    "&7Online: &a{online}",
                    "&7Rang: {prefix}",
                    "",
                    "&7Coins: &6{coins}",
                    "&7Bank: &6{bank_coins}",
                    "",
                    "&7Spielzeit: &b{playtime}",
                    "&7&m─────────────────"
            );
        }
    }

    public void createBoard(Player player) {
        FastBoard board = new FastBoard(player);
        board.updateTitle(title);
        boards.put(player.getUniqueId(), board);
        updateBoard(player);
    }

    public void updateBoard(Player player) {
        FastBoard board = boards.get(player.getUniqueId());
        if (board == null) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        List<String> processedLines = new ArrayList<>();

        for (String line : lines) {
            String processed = line
                    .replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
                    .replace("{prefix}", plugin.getPrefixManager().getPrefix(player))
                    .replace("{player}", player.getName())
                    .replace("{coins}", String.valueOf(data.getCoins()))
                    .replace("{bank_coins}", String.valueOf(data.getBankCoins()))
                    .replace("{playtime}", formatPlaytime(data.getPlayTime()));

            processed = ColorUtil.colorize(processed);
            processedLines.add(processed);
        }

        board.updateLines(processedLines);
    }

    public void removeBoard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    public void updateAllBoards() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updateBoard(player);
        }
    }

    public void removeAllBoards() {
        for (FastBoard board : boards.values()) {
            board.delete();
        }
        boards.clear();
    }

    private String formatPlaytime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else {
            return minutes + "m";
        }
    }

    public void reload() {
        loadConfig();
        removeAllBoards();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            createBoard(player);
        }
    }
}