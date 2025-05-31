package de.syscall.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    public static Component component(String text) {
        if (text == null) return Component.empty();
        text = translateHexColorCodes(text);
        return LEGACY_SERIALIZER.deserialize(text);
    }

    public static String colorize(String text) {
        if (text == null) return "";
        return LEGACY_SERIALIZER.serialize(component(text));
    }

    public static String stripColors(String text) {
        if (text == null) return "";
        return PLAIN_SERIALIZER.serialize(component(text));
    }

    private static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, "§x"
                    + "§" + group.charAt(0) + "§" + group.charAt(1)
                    + "§" + group.charAt(2) + "§" + group.charAt(3)
                    + "§" + group.charAt(4) + "§" + group.charAt(5));
        }

        return matcher.appendTail(buffer).toString();
    }

    public static Component rainbow(String text) {
        TextColor[] colors = {
                TextColor.color(255, 85, 85),   // RED
                TextColor.color(255, 170, 0),   // GOLD
                TextColor.color(255, 255, 85),  // YELLOW
                TextColor.color(85, 255, 85),   // GREEN
                TextColor.color(85, 255, 255),  // AQUA
                TextColor.color(85, 85, 255),   // BLUE
                TextColor.color(255, 85, 255)   // LIGHT_PURPLE
        };

        Component result = Component.empty();
        int colorIndex = 0;

        for (char c : text.toCharArray()) {
            if (c == ' ') {
                result = result.append(Component.text(c));
            } else {
                result = result.append(Component.text(c).color(colors[colorIndex]));
                colorIndex = (colorIndex + 1) % colors.length;
            }
        }

        return result;
    }

    public static Component gradient(String text, TextColor startColor, TextColor endColor) {
        if (text.length() <= 1) {
            return Component.text(text).color(startColor);
        }

        Component result = Component.empty();
        float step = 1.0f / (text.length() - 1);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result = result.append(Component.text(c));
                continue;
            }

            float ratio = i * step;
            TextColor interpolatedColor = interpolateColor(startColor, endColor, ratio);
            result = result.append(Component.text(c).color(interpolatedColor));
        }

        return result;
    }

    private static TextColor interpolateColor(TextColor start, TextColor end, float ratio) {
        if (ratio <= 0) return start;
        if (ratio >= 1) return end;

        int startR = start.red();
        int startG = start.green();
        int startB = start.blue();

        int endR = end.red();
        int endG = end.green();
        int endB = end.blue();

        int r = (int) (startR + (endR - startR) * ratio);
        int g = (int) (startG + (endG - startG) * ratio);
        int b = (int) (startB + (endB - startB) * ratio);

        return TextColor.color(r, g, b);
    }
}