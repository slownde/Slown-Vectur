package de.syscall.command;

import de.syscall.SlownVectur;
import de.syscall.data.Portal;
import de.syscall.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

public class PortalCommand implements CommandExecutor, TabCompleter {

    private final SlownVectur plugin;
    private final Map<UUID, Location> selections;

    public PortalCommand(SlownVectur plugin) {
        this.plugin = plugin;
        this.selections = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("slownvectur.portal")) {
            sender.sendMessage(ColorUtil.component("&cKeine Berechtigung!"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(sender, args);
            case "delete", "remove" -> handleDelete(sender, args);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "set" -> handleSet(sender, args);
            case "select" -> handleSelect(sender);
            case "enable" -> handleToggle(sender, args, true);
            case "disable" -> handleToggle(sender, args, false);
            case "reload" -> handleReload(sender);
            default -> showHelp(sender);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("slownvectur.portal")) return Collections.emptyList();

        if (args.length == 1) {
            return Stream.of("create", "delete", "list", "info", "set", "select", "enable", "disable", "reload").filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }

        if (args.length == 2 && Arrays.asList("delete", "info", "set", "enable", "disable").contains(args[0].toLowerCase())) {
            return plugin.getPortalManager().getAllPortals().stream()
                    .map(Portal::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return Stream.of("action", "teleport", "permission", "particle", "spacing", "density", "days", "time", "value").filter(s -> s.startsWith(args[2].toLowerCase())).toList();
        }

        return Collections.emptyList();
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.component("&cNur Spieler!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal create <name>"));
            return;
        }

        String name = args[1];
        if (plugin.getPortalManager().hasPortal(name)) {
            sender.sendMessage(ColorUtil.component("&cPortal existiert bereits!"));
            return;
        }

        Location corner1 = selections.get(player.getUniqueId());
        if (corner1 == null) {
            sender.sendMessage(ColorUtil.component("&cErstelle eine Auswahl mit /portal select!"));
            return;
        }

        plugin.getPortalManager().createPortal(name, corner1, player.getLocation());
        selections.remove(player.getUniqueId());
        sender.sendMessage(ColorUtil.component("&aPortal erstellt!"));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal delete <name>"));
            return;
        }

        String name = args[1];
        if (!plugin.getPortalManager().hasPortal(name)) {
            sender.sendMessage(ColorUtil.component("&cPortal nicht gefunden!"));
            return;
        }

        plugin.getPortalManager().deletePortal(name);
        sender.sendMessage(ColorUtil.component("&aPortal gelöscht!"));
    }

    private void handleList(CommandSender sender) {
        Collection<Portal> portals = plugin.getPortalManager().getAllPortals();
        if (portals.isEmpty()) {
            sender.sendMessage(ColorUtil.component("&7Keine Portale vorhanden."));
            return;
        }

        sender.sendMessage(ColorUtil.component("&6Portale:"));
        portals.forEach(portal -> {
            String status = portal.isEnabled() ? "&a✓" : "&c✗";
            sender.sendMessage(ColorUtil.component("&7- " + status + " &6" + portal.getName()));
        });
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal info <name>"));
            return;
        }

        Portal portal = plugin.getPortalManager().getPortal(args[1]);
        if (portal == null) {
            sender.sendMessage(ColorUtil.component("&cPortal nicht gefunden!"));
            return;
        }

        sender.sendMessage(ColorUtil.component("&6Portal: " + portal.getName()));
        sender.sendMessage(ColorUtil.component("&7Status: " + (portal.isEnabled() ? "&aAktiv" : "&cInaktiv")));
        sender.sendMessage(ColorUtil.component("&7Aktion: &6" + portal.getActionType()));
        sender.sendMessage(ColorUtil.component("&7Partikel: &6" + portal.getParticle()));
        sender.sendMessage(ColorUtil.component("&7Zeitstatus: " + (portal.isTimeAllowed() ? "&aErlaubt" : "&cGesperrt")));
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal set <name> <property> <value>"));
            return;
        }

        Portal portal = plugin.getPortalManager().getPortal(args[1]);
        if (portal == null) {
            sender.sendMessage(ColorUtil.component("&cPortal nicht gefunden!"));
            return;
        }

        String property = args[2].toLowerCase();
        String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        switch (property) {
            case "action" -> {
                try {
                    portal.setActionType(Portal.ActionType.valueOf(value.toUpperCase()));
                    sender.sendMessage(ColorUtil.component("&aAktion gesetzt!"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Aktion!"));
                    return;
                }
            }
            case "teleport" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.component("&cNur Spieler!"));
                    return;
                }
                portal.setTeleportLocation(player.getLocation());
                portal.setActionType(Portal.ActionType.TELEPORT);
                sender.sendMessage(ColorUtil.component("&aTeleport-Location gesetzt!"));
            }
            case "permission" -> {
                portal.setPermission(value.equals("none") ? null : value);
                sender.sendMessage(ColorUtil.component("&aPermission gesetzt!"));
            }
            case "particle" -> {
                try {
                    portal.setParticle(Particle.valueOf(value.toUpperCase()));
                    sender.sendMessage(ColorUtil.component("&aPartikel gesetzt!"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültiger Partikel!"));
                    return;
                }
            }
            case "spacing" -> {
                try {
                    portal.setParticleSpacing(Double.parseDouble(value));
                    sender.sendMessage(ColorUtil.component("&aAbstand gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                    return;
                }
            }
            case "density" -> {
                try {
                    portal.setParticleDensity(Integer.parseInt(value));
                    sender.sendMessage(ColorUtil.component("&aDichte gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                    return;
                }
            }
            case "days" -> {
                if (value.equals("none")) {
                    portal.setAllowedDays(null);
                } else {
                    Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
                    for (String day : value.split(",")) {
                        try {
                            days.add(DayOfWeek.valueOf(day.trim().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(ColorUtil.component("&cUngültiger Tag: " + day));
                            return;
                        }
                    }
                    portal.setAllowedDays(days);
                }
                sender.sendMessage(ColorUtil.component("&aTage gesetzt!"));
            }
            case "time" -> {
                if (value.equals("none")) {
                    portal.setStartTime(null);
                    portal.setEndTime(null);
                } else {
                    String[] times = value.split("-");
                    if (times.length != 2) {
                        sender.sendMessage(ColorUtil.component("&cFormat: HH:MM-HH:MM"));
                        return;
                    }
                    try {
                        portal.setStartTime(LocalTime.parse(times[0].trim()));
                        portal.setEndTime(LocalTime.parse(times[1].trim()));
                    } catch (Exception e) {
                        sender.sendMessage(ColorUtil.component("&cUngültiges Format!"));
                        return;
                    }
                }
                sender.sendMessage(ColorUtil.component("&aZeit gesetzt!"));
            }
            case "value" -> {
                portal.setActionValue(value);
                sender.sendMessage(ColorUtil.component("&aWert gesetzt!"));
            }
            default -> {
                sender.sendMessage(ColorUtil.component("&cUnbekannte Property!"));
                return;
            }
        }

        plugin.getPortalManager().updatePortal(portal);
    }

    private void handleSelect(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.component("&cNur Spieler!"));
            return;
        }

        selections.put(player.getUniqueId(), player.getLocation());
        sender.sendMessage(ColorUtil.component("&aPosition 1 ausgewählt!"));
    }

    private void handleToggle(CommandSender sender, String[] args, boolean enabled) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal " + (enabled ? "enable" : "disable") + " <n>"));
            return;
        }

        Portal portal = plugin.getPortalManager().getPortal(args[1]);
        if (portal == null) {
            sender.sendMessage(ColorUtil.component("&cPortal nicht gefunden!"));
            return;
        }

        portal.setEnabled(enabled);
        plugin.getPortalManager().updatePortal(portal);

        String status = enabled ? "&aaktiviert" : "&cdeaktiviert";
        sender.sendMessage(ColorUtil.component("&7Portal " + status + "&7!"));
    }

    private void handleReload(CommandSender sender) {
        plugin.getPortalManager().reloadConfig();
        sender.sendMessage(ColorUtil.component("&aKonfiguration neu geladen!"));
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.component("&6Portal Commands:"));
        sender.sendMessage(ColorUtil.component("&7/portal select - Position auswählen"));
        sender.sendMessage(ColorUtil.component("&7/portal create <n> - Portal erstellen"));
        sender.sendMessage(ColorUtil.component("&7/portal delete <n> - Portal löschen"));
        sender.sendMessage(ColorUtil.component("&7/portal list - Alle Portale"));
        sender.sendMessage(ColorUtil.component("&7/portal info <n> - Portal-Info"));
        sender.sendMessage(ColorUtil.component("&7/portal set <n> <prop> <val> - Eigenschaft setzen"));
        sender.sendMessage(ColorUtil.component("&7/portal enable/disable <n> - Aktivieren/Deaktivieren"));
        sender.sendMessage(ColorUtil.component("&7/portal reload - Neu laden"));
    }
}