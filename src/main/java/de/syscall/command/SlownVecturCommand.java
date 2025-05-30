package de.syscall.command;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlownVecturCommand implements CommandExecutor, TabCompleter {

    private final SlownVectur plugin;
    private final List<String> subCommands = Arrays.asList("reload", "performance", "perf");

    public SlownVecturCommand(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.component("&6&lSlown-Vectur &7v" + plugin.getDescription().getVersion()));
            sender.sendMessage(ColorUtil.component("&7Verfügbare Commands:"));
            sender.sendMessage(ColorUtil.component("&6/" + label + " reload &7- Plugin neu laden"));
            sender.sendMessage(ColorUtil.component("&6/" + label + " performance &7- Performance anzeigen"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> {
                if (!sender.hasPermission("slownvectur.reload")) {
                    sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
                    return true;
                }

                long startTime = System.currentTimeMillis();
                plugin.reload();
                long endTime = System.currentTimeMillis();

                sender.sendMessage(ColorUtil.component("&aSlown-Vectur erfolgreich neu geladen! &7(" + (endTime - startTime) + "ms)"));
                return true;
            }

            case "performance", "perf" -> {
                if (!sender.hasPermission("slownvectur.performance")) {
                    sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
                    return true;
                }

                showPerformance(sender);
                return true;
            }

            default -> {
                sender.sendMessage(ColorUtil.component("&cUnbekannter Subcommand! Verwende: reload, performance"));
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    if ((subCommand.equals("reload") && sender.hasPermission("slownvectur.reload")) ||
                            ((subCommand.equals("performance") || subCommand.equals("perf")) && sender.hasPermission("slownvectur.performance"))) {
                        completions.add(subCommand);
                    }
                }
            }
        }

        return completions;
    }

    private void showPerformance(CommandSender sender) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        double tps = plugin.getServer().getTPS()[0];

        sender.sendMessage(ColorUtil.component("&6&lSlown-Vectur Performance"));
        sender.sendMessage(ColorUtil.component("&7&m─────────────────────────"));
        sender.sendMessage(ColorUtil.component("&7Speicher: &a" + formatBytes(usedMemory) + "&7/&a" + formatBytes(maxMemory) +
                " &7(" + String.format("%.1f", memoryUsagePercent) + "%)"));
        sender.sendMessage(ColorUtil.component("&7TPS: " + getTpsColor(tps) + String.format("%.2f", tps)));
        sender.sendMessage(ColorUtil.component("&7Online Spieler: &a" + onlinePlayers));
        sender.sendMessage(ColorUtil.component("&7Plugin Version: &a" + plugin.getDescription().getVersion()));
        sender.sendMessage(ColorUtil.component("&7&m─────────────────────────"));
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private String getTpsColor(double tps) {
        if (tps >= 19.0) return "&a";
        if (tps >= 17.0) return "&e";
        return "&c";
    }
}