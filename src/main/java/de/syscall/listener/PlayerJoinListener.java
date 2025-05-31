package de.syscall.listener;

import de.syscall.SlownVectur;
import de.syscall.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class PlayerJoinListener implements Listener {

    private final SlownVectur plugin;

    public PlayerJoinListener(SlownVectur plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.joinMessage(plugin.getChatManager().getJoinMessage(player));

        showLoadingAnimation(player);

        plugin.getPlayerDataManager().loadPlayerData(player).thenRun(() -> {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

            if (data != null && data.isDataLoaded()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    hideLoadingAnimation(player);
                    setupPlayer(player);
                });
            }
        });
    }

    private void showLoadingAnimation(Player player) {
        new BukkitRunnable() {
            private int progress = 0;
            private int dots = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
                if (data != null && data.isDataLoaded()) {
                    cancel();
                    return;
                }

                progress += 10;
                if (progress > 100) progress = 100;

                dots = (dots + 1) % 4;
                String dotString = ".".repeat(dots);

                Component titleComponent;
                Component subtitleComponent;

                if (player.hasPlayedBefore()) {
                    titleComponent = Component.text("§6Daten werden geladen" + dotString);
                    subtitleComponent = Component.text("§7Fortschritt: §a" + progress + "%");
                } else {
                    titleComponent = Component.text("§6Daten werden erstellt" + dotString);
                    subtitleComponent = Component.text("§7Willkommen auf §6Slown Network§7!");
                }

                Title title = Title.title(
                        titleComponent,
                        subtitleComponent,
                        Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
                );

                player.showTitle(title);

                if (progress >= 100) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            hideLoadingAnimation(player);
                        }
                    }, 20L);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void hideLoadingAnimation(Player player) {
        Title clearTitle = Title.title(
                Component.empty(),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(500), Duration.ofMillis(500))
        );
        player.showTitle(clearTitle);
    }

    private void setupPlayer(Player player) {
        plugin.getPrefixManager().updatePlayer(player);
        plugin.getTablistManager().updatePlayer(player);
        plugin.getScoreboardManager().createBoard(player);

        if (!player.hasPlayedBefore()) {
            showWelcomeMessage(player);
        }
    }

    private void showWelcomeMessage(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Title welcomeTitle = Title.title(
                    Component.text("§6§lWillkommen!"),
                    Component.text("§7Viel Spaß auf §6Slown Network§7!"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            );
            player.showTitle(welcomeTitle);
        }, 40L);
    }
}