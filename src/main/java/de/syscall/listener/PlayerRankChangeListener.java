package de.syscall.listener;

import de.syscall.SlownVectur;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class PlayerRankChangeListener implements Listener {

    private final SlownVectur plugin;

    public PlayerRankChangeListener(SlownVectur plugin) {
        this.plugin = plugin;
        registerLuckPermsEvents();
    }

    private void registerLuckPermsEvents() {
        try {
            EventBus eventBus = LuckPermsProvider.get().getEventBus();
            eventBus.subscribe(plugin, UserDataRecalculateEvent.class, this::onUserDataRecalculate);
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Registrieren der LuckPerms Events: " + e.getMessage());
        }
    }

    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        Player player = plugin.getServer().getPlayer(event.getUser().getUniqueId());
        if (player != null && player.isOnline()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getPrefixManager().updatePlayer(player);
                plugin.getTablistManager().updatePlayer(player);
                plugin.getScoreboardManager().updateBoard(player);
            });
        }
    }
}