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

public class PortalCommand implements CommandExecutor, TabCompleter {

    private final SlownVectur plugin;
    private final Map<UUID, Location> selections;
    private final List<String> subCommands = Arrays.asList("create", "delete", "remove", "list", "info", "set", "select", "enable", "disable", "reload");
    private final List<String> properties = Arrays.asList("action", "teleport", "permission", "frame-particle", "inner-particle", "spacing", "count", "speed", "days", "time", "value");
    private final List<String> actionTypes = Arrays.asList("TELEPORT", "COMMAND", "SERVER", "WORLD");
    private final List<String> particles = Arrays.asList("PORTAL", "ENCHANT", "FLAME", "HEART", "NAUTILUS", "ENCHANTMENT_TABLE", "SMOKE", "CLOUD", "REDSTONE", "SNOW_SHOVEL");
    private final List<String> days = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    public PortalCommand(SlownVectur plugin) {
        this.plugin = plugin;
        this.selections = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("slownvectur.portal")) {
            sender.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für diesen Command!"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(sender, args);
                break;
            case "delete":
            case "remove":
                handleDelete(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            case "set":
                handleSet(sender, args);
                break;
            case "select":
                handleSelect(sender, args);
                break;
            case "enable":
                handleEnable(sender, args, true);
                break;
            case "disable":
                handleEnable(sender, args, false);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("slownvectur.portal")) {
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
            String input = args[1].toLowerCase();

            switch (subCommand) {
                case "create":
                    completions.add("<portal-name>");
                    break;
                case "delete":
                case "remove":
                case "info":
                case "enable":
                case "disable":
                case "set":
                    for (Portal portal : plugin.getPortalManager().getAllPortals()) {
                        if (portal.getName().toLowerCase().startsWith(input)) {
                            completions.add(portal.getName());
                        }
                    }
                    break;
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String input = args[2].toLowerCase();

            if (subCommand.equals("set")) {
                for (String property : properties) {
                    if (property.startsWith(input)) {
                        completions.add(property);
                    }
                }
            }
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            String property = args[2].toLowerCase();
            String input = args[3].toLowerCase();

            if (subCommand.equals("set")) {
                switch (property) {
                    case "action":
                        for (String actionType : actionTypes) {
                            if (actionType.toLowerCase().startsWith(input)) {
                                completions.add(actionType);
                            }
                        }
                        break;
                    case "frame-particle":
                    case "inner-particle":
                        for (String particle : particles) {
                            if (particle.toLowerCase().startsWith(input)) {
                                completions.add(particle);
                            }
                        }
                        break;
                    case "permission":
                        completions.addAll(Arrays.asList("none", "slownvectur.portal.use", "slownvectur.portal.event"));
                        break;
                    case "spacing":
                        completions.addAll(Arrays.asList("0.3", "0.5", "1.0"));
                        break;
                    case "count":
                        completions.addAll(Arrays.asList("1", "5", "10"));
                        break;
                    case "speed":
                        completions.addAll(Arrays.asList("1", "2", "5"));
                        break;
                    case "days":
                        for (String day : days) {
                            if (day.toLowerCase().startsWith(input)) {
                                completions.add(day);
                            }
                        }
                        completions.add("none");
                        break;
                    case "time":
                        completions.addAll(Arrays.asList("20:00-22:00", "18:00-20:00", "none"));
                        break;
                    case "value":
                        completions.add("<wert>");
                        break;
                }
            }
        }

        return completions;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.component("&cDieser Command kann nur von Spielern ausgeführt werden!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal create <name>"));
            return;
        }

        String portalName = args[1];

        if (plugin.getPortalManager().hasPortal(portalName)) {
            sender.sendMessage(ColorUtil.component("&cEin Portal mit diesem Namen existiert bereits!"));
            return;
        }

        Location corner1 = selections.get(player.getUniqueId());
        if (corner1 == null) {
            sender.sendMessage(ColorUtil.component("&cBitte wähle zuerst einen Bereich mit /portal select aus!"));
            return;
        }

        Location corner2 = player.getLocation();
        plugin.getPortalManager().createPortal(portalName, corner1, corner2);
        selections.remove(player.getUniqueId());

        sender.sendMessage(ColorUtil.component("&7Portal &6" + portalName + " &7wurde erfolgreich erstellt!"));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal delete <name>"));
            return;
        }

        String portalName = args[1];
        Portal portal = plugin.getPortalManager().getPortal(portalName);

        if (portal == null) {
            sender.sendMessage(ColorUtil.component("&cPortal nicht gefunden!"));
            return;
        }

        plugin.getPortalManager().deletePortal(portalName);
        sender.sendMessage(ColorUtil.component("&7Portal &6" + portalName + " &7wurde gelöscht!"));
    }

    private void handleList(CommandSender sender) {
        Collection<Portal> portals = plugin.getPortalManager().getAllPortals();

        if (portals.isEmpty()) {
            sender.sendMessage(ColorUtil.component("&7Keine Portale vorhanden."));
            return;
        }

        sender.sendMessage(ColorUtil.component("&6&lPortale:"));
        sender.sendMessage(ColorUtil.component("&7&m───────────────────"));

        for (Portal portal : portals) {
            String status = portal.isEnabled() ? "&a✓" : "&c✗";
            String timeStatus = portal.isTimeAllowed() ? "&a" : "&c";
            sender.sendMessage(ColorUtil.component("&6" + portal.getName() + " " + status + " " + timeStatus + portal.getActionType().name()));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal info <name>"));
            return;
        }

        String portalName = args[1];
        Portal portal = plugin.getPortalManager().getPortal(portalName);

        if (portal == null) {
            sender.sendMessage(ColorUtil.component("&cPortal nicht gefunden!"));
            return;
        }

        sender.sendMessage(ColorUtil.component("&6&lPortal Info: " + portal.getName()));
        sender.sendMessage(ColorUtil.component("&7&m─────────────────────"));
        sender.sendMessage(ColorUtil.component("&7Status: " + (portal.isEnabled() ? "&aAktiv" : "&cInaktiv")));
        sender.sendMessage(ColorUtil.component("&7Aktion: &6" + portal.getActionType().name()));
        sender.sendMessage(ColorUtil.component("&7Wert: &6" + (portal.getActionValue() != null ? portal.getActionValue() : "Nicht gesetzt")));
        sender.sendMessage(ColorUtil.component("&7Permission: &6" + (portal.getPermission() != null ? portal.getPermission() : "Keine")));
        sender.sendMessage(ColorUtil.component("&7Frame Partikel: &6" + portal.getFrameParticle().name()));
        sender.sendMessage(ColorUtil.component("&7Inner Partikel: &6" + portal.getInnerParticle().name()));

        if (portal.getAllowedDays() != null || portal.getStartTime() != null) {
            sender.sendMessage(ColorUtil.component("&7&m─────────────────────"));
            if (portal.getAllowedDays() != null) {
                sender.sendMessage(ColorUtil.component("&7Erlaubte Tage: &6" + portal.getAllowedDays().toString()));
            }
            if (portal.getStartTime() != null && portal.getEndTime() != null) {
                sender.sendMessage(ColorUtil.component("&7Zeit: &6" + portal.getStartTime() + " - " + portal.getEndTime()));
            }
            sender.sendMessage(ColorUtil.component("&7Zeitstatus: " + (portal.isTimeAllowed() ? "&aErlaubt" : "&cNicht erlaubt")));
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal set <name> <property> <value>"));
            sender.sendMessage(ColorUtil.component("&7Properties: action, teleport, permission, frame-particle, inner-particle, spacing, count, speed, days, time"));
            return;
        }

        String portalName = args[1];
        String property = args[2].toLowerCase();
        String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        Portal portal = plugin.getPortalManager().getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(ColorUtil.component("&cPortal nicht gefunden!"));
            return;
        }

        switch (property) {
            case "action":
                try {
                    Portal.ActionType actionType = Portal.ActionType.valueOf(value.toUpperCase());
                    portal.setActionType(actionType);
                    sender.sendMessage(ColorUtil.component("&7Aktion auf &6" + actionType.name() + " &7gesetzt!"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Aktion! Verfügbar: TELEPORT, COMMAND, SERVER, WORLD"));
                    return;
                }
                break;
            case "teleport":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.component("&cDieser Command kann nur von Spielern ausgeführt werden!"));
                    return;
                }
                portal.setTeleportLocation(player.getLocation());
                portal.setActionType(Portal.ActionType.TELEPORT);
                sender.sendMessage(ColorUtil.component("&7Teleport-Location gesetzt!"));
                break;
            case "permission":
                portal.setPermission(value.equals("none") ? null : value);
                sender.sendMessage(ColorUtil.component("&7Permission auf &6" + value + " &7gesetzt!"));
                break;
            case "frame-particle":
                try {
                    Particle particle = Particle.valueOf(value.toUpperCase());
                    portal.setFrameParticle(particle);
                    sender.sendMessage(ColorUtil.component("&7Frame-Partikel auf &6" + particle.name() + " &7gesetzt!"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültiger Partikel!"));
                    return;
                }
                break;
            case "inner-particle":
                try {
                    Particle particle = Particle.valueOf(value.toUpperCase());
                    portal.setInnerParticle(particle);
                    sender.sendMessage(ColorUtil.component("&7Inner-Partikel auf &6" + particle.name() + " &7gesetzt!"));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültiger Partikel!"));
                    return;
                }
                break;
            case "spacing":
                try {
                    double spacing = Double.parseDouble(value);
                    portal.setParticleSpacing(spacing);
                    sender.sendMessage(ColorUtil.component("&7Partikel-Abstand auf &6" + spacing + " &7gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                    return;
                }
                break;
            case "count":
                try {
                    int count = Integer.parseInt(value);
                    portal.setParticleCount(count);
                    sender.sendMessage(ColorUtil.component("&7Partikel-Anzahl auf &6" + count + " &7gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                    return;
                }
                break;
            case "speed":
                try {
                    int speed = Integer.parseInt(value);
                    portal.setParticleSpeed(speed);
                    sender.sendMessage(ColorUtil.component("&7Partikel-Geschwindigkeit auf &6" + speed + " &7gesetzt!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("&cUngültige Zahl!"));
                    return;
                }
                break;
            case "days":
                if (value.equals("none")) {
                    portal.setAllowedDays(null);
                    sender.sendMessage(ColorUtil.component("&7Tagesbeschränkung entfernt!"));
                } else {
                    String[] dayNames = value.split(",");
                    Set<DayOfWeek> allowedDays = EnumSet.noneOf(DayOfWeek.class);
                    for (String dayName : dayNames) {
                        try {
                            allowedDays.add(DayOfWeek.valueOf(dayName.trim().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(ColorUtil.component("&cUngültiger Tag: " + dayName));
                            return;
                        }
                    }
                    portal.setAllowedDays(allowedDays);
                    sender.sendMessage(ColorUtil.component("&7Erlaubte Tage gesetzt: &6" + allowedDays.toString()));
                }
                break;
            case "time":
                if (value.equals("none")) {
                    portal.setStartTime(null);
                    portal.setEndTime(null);
                    sender.sendMessage(ColorUtil.component("&7Zeitbeschränkung entfernt!"));
                } else {
                    String[] times = value.split("-");
                    if (times.length != 2) {
                        sender.sendMessage(ColorUtil.component("&cFormat: HH:MM-HH:MM"));
                        return;
                    }
                    try {
                        LocalTime startTime = LocalTime.parse(times[0].trim());
                        LocalTime endTime = LocalTime.parse(times[1].trim());
                        portal.setStartTime(startTime);
                        portal.setEndTime(endTime);
                        sender.sendMessage(ColorUtil.component("&7Zeit gesetzt: &6" + startTime + " - " + endTime));
                    } catch (Exception e) {
                        sender.sendMessage(ColorUtil.component("&cUngültiges Zeitformat! Verwende HH:MM-HH:MM"));
                        return;
                    }
                }
                break;
            case "value":
                portal.setActionValue(value);
                sender.sendMessage(ColorUtil.component("&7Aktionswert auf &6" + value + " &7gesetzt!"));
                break;
            default:
                sender.sendMessage(ColorUtil.component("&cUnbekannte Property!"));
                return;
        }

        plugin.getPortalManager().updatePortal(portal);
    }

    private void handleSelect(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.component("&cDieser Command kann nur von Spielern ausgeführt werden!"));
            return;
        }

        selections.put(player.getUniqueId(), player.getLocation());
        sender.sendMessage(ColorUtil.component("&7Position 1 ausgewählt! Verwende &6/portal create <name> &7für Position 2."));
    }

    private void handleEnable(CommandSender sender, String[] args, boolean enabled) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.component("&cVerwendung: /portal " + (enabled ? "enable" : "disable") + " <name>"));
            return;
        }

        String portalName = args[1];
        Portal portal = plugin.getPortalManager().getPortal(portalName);

        if (portal == null) {
            sender.sendMessage(ColorUtil.component("&cPortal nicht gefunden!"));
            return;
        }

        portal.setEnabled(enabled);
        plugin.getPortalManager().updatePortal(portal);

        String status = enabled ? "&aaktiviert" : "&cdeaktiviert";
        sender.sendMessage(ColorUtil.component("&7Portal &6" + portalName + " &7wurde " + status + "&7!"));
    }

    private void handleReload(CommandSender sender) {
        plugin.getPortalManager().reloadConfig();
        sender.sendMessage(ColorUtil.component("&7Portal-Konfiguration neu geladen!"));
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.component("&6&lPortal Commands"));
        sender.sendMessage(ColorUtil.component("&7&m─────────────────────"));
        sender.sendMessage(ColorUtil.component("&6/portal select &7- Position 1 auswählen"));
        sender.sendMessage(ColorUtil.component("&6/portal create <name> &7- Portal erstellen (Position 2)"));
        sender.sendMessage(ColorUtil.component("&6/portal delete <name> &7- Portal löschen"));
        sender.sendMessage(ColorUtil.component("&6/portal list &7- Alle Portale anzeigen"));
        sender.sendMessage(ColorUtil.component("&6/portal info <name> &7- Portal-Informationen"));
        sender.sendMessage(ColorUtil.component("&6/portal set <name> <property> <value> &7- Eigenschaft setzen"));
        sender.sendMessage(ColorUtil.component("&6/portal enable/disable <name> &7- Portal aktivieren/deaktivieren"));
        sender.sendMessage(ColorUtil.component("&6/portal reload &7- Konfiguration neu laden"));
    }
}