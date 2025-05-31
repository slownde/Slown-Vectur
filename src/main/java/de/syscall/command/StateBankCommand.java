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

public class StateBankCommand implements CommandExecutor, TabCompleter {

    private final SlownVectur plugin;
    private final List<String> subCommands = Arrays.asList("balance", "set", "add", "remove", "taxrate", "threshold", "info");

    public StateBankCommand(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("slownvectur.statebank.admin")) {
            sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
            return true;
        }

        if (args.length == 0) {
            showStateBankInfo(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "balance" -> {
                int balance = plugin.getStateBankManager().getStateBankBalance();
                sender.sendMessage(ColorUtil.component("&6Staatsbank Guthaben: &a" + balance + " Coins"));
                return true;
            }

            case "set" -> {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.component("&cVerwendung: /statebank set <betrag>"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[1]);
                    if (amount < 0) {
                        sender.sendMessage(ColorUtil.component("&cDer Betrag kann nicht negativ sein!"));
                        return true;
                    }

                    plugin.getStateBankManager().setStateBankBalance(amount);
                    sender.sendMessage(ColorUtil.component("&aStaatsbank Guthaben auf &6" + amount + " Coins &agesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                }
                return true;
            }

            case "add" -> {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.component("&cVerwendung: /statebank add <betrag>"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[1]);
                    if (amount <= 0) {
                        sender.sendMessage(ColorUtil.component("&cDer Betrag muss positiv sein!"));
                        return true;
                    }

                    plugin.getStateBankManager().addToStateBank(amount);
                    sender.sendMessage(ColorUtil.component("&6" + amount + " Coins &7zur Staatsbank hinzugefügt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                }
                return true;
            }

            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.component("&cVerwendung: /statebank remove <betrag>"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[1]);
                    if (amount <= 0) {
                        sender.sendMessage(ColorUtil.component("&cDer Betrag muss positiv sein!"));
                        return true;
                    }

                    if (plugin.getStateBankManager().removeFromStateBank(amount)) {
                        sender.sendMessage(ColorUtil.component("&6" + amount + " Coins &7aus der Staatsbank entnommen!"));
                    } else {
                        sender.sendMessage(ColorUtil.component("&cNicht genug Guthaben in der Staatsbank!"));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                }
                return true;
            }

            case "taxrate" -> {
                if (args.length < 2) {
                    double currentRate = plugin.getStateBankManager().getTaxRate() * 100;
                    sender.sendMessage(ColorUtil.component("&7Aktueller Steuersatz: &6" + String.format("%.1f", currentRate) + "%"));
                    sender.sendMessage(ColorUtil.component("&7Verwendung: /statebank taxrate <prozent>"));
                    return true;
                }

                try {
                    double rate = Double.parseDouble(args[1]) / 100.0;
                    if (rate < 0 || rate > 100) {
                        sender.sendMessage(ColorUtil.component("&cSteuersatz muss zwischen 0 und 100% liegen!"));
                        return true;
                    }

                    plugin.getStateBankManager().setTaxRate(rate);
                    sender.sendMessage(ColorUtil.component("&aSteuersatz auf &6" + String.format("%.1f", rate * 100) + "% &agesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                }
                return true;
            }

            case "threshold" -> {
                if (args.length < 2) {
                    int currentThreshold = plugin.getStateBankManager().getRichPlayerThreshold();
                    sender.sendMessage(ColorUtil.component("&7Aktueller Reichtums-Schwellwert: &6" + currentThreshold + " Coins"));
                    sender.sendMessage(ColorUtil.component("&7Verwendung: /statebank threshold <betrag>"));
                    return true;
                }

                try {
                    int threshold = Integer.parseInt(args[1]);
                    if (threshold < 0) {
                        sender.sendMessage(ColorUtil.component("&cSchwellwert kann nicht negativ sein!"));
                        return true;
                    }

                    plugin.getStateBankManager().setRichPlayerThreshold(threshold);
                    sender.sendMessage(ColorUtil.component("&aReichtums-Schwellwert auf &6" + threshold + " Coins &agesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                }
                return true;
            }

            case "info" -> {
                showStateBankInfo(sender);
                return true;
            }

            default -> {
                sender.sendMessage(ColorUtil.component("&cUnbekannter Subcommand! Verwende: balance, set, add, remove, taxrate, threshold, info"));
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("slownvectur.statebank.admin")) {
            return completions;
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("set") || subCommand.equals("add") || subCommand.equals("remove") ||
                    subCommand.equals("threshold")) {
                completions.addAll(Arrays.asList("100", "1000", "10000"));
            } else if (subCommand.equals("taxrate")) {
                completions.addAll(Arrays.asList("1", "3", "5", "10"));
            }
        }

        return completions;
    }

    private void showStateBankInfo(CommandSender sender) {
        int balance = plugin.getStateBankManager().getStateBankBalance();
        double taxRate = plugin.getStateBankManager().getTaxRate() * 100;
        int threshold = plugin.getStateBankManager().getRichPlayerThreshold();

        sender.sendMessage(ColorUtil.component("&6&l◆ Staatsbank Information ◆"));
        sender.sendMessage(ColorUtil.component("&7&m──────────────────────────"));
        sender.sendMessage(ColorUtil.component("&7💰 Guthaben: &6" + balance + " Coins"));
        sender.sendMessage(ColorUtil.component("&7📊 Steuersatz: &6" + String.format("%.1f", taxRate) + "%"));
        sender.sendMessage(ColorUtil.component("&7💎 Reichtums-Schwelle: &6" + threshold + " Coins"));
        sender.sendMessage(ColorUtil.component("&7&m──────────────────────────"));
        sender.sendMessage(ColorUtil.component("&7Spieler über dem Schwellwert werden besteuert"));
    }
}