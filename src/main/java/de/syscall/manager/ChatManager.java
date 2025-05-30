package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import de.syscall.util.AnimationUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatManager {

    private final SlownVectur plugin;
    private String chatFormat;
    private String joinMessage;
    private String leaveMessage;

    public ChatManager(SlownVectur plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.reloadConfig();
        this.chatFormat = plugin.getConfig().getString("chat.format", "{prefix} {player}: {message}");
        this.joinMessage = plugin.getConfig().getString("messages.join", "&a{player} &7ist dem Server beigetreten");
        this.leaveMessage = plugin.getConfig().getString("messages.leave", "&c{player} &7hat den Server verlassen");
    }

    public void handleChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (player.hasPermission("slownvectur.chat.color")) {
            message = ColorUtil.colorize(message);
        }

        if (player.hasPermission("slownvectur.chat.animation")) {
            message = AnimationUtil.processAnimations(message);
        }

        String prefix = plugin.getPrefixManager().getPrefix(player);
        String finalMessage = chatFormat
                .replace("{prefix}", prefix)
                .replace("{player}", player.getName())
                .replace("{message}", message);

        finalMessage = ColorUtil.colorize(finalMessage);

        event.setCancelled(true);

        Component component = Component.text(finalMessage);
        plugin.getServer().broadcast(component);
    }

    public Component getJoinMessage(Player player) {
        String prefix = plugin.getPrefixManager().getPrefix(player);
        String message = joinMessage
                .replace("{prefix}", prefix)
                .replace("{player}", player.getName());

        message = ColorUtil.colorize(message);
        return Component.text(message);
    }

    public Component getLeaveMessage(Player player) {
        String prefix = plugin.getPrefixManager().getPrefix(player);
        String message = leaveMessage
                .replace("{prefix}", prefix)
                .replace("{player}", player.getName());

        message = ColorUtil.colorize(message);
        return Component.text(message);
    }

    public void reload() {
        loadConfig();
    }
}