package de.syscall.command;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoinsCommand implements CommandExecutor, TabCompleter {

    private final SlownVectur plugin;
    private final List<String> subCommands = Arrays.asList("balance", "bal", "add", "remove", "set");

    public CoinsCommand(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtil.component("&cDieser Command kann nur von Spielern ausgeführt werden!"));
                return true;
            }

            int coins = plugin.getCoinManager().getCoins(player);
            player.sendMessage(ColorUtil.component("&7Du hast &6" + coins + " Coins&7!"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "balance", "bal" -> {
                Player target = args.length > 1 ? plugin.getServer().getPlayer(args[1]) :
                        (sender instanceof Player ? (Player) sender : null);

                if (target == null) {
                    sender.sendMessage(ColorUtil.component("&cSpieler nicht gefunden!"));
                    return true;
                }

                int coins = plugin.getCoinManager().getCoins(target);
                if (sender.equals(target)) {
                    sender.sendMessage(ColorUtil.component("&7Du hast &6" + coins + " Coins&7!"));
                } else {
                    sender.sendMessage(ColorUtil.component("&6" + target.getName() + " &7hat &6" + coins + " Coins&7!"));
                }
                return true;
            }

            case "add" -> {
                if (!sender.hasPermission("slownvectur.coins.admin")) {
                    sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.component("&cVerwendung: /coins add <spieler> <menge>"));
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.component("&cSpieler nicht gefunden!"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage(ColorUtil.component("&cDie Menge muss positiv sein!"));
                        return true;
                    }

                    plugin.getCoinManager().addCoins(target, amount);
                    sender.sendMessage(ColorUtil.component("&6" + target.getName() + " &7wurden &a" + amount + " Coins &7hinzugefügt!"));
                    target.sendMessage(ColorUtil.component("&7Du hast &a" + amount + " Coins &7erhalten!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                }
                return true;
            }

            case "remove" -> {
                if (!sender.hasPermission("slownvectur.coins.admin")) {
                    sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.component("&cVerwendung: /coins remove <spieler> <menge>"));
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.component("&cSpieler nicht gefunden!"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage(ColorUtil.component("&cDie Menge muss positiv sein!"));
                        return true;
                    }

                    if (plugin.getCoinManager().removeCoins(target, amount)) {
                        sender.sendMessage(ColorUtil.component("&6" + target.getName() + " &7wurden &c" + amount + " Coins &7abgezogen!"));
                        target.sendMessage(ColorUtil.component("&7Dir wurden &c" + amount + " Coins &7abgezogen!"));
                    } else {
                        sender.sendMessage(ColorUtil.component("&cDer Spieler hat nicht genug Coins!"));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                }
                return true;
            }

            case "set" -> {
                if (!sender.hasPermission("slownvectur.coins.admin")) {
                    sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.component("&cVerwendung: /coins set <spieler> <menge>"));
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.component("&cSpieler nicht gefunden!"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount < 0) {
                        sender.sendMessage(ColorUtil.component("&cDie Menge kann nicht negativ sein!"));
                        return true;
                    }

                    plugin.getCoinManager().setCoins(target, amount);
                    sender.sendMessage(ColorUtil.component("&6" + target.getName() + "&7s Coins wurden auf &6" + amount + " &7gesetzt!"));
                    target.sendMessage(ColorUtil.component("&7Deine Coins wurden auf &6" + amount + " &7gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                }
                return true;
            }

            default -> {
                sender.sendMessage(ColorUtil.component("&cVerwendung: /coins <balance|add|remove|set> [spieler] [menge]"));
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
                    if ((subCommand.equals("balance") || subCommand.equals("bal")) ||
                            (sender.hasPermission("slownvectur.coins.admin") &&
                                    (subCommand.equals("add") || subCommand.equals("remove") || subCommand.equals("set")))) {
                        completions.add(subCommand);
                    }
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("balance") || subCommand.equals("bal") ||
                    (sender.hasPermission("slownvectur.coins.admin") &&
                            (subCommand.equals("add") || subCommand.equals("remove") || subCommand.equals("set")))) {

                String input = args[1].toLowerCase();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (sender.hasPermission("slownvectur.coins.admin") &&
                    (subCommand.equals("add") || subCommand.equals("remove") || subCommand.equals("set"))) {
                completions.addAll(Arrays.asList("1", "10", "100", "1000"));
            }
        }

        return completions;
    }
}