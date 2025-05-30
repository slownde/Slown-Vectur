package de.syscall.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static String colorize(String text) {
        if (text == null) return "";

        text = translateHexColorCodes(text);
        text = ChatColor.translateAlternateColorCodes('&', text);

        return text;
    }

    public static String stripColors(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }

    private static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5));
        }

        return matcher.appendTail(buffer).toString();
    }

    public static String rainbow(String text) {
        ChatColor[] colors = {
                ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
                ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE
        };

        StringBuilder result = new StringBuilder();
        int colorIndex = 0;

        for (char c : text.toCharArray()) {
            if (c == ' ') {
                result.append(c);
            } else {
                result.append(colors[colorIndex]).append(c);
                colorIndex = (colorIndex + 1) % colors.length;
            }
        }

        return result.toString();
    }

    public static String gradient(String text, ChatColor startColor, ChatColor endColor) {
        if (text.length() <= 1) {
            return startColor + text;
        }

        StringBuilder result = new StringBuilder();
        float step = 1.0f / (text.length() - 1);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }

            float ratio = i * step;
            ChatColor interpolatedColor = interpolateColor(startColor, endColor, ratio);
            result.append(interpolatedColor).append(c);
        }

        return result.toString();
    }

    private static ChatColor interpolateColor(ChatColor start, ChatColor end, float ratio) {
        if (ratio <= 0) return start;
        if (ratio >= 1) return end;

        ChatColor[] transition = {start, end};
        return transition[Math.round(ratio)];
    }
}