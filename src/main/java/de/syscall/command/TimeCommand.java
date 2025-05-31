package de.syscall.command;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TimeCommand implements CommandExecutor, TabCompleter {

    private final SlownVectur plugin;

    public TimeCommand(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("slownvectur.time")) {
            sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
            return true;
        }

        World world;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtil.component("&cBitte gib eine Welt an!"));
                return true;
            }
            world = ((Player) sender).getWorld();
        } else {
            world = plugin.getServer().getWorld(args[0]);
            if (world == null) {
                sender.sendMessage(ColorUtil.component("&cWelt nicht gefunden!"));
                return true;
            }
        }

        long targetTime = label.equalsIgnoreCase("day") ? 1000L : 13000L;
        String timeName = label.equalsIgnoreCase("day") ? "Tag" : "Nacht";

        animateTimeChange(world, targetTime, timeName, sender);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("slownvectur.time")) {
            return completions;
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (World world : plugin.getServer().getWorlds()) {
                if (world.getName().toLowerCase().startsWith(input)) {
                    completions.add(world.getName());
                }
            }
        }

        return completions;
    }

    private void animateTimeChange(World world, long targetTime, String timeName, CommandSender sender) {
        long currentTime = world.getTime();
        long difference = targetTime - currentTime;

        if (difference < 0) {
            difference += 24000;
        }

        if (difference == 0) {
            sender.sendMessage(ColorUtil.component("&7Es ist bereits &6" + timeName + "&7!"));
            return;
        }

        sender.sendMessage(ColorUtil.component("&7Ändere Zeit zu &6" + timeName + "&7..."));

        long finalDifference = difference;
        new BukkitRunnable() {
            private long step = 0;
            private final long totalSteps = Math.min(finalDifference / 100, 50);
            private final long timePerStep = finalDifference / Math.max(totalSteps, 1);

            @Override
            public void run() {
                if (step >= totalSteps) {
                    world.setTime(targetTime);
                    sender.sendMessage(ColorUtil.component("&7Zeit erfolgreich auf &6" + timeName + " &7gesetzt!"));
                    cancel();
                    return;
                }

                long newTime = (currentTime + (step * timePerStep)) % 24000;
                world.setTime(newTime);
                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}