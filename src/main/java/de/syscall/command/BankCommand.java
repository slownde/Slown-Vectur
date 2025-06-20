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

public class BankCommand implements CommandExecutor, TabCompleter {

    private final SlownVectur plugin;
    private final List<String> subCommands = Arrays.asList("balance", "bal", "deposit", "dep", "einzahlen", "withdraw", "with", "abheben", "transfer", "überweisen", "help", "hilfe");

    public BankCommand(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.component("&cDieser Command kann nur von Spielern ausgeführt werden!"));
            return true;
        }

        if (args.length == 0) {
            showBankInfo(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "balance", "bal" -> {
                showBankInfo(player);
                return true;
            }

            case "deposit", "dep", "einzahlen" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.component("&cVerwendung: /bank deposit <menge>"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("alles")) {
                    double playerCoins = plugin.getCoinManager().getCoins(player);
                    if (playerCoins <= 0) {
                        player.sendMessage(ColorUtil.component("&cDu hast keine Coins zum Einzahlen!"));
                        return true;
                    }

                    if (plugin.getCoinManager().depositCoins(player, playerCoins)) {
                        player.sendMessage(ColorUtil.component("&7Du hast &aalle &6" + String.format("%.2f", playerCoins) + " Coins &7in deine Bank eingezahlt!"));
                        showBankInfo(player);
                    }
                    return true;
                }

                try {
                    double amount = Double.parseDouble(args[1]);
                    if (amount <= 0) {
                        player.sendMessage(ColorUtil.component("&cDie Menge muss positiv sein!"));
                        return true;
                    }

                    if (plugin.getCoinManager().depositCoins(player, amount)) {
                        player.sendMessage(ColorUtil.component("&7Du hast &6" + String.format("%.2f", amount) + " Coins &7in deine Bank eingezahlt!"));
                        showBankInfo(player);
                    } else {
                        player.sendMessage(ColorUtil.component("&cDu hast nicht genug Coins! Du hast nur &6" + String.format("%.2f", plugin.getCoinManager().getCoins(player)) + " Coins&c."));
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ColorUtil.component("&cUngültige Zahl! Verwende eine Zahl oder 'all'."));
                }
                return true;
            }

            case "withdraw", "with", "abheben" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.component("&cVerwendung: /bank withdraw <menge>"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("alles")) {
                    double bankCoins = plugin.getCoinManager().getBankCoins(player);
                    if (bankCoins <= 0) {
                        player.sendMessage(ColorUtil.component("&cDu hast keine Coins in der Bank zum Abheben!"));
                        return true;
                    }

                    if (plugin.getCoinManager().withdrawCoins(player, bankCoins)) {
                        player.sendMessage(ColorUtil.component("&7Du hast &aalle &6" + String.format("%.2f", bankCoins) + " Coins &7aus deiner Bank abgehoben!"));
                        showBankInfo(player);
                    }
                    return true;
                }

                try {
                    double amount = Double.parseDouble(args[1]);
                    if (amount <= 0) {
                        player.sendMessage(ColorUtil.component("&cDie Menge muss positiv sein!"));
                        return true;
                    }

                    if (plugin.getCoinManager().withdrawCoins(player, amount)) {
                        player.sendMessage(ColorUtil.component("&7Du hast &6" + String.format("%.2f", amount) + " Coins &7aus deiner Bank abgehoben!"));
                        showBankInfo(player);
                    } else {
                        player.sendMessage(ColorUtil.component("&cDu hast nicht genug Coins in der Bank! Du hast nur &6" + String.format("%.2f", plugin.getCoinManager().getBankCoins(player)) + " Coins &cin der Bank."));
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ColorUtil.component("&cUngültige Zahl! Verwende eine Zahl oder 'all'."));
                }
                return true;
            }

            case "transfer", "überweisen" -> {
                if (args.length < 3) {
                    player.sendMessage(ColorUtil.component("&cVerwendung: /bank transfer <spieler> <menge>"));
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ColorUtil.component("&cSpieler nicht gefunden!"));
                    return true;
                }

                if (target.equals(player)) {
                    player.sendMessage(ColorUtil.component("&cDu kannst dir nicht selbst Coins überweisen!"));
                    return true;
                }

                try {
                    double amount = Double.parseDouble(args[2]);
                    if (amount <= 0) {
                        player.sendMessage(ColorUtil.component("&cDie Menge muss positiv sein!"));
                        return true;
                    }

                    if (plugin.getCoinManager().withdrawCoins(player, amount)) {
                        plugin.getCoinManager().depositCoins(target, amount);

                        player.sendMessage(ColorUtil.component("&7Du hast &6" + String.format("%.2f", amount) + " Coins &7an &a" + target.getName() + " &7überwiesen!"));
                        target.sendMessage(ColorUtil.component("&7Du hast &6" + String.format("%.2f", amount) + " Coins &7von &a" + player.getName() + " &7erhalten! (auf deine Bank)"));

                        showBankInfo(player);
                    } else {
                        player.sendMessage(ColorUtil.component("&cDu hast nicht genug Coins in der Bank! Du hast nur &6" + String.format("%.2f", plugin.getCoinManager().getBankCoins(player)) + " Coins &cin der Bank."));
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                }
                return true;
            }

            case "help", "hilfe" -> {
                showBankHelp(player);
                return true;
            }

            default -> {
                player.sendMessage(ColorUtil.component("&cUnbekannter Subcommand! Verwende &6/bank help &cfür Hilfe."));
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player)) {
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

            if (subCommand.equals("transfer") || subCommand.equals("überweisen")) {
                String input = args[1].toLowerCase();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!player.equals(sender) && player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
            } else if (subCommand.equals("deposit") || subCommand.equals("dep") || subCommand.equals("einzahlen") ||
                    subCommand.equals("withdraw") || subCommand.equals("with") || subCommand.equals("abheben")) {
                completions.addAll(Arrays.asList("all", "alles", "1", "10", "100", "1000"));
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("transfer") || subCommand.equals("überweisen")) {
                completions.addAll(Arrays.asList("1", "10", "100", "1000"));
            }
        }

        return completions;
    }

    private void showBankInfo(Player player) {
        double coins = plugin.getCoinManager().getCoins(player);
        double bankCoins = plugin.getCoinManager().getBankCoins(player);
        double totalCoins = coins + bankCoins;

        player.sendMessage(ColorUtil.component("&6&l◆ Bank Information ◆"));
        player.sendMessage(ColorUtil.component("&7&m──────────────────────"));
        player.sendMessage(ColorUtil.component("&7💰 Geldbörse: &6" + formatCoins(coins)));
        player.sendMessage(ColorUtil.component("&7🏦 Bank: &6" + formatCoins(bankCoins)));
        player.sendMessage(ColorUtil.component("&7💎 Gesamt: &6" + formatCoins(totalCoins)));
        player.sendMessage(ColorUtil.component("&7&m──────────────────────"));
        player.sendMessage(ColorUtil.component("&7Verwende &6/bank help &7für Commands"));
    }

    private void showBankHelp(Player player) {
        player.sendMessage(ColorUtil.component("&6&l◆ Bank Commands ◆"));
        player.sendMessage(ColorUtil.component("&7&m───────────────────────"));
        player.sendMessage(ColorUtil.component("&6/bank balance &7- Kontostand anzeigen"));
        player.sendMessage(ColorUtil.component("&6/bank deposit <menge> &7- Coins einzahlen"));
        player.sendMessage(ColorUtil.component("&6/bank withdraw <menge> &7- Coins abheben"));
        player.sendMessage(ColorUtil.component("&6/bank transfer <spieler> <menge> &7- Coins überweisen"));
        player.sendMessage(ColorUtil.component("&7&m───────────────────────"));
        player.sendMessage(ColorUtil.component("&7💡 Tipp: Verwende &6'all' &7statt einer Zahl für alle Coins!"));
        player.sendMessage(ColorUtil.component("&7💡 Transfers gehen auf die Bank des Empfängers!"));
    }

    private String formatCoins(double coins) {
        return String.format("%.2f", coins) + " Coins";
    }
}