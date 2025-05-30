package de.syscall.command;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinsCommand implements CommandExecutor {

    private final SlownVectur plugin;

    public CoinsCommand(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtil.colorize("&cDieser Command kann nur von Spielern ausgeführt werden!"));
                return true;
            }

            Player player = (Player) sender;
            int coins = plugin.getCoinManager().getCoins(player);
            player.sendMessage(ColorUtil.colorize("&7Du hast &6" + coins + " Coins&7!"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "balance", "bal" -> {
                Player target = args.length > 1 ? plugin.getServer().getPlayer(args[1]) :
                        (sender instanceof Player ? (Player) sender : null);

                if (target == null) {
                    sender.sendMessage(ColorUtil.colorize("&cSpieler nicht gefunden!"));
                    return true;
                }

                int coins = plugin.getCoinManager().getCoins(target);
                if (sender.equals(target)) {
                    sender.sendMessage(ColorUtil.colorize("&7Du hast &6" + coins + " Coins&7!"));
                } else {
                    sender.sendMessage(ColorUtil.colorize("&6" + target.getName() + " &7hat &6" + coins + " Coins&7!"));
                }
                return true;
            }

            case "add" -> {
                if (!sender.hasPermission("slownvectur.coins.admin")) {
                    sender.sendMessage(ColorUtil.colorize("&cDu hast keine Berechtigung für diesen Command!"));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.colorize("&cVerwendung: /coins add <spieler> <menge>"));
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.colorize("&cSpieler nicht gefunden!"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage(ColorUtil.colorize("&cDie Menge muss positiv sein!"));
                        return true;
                    }

                    plugin.getCoinManager().addCoins(target, amount);
                    sender.sendMessage(ColorUtil.colorize("&6" + target.getName() + " &7wurden &a" + amount + " Coins &7hinzugefügt!"));
                    target.sendMessage(ColorUtil.colorize("&7Du hast &a" + amount + " Coins &7erhalten!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&cUngültige Zahl!"));
                }
                return true;
            }

            case "remove" -> {
                if (!sender.hasPermission("slownvectur.coins.admin")) {
                    sender.sendMessage(ColorUtil.colorize("&cDu hast keine Berechtigung für diesen Command!"));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.colorize("&cVerwendung: /coins remove <spieler> <menge>"));
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.colorize("&cSpieler nicht gefunden!"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage(ColorUtil.colorize("&cDie Menge muss positiv sein!"));
                        return true;
                    }

                    if (plugin.getCoinManager().removeCoins(target, amount)) {
                        sender.sendMessage(ColorUtil.colorize("&6" + target.getName() + " &7wurden &c" + amount + " Coins &7abgezogen!"));
                        target.sendMessage(ColorUtil.colorize("&7Dir wurden &c" + amount + " Coins &7abgezogen!"));
                    } else {
                        sender.sendMessage(ColorUtil.colorize("&cDer Spieler hat nicht genug Coins!"));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&cUngültige Zahl!"));
                }
                return true;
            }

            case "set" -> {
                if (!sender.hasPermission("slownvectur.coins.admin")) {
                    sender.sendMessage(ColorUtil.colorize("&cDu hast keine Berechtigung für diesen Command!"));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.colorize("&cVerwendung: /coins set <spieler> <menge>"));
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.colorize("&cSpieler nicht gefunden!"));
                    return true;
                }

                try {
                    int amount = Integer.parseInt(args[2]);
                    if (amount < 0) {
                        sender.sendMessage(ColorUtil.colorize("&cDie Menge kann nicht negativ sein!"));
                        return true;
                    }

                    plugin.getCoinManager().setCoins(target, amount);
                    sender.sendMessage(ColorUtil.colorize("&6" + target.getName() + "&7s Coins wurden auf &6" + amount + " &7gesetzt!"));
                    target.sendMessage(ColorUtil.colorize("&7Deine Coins wurden auf &6" + amount + " &7gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&cUngültige Zahl!"));
                }
                return true;
            }

            default -> {
                sender.sendMessage(ColorUtil.colorize("&cVerwendung: /coins <balance|add|remove|set> [spieler] [menge]"));
                return true;
            }
        }
    }
}