package de.syscall.command;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand implements CommandExecutor {

    private final SlownVectur plugin;

    public FlyCommand(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtil.component("&cDieser Command kann nur von Spielern ausgeführt werden!"));
                return true;
            }

            if (!player.hasPermission("slownvectur.fly")) {
                player.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
                return true;
            }

            toggleFly(player, player);
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("slownvectur.fly.others")) {
                sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ColorUtil.component("&cSpieler nicht gefunden!"));
                return true;
            }

            toggleFly(sender, target);
            return true;
        }

        sender.sendMessage(ColorUtil.component("&cVerwendung: /fly [spieler]"));
        return true;
    }

    private void toggleFly(CommandSender sender, Player target) {
        boolean newFlyState = !target.getAllowFlight();

        target.setAllowFlight(newFlyState);
        target.setFlying(newFlyState && target.isFlying());

        String status = newFlyState ? "&aaktiviert" : "&cdeaktiviert";

        if (sender.equals(target)) {
            target.sendMessage(ColorUtil.component("&7Fliegen wurde " + status + "&7!"));
        } else {
            target.sendMessage(ColorUtil.component("&7Fliegen wurde " + status + "&7!"));
            sender.sendMessage(ColorUtil.component("&7Fliegen für &6" + target.getName() + " &7wurde " + status + "&7!"));
        }
    }
}