package de.syscall.command;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import org.bukkit.GameMode;
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

public class GamemodeCommand implements CommandExecutor, TabCompleter {

    private final SlownVectur plugin;
    private final List<String> gameModes = Arrays.asList("survival", "creative", "adventure", "spectator", "0", "1", "2", "3", "s", "c", "a", "sp");

    public GamemodeCommand(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("slownvectur.gamemode")) {
            sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
            return true;
        }

        GameMode gameMode = getGameModeFromLabel(label);

        if (gameMode == null && args.length == 0) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /gm <mode> [spieler]"));
            return true;
        }

        if (gameMode == null) {
            gameMode = parseGameMode(args[0]);
            if (gameMode == null) {
                sender.sendMessage(ColorUtil.component("&cUngültiger Gamemode!"));
                return true;
            }
        }

        Player target;
        if (args.length == 0 || (args.length == 1 && !label.equals("gm"))) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtil.component("&cDieser Command kann nur von Spielern ausgeführt werden!"));
                return true;
            }
            target = (Player) sender;
        } else {
            String playerName = args.length > 1 ? args[1] : args[0];
            target = plugin.getServer().getPlayer(playerName);
            if (target == null) {
                sender.sendMessage(ColorUtil.component("&cSpieler nicht gefunden!"));
                return true;
            }
        }

        setGameMode(sender, target, gameMode);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("slownvectur.gamemode")) {
            return completions;
        }

        if (label.equals("gm")) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                for (String mode : gameModes) {
                    if (mode.startsWith(input)) {
                        completions.add(mode);
                    }
                }
            } else if (args.length == 2) {
                String input = args[1].toLowerCase();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
            }
        }

        return completions;
    }

    private GameMode getGameModeFromLabel(String label) {
        return switch (label.toLowerCase()) {
            case "gms" -> GameMode.SURVIVAL;
            case "gmc" -> GameMode.CREATIVE;
            case "gma" -> GameMode.ADVENTURE;
            case "gmsp" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    private GameMode parseGameMode(String mode) {
        return switch (mode.toLowerCase()) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    private void setGameMode(CommandSender sender, Player target, GameMode gameMode) {
        target.setGameMode(gameMode);

        String modeName = getGameModeName(gameMode);

        if (sender.equals(target)) {
            target.sendMessage(ColorUtil.component("&7Gamemode auf &6" + modeName + " &7gesetzt!"));
        } else {
            target.sendMessage(ColorUtil.component("&7Dein Gamemode wurde auf &6" + modeName + " &7gesetzt!"));
            sender.sendMessage(ColorUtil.component("&7Gamemode für &6" + target.getName() + " &7auf &6" + modeName + " &7gesetzt!"));
        }
    }

    private String getGameModeName(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> "Survival";
            case CREATIVE -> "Creative";
            case ADVENTURE -> "Adventure";
            case SPECTATOR -> "Spectator";
        };
    }
}