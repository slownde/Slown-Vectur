package de.syscall.command;

import de.syscall.SlownVectur;
import de.syscall.data.Portal;
import de.syscall.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

public class PortalCommand implements CommandExecutor {

    private final SlownVectur plugin;
    private final Map<UUID, Location> selections;

    public PortalCommand(SlownVectur plugin) {
        this.plugin = plugin;
        this.selections = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("slownvectur.portal")) {
            sender.sendMessage(ColorUtil.colorize("&cDu hast keine Berechtigung für diesen Command!"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create" -> handleCreate(sender, args);
            case "delete", "remove" -> handleDelete(sender, args);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "set" -> handleSet(sender, args);
            case "select" -> handleSelect(sender, args);
            case "enable" -> handleEnable(sender, args, true);
            case "disable" -> handleEnable(sender, args, false);
            case "reload" -> handleReload(sender);
            default -> showHelp(sender);
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.colorize("&cDieser Command kann nur von Spielern ausgeführt werden!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&cVerwendung: /portal create <name>"));
            return;
        }

        String portalName = args[1];

        if (plugin.getPortalManager().hasPortal(portalName)) {
            sender.sendMessage(ColorUtil.colorize("&cEin Portal mit diesem Namen existiert bereits!"));
            return;
        }

        Location corner1 = selections.get(player.getUniqueId());
        if (corner1 == null) {
            sender.sendMessage(ColorUtil.colorize("&cBitte wähle zuerst einen Bereich mit /portal select aus!"));
            return;
        }

        Location corner2 = player.getLocation();
        plugin.getPortalManager().createPortal(portalName, corner1, corner2);
        selections.remove(player.getUniqueId());

        sender.sendMessage(ColorUtil.colorize("&7Portal &6" + portalName + " &7wurde erfolgreich erstellt!"));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&cVerwendung: /portal delete <name>"));
            return;
        }

        String portalName = args[1];
        Portal portal = plugin.getPortalManager().getPortal(portalName);

        if (portal == null) {
            sender.sendMessage(ColorUtil.colorize("&cPortal nicht gefunden!"));
            return;
        }

        plugin.getPortalManager().deletePortal(portalName);
        sender.sendMessage(ColorUtil.colorize("&7Portal &6" + portalName + " &7wurde gelöscht!"));
    }

    private void handleList(CommandSender sender) {
        Collection<Portal> portals = plugin.getPortalManager().getAllPortals();

        if (portals.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize("&7Keine Portale vorhanden."));
            return;
        }

        sender.sendMessage(ColorUtil.colorize("&6&lPortale:"));
        sender.sendMessage(ColorUtil.colorize("&7&m───────────────────"));

        for (Portal portal : portals) {
            String status = portal.isEnabled() ? "&a✓" : "&c✗";
            String timeStatus = portal.isTimeAllowed() ? "&a" : "&c";
            sender.sendMessage(ColorUtil.colorize("&6" + portal.getName() + " " + status + " " + timeStatus + portal.getActionType().name()));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&cVerwendung: /portal info <name>"));
            return;
        }

        String portalName = args[1];
        Portal portal = plugin.getPortalManager().getPortal(portalName);

        if (portal == null) {
            sender.sendMessage(ColorUtil.colorize("&cPortal nicht gefunden!"));
            return;
        }

        sender.sendMessage(ColorUtil.colorize("&6&lPortal Info: " + portal.getName()));
        sender.sendMessage(ColorUtil.colorize("&7&m─────────────────────"));
        sender.sendMessage(ColorUtil.colorize("&7Status: " + (portal.isEnabled() ? "&aAktiv" : "&cInaktiv")));
        sender.sendMessage(ColorUtil.colorize("&7Aktion: &6" + portal.getActionType().name()));
        sender.sendMessage(ColorUtil.colorize("&7Wert: &6" + (portal.getActionValue() != null ? portal.getActionValue() : "Nicht gesetzt")));
        sender.sendMessage(ColorUtil.colorize("&7Permission: &6" + (portal.getPermission() != null ? portal.getPermission() : "Keine")));
        sender.sendMessage(ColorUtil.colorize("&7Frame Partikel: &6" + portal.getFrameParticle().name()));
        sender.sendMessage(ColorUtil.colorize("&7Inner Partikel: &6" + portal.getInnerParticle().name()));

        if (portal.getAllowedDays() != null || portal.getStartTime() != null) {
            sender.sendMessage(ColorUtil.colorize("&7&m─────────────────────"));
            if (portal.getAllowedDays() != null) {
                sender.sendMessage(ColorUtil.colorize("&7Erlaubte Tage: &6" + portal.getAllowedDays().toString()));
            }
            if (portal.getStartTime() != null && portal.getEndTime() != null) {
                sender.sendMessage(ColorUtil.colorize("&7Zeit: &6" + portal.getStartTime() + " - " + portal.getEndTime()));
            }
            sender.sendMessage(ColorUtil.colorize("&7Zeitstatus: " + (portal.isTimeAllowed() ? "&aErlaubt" : "&cNicht erlaubt")));
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ColorUtil.colorize("&cVerwendung: /portal set <name> <property> <value>"));
            sender.sendMessage(ColorUtil.colorize("&7Properties: action, teleport, permission, frame-particle, inner-particle, spacing, count, speed, days, time"));
            return;
        }

        String portalName = args[1];
        String property = args[2].toLowerCase();
        String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        Portal portal = plugin.getPortalManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(ColorUtil.colorize("&cPortal nicht gefunden!"));
            return;
        }

        switch (property) {
            case "action" -> {
                try {
                    Portal.ActionType actionType = Portal.ActionType.valueOf(value.toUpperCase());
                    portal.setActionType(actionType);
                    sender.sendMessage(ColorUtil.colorize("&7Aktion auf &6" + actionType.name() + " &7gesetzt!"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.colorize("&cUngültige Aktion! Verfügbar: TELEPORT, COMMAND, SERVER, WORLD"));
                    return;
                }
            }
            case "teleport" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.colorize("&cDieser Command kann nur von Spielern ausgeführt werden!"));
                    return;
                }
                portal.setTeleportLocation(player.getLocation());
                portal.setActionType(Portal.ActionType.TELEPORT);
                sender.sendMessage(ColorUtil.colorize("&7Teleport-Location gesetzt!"));
            }
            case "permission" -> {
                portal.setPermission(value.equals("none") ? null : value);
                sender.sendMessage(ColorUtil.colorize("&7Permission auf &6" + value + " &7gesetzt!"));
            }
            case "frame-particle" -> {
                try {
                    Particle particle = Particle.valueOf(value.toUpperCase());
                    portal.setFrameParticle(particle);
                    sender.sendMessage(ColorUtil.colorize("&7Frame-Partikel auf &6" + particle.name() + " &7gesetzt!"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.colorize("&cUngültiger Partikel!"));
                    return;
                }
            }
            case "inner-particle" -> {
                try {
                    Particle particle = Particle.valueOf(value.toUpperCase());
                    portal.setInnerParticle(particle);
                    sender.sendMessage(ColorUtil.colorize("&7Inner-Partikel auf &6" + particle.name() + " &7gesetzt!"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.colorize("&cUngültiger Partikel!"));
                    return;
                }
            }
            case "spacing" -> {
                try {
                    double spacing = Double.parseDouble(value);
                    portal.setParticleSpacing(spacing);
                    sender.sendMessage(ColorUtil.colorize("&7Partikel-Abstand auf &6" + spacing + " &7gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&cUngültige Zahl!"));
                    return;
                }
            }
            case "count" -> {
                try {
                    int count = Integer.parseInt(value);
                    portal.setParticleCount(count);
                    sender.sendMessage(ColorUtil.colorize("&7Partikel-Anzahl auf &6" + count + " &7gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&cUngültige Zahl!"));
                    return;
                }
            }
            case "speed" -> {
                try {
                    int speed = Integer.parseInt(value);
                    portal.setParticleSpeed(speed);
                    sender.sendMessage(ColorUtil.colorize("&7Partikel-Geschwindigkeit auf &6" + speed + " &7gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&cUngültige Zahl!"));
                    return;
                }
            }
            case "days" -> {
                if (value.equals("none")) {
                    portal.setAllowedDays(null);
                    sender.sendMessage(ColorUtil.colorize("&7Tagesbeschränkung entfernt!"));
                } else {
                    String[] dayNames = value.split(",");
                    Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
                    for (String dayName : dayNames) {
                        try {
                            days.add(DayOfWeek.valueOf(dayName.trim().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(ColorUtil.colorize("&cUngültiger Tag: " + dayName));
                            return;
                        }
                    }
                    portal.setAllowedDays(days);
                    sender.sendMessage(ColorUtil.colorize("&7Erlaubte Tage gesetzt: &6" + days.toString()));
                }
            }
            case "time" -> {
                if (value.equals("none")) {
                    portal.setStartTime(null);
                    portal.setEndTime(null);
                    sender.sendMessage(ColorUtil.colorize("&7Zeitbeschränkung entfernt!"));
                } else {
                    String[] times = value.split("-");
                    if (times.length != 2) {
                        sender.sendMessage(ColorUtil.colorize("&cFormat: HH:MM-HH:MM"));
                        return;
                    }
                    try {
                        LocalTime startTime = LocalTime.parse(times[0].trim());
                        LocalTime endTime = LocalTime.parse(times[1].trim());
                        portal.setStartTime(startTime);
                        portal.setEndTime(endTime);
                        sender.sendMessage(ColorUtil.colorize("&7Zeit gesetzt: &6" + startTime + " - " + endTime));
                    } catch (Exception e) {
                        sender.sendMessage(ColorUtil.colorize("&cUngültiges Zeitformat! Verwende HH:MM-HH:MM"));
                        return;
                    }
                }
            }
            case "value" -> {
                portal.setActionValue(value);
                sender.sendMessage(ColorUtil.colorize("&7Aktionswert auf &6" + value + " &7gesetzt!"));
            }
            default -> {
                sender.sendMessage(ColorUtil.colorize("&cUnbekannte Property!"));
                return;
            }
        }

        plugin.getPortalManager().updatePortal(portal);
    }

    private void handleSelect(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.colorize("&cDieser Command kann nur von Spielern ausgeführt werden!"));
            return;
        }

        selections.put(player.getUniqueId(), player.getLocation());
        sender.sendMessage(ColorUtil.colorize("&7Position 1 ausgewählt! Verwende &6/portal create <name> &7für Position 2."));
    }

    private void handleEnable(CommandSender sender, String[] args, boolean enabled) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&cVerwendung: /portal " + (enabled ? "enable" : "disable") + " <name>"));
            return;
        }

        String portalName = args[1];
        Portal portal = plugin.getPortalManager().getPortal(portalName);

        if (portal == null) {
            sender.sendMessage(ColorUtil.colorize("&cPortal nicht gefunden!"));
            return;
        }

        portal.setEnabled(enabled);
        plugin.getPortalManager().updatePortal(portal);

        String status = enabled ? "&aaktiviert" : "&cdeaktiviert";
        sender.sendMessage(ColorUtil.colorize("&7Portal &6" + portalName + " &7wurde " + status + "&7!"));
    }

    private void handleReload(CommandSender sender) {
        plugin.getPortalManager().reloadConfig();
        sender.sendMessage(ColorUtil.colorize("&7Portal-Konfiguration neu geladen!"));
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&6&lPortal Commands"));
        sender.sendMessage(ColorUtil.colorize("&7&m─────────────────────"));
        sender.sendMessage(ColorUtil.colorize("&6/portal select &7- Position 1 auswählen"));
        sender.sendMessage(ColorUtil.colorize("&6/portal create <name> &7- Portal erstellen (Position 2)"));
        sender.sendMessage(ColorUtil.colorize("&6/portal delete <name> &7- Portal löschen"));
        sender.sendMessage(ColorUtil.colorize("&6/portal list &7- Alle Portale anzeigen"));
        sender.sendMessage(ColorUtil.colorize("&6/portal info <name> &7- Portal-Informationen"));
        sender.sendMessage(ColorUtil.colorize("&6/portal set <name> <property> <value> &7- Eigenschaft setzen"));
        sender.sendMessage(ColorUtil.colorize("&6/portal enable/disable <name> &7- Portal aktivieren/deaktivieren"));
        sender.sendMessage(ColorUtil.colorize("&6/portal reload &7- Konfiguration neu laden"));
    }
}