package de.syscall.listener;

import de.syscall.SlownVectur;
import de.syscall.data.Portal;
import de.syscall.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalListener implements Listener {

    private final SlownVectur plugin;
    private final Map<UUID, String> lastPortal;

    public PortalListener(SlownVectur plugin) {
        this.plugin = plugin;
        this.lastPortal = new HashMap<>();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Portal portal = plugin.getPortalManager().getPortalAtLocation(event.getTo());

        if (portal != null) {
            String lastPortalName = lastPortal.get(playerId);
            if (!portal.getName().equals(lastPortalName)) {
                handlePortalEnter(player, portal);
                lastPortal.put(playerId, portal.getName());
            }
        } else {
            String lastPortalName = lastPortal.remove(playerId);
            if (lastPortalName != null) {
                handlePortalLeave(player, lastPortalName);
            }
        }
    }

    private void handlePortalEnter(Player player, Portal portal) {
        if (!portal.isEnabled()) {
            player.sendMessage(ColorUtil.component("&cDieses Portal ist deaktiviert!"));
            return;
        }

        if (!portal.isTimeAllowed()) {
            player.sendMessage(ColorUtil.component("&cDieses Portal ist zur Zeit nicht verfügbar!"));
            if (portal.getStartTime() != null && portal.getEndTime() != null) {
                player.sendMessage(ColorUtil.component("&7Verfügbar: &6" + portal.getStartTime() + " - " + portal.getEndTime()));
            }
            if (portal.getAllowedDays() != null) {
                player.sendMessage(ColorUtil.component("&7Tage: &6" + portal.getAllowedDays().toString()));
            }
            return;
        }

        if (portal.getPermission() != null && !player.hasPermission(portal.getPermission())) {
            player.sendMessage(ColorUtil.component("&cDu hast keine Berechtigung für dieses Portal!"));
            return;
        }

        player.sendMessage(ColorUtil.component("&7Du betrittst das Portal &6" + portal.getName() + "&7..."));
    }

    private void handlePortalLeave(Player player, String portalName) {
        player.sendMessage(ColorUtil.component("&7Du verlässt das Portal &6" + portalName + "&7."));
    }
}