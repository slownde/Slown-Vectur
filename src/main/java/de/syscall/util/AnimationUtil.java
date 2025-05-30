package de.syscall.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimationUtil {

    private static final Pattern RAINBOW_PATTERN = Pattern.compile("\\{rainbow\\}(.*?)\\{/rainbow\\}");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("\\{gradient:(\\w+)-(\\w+)\\}(.*?)\\{/gradient\\}");
    private static final Pattern WAVE_PATTERN = Pattern.compile("\\{wave\\}(.*?)\\{/wave\\}");
    private static final Pattern GLOW_PATTERN = Pattern.compile("\\{glow\\}(.*?)\\{/glow\\}");

    public static String processAnimations(String text) {
        if (text == null) return "";

        text = processRainbow(text);
        text = processGradient(text);
        text = processWave(text);
        text = processGlow(text);

        return text;
    }

    private static String processRainbow(String text) {
        Matcher matcher = RAINBOW_PATTERN.matcher(text);

        while (matcher.find()) {
            String content = matcher.group(1);
            String rainbow = ColorUtil.rainbow(content);
            text = text.replace(matcher.group(0), rainbow);
        }

        return text;
    }

    private static String processGradient(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);

        while (matcher.find()) {
            String startColorName = matcher.group(1);
            String endColorName = matcher.group(2);
            String content = matcher.group(3);

            ChatColor startColor = parseColor(startColorName);
            ChatColor endColor = parseColor(endColorName);

            if (startColor != null && endColor != null) {
                String gradient = ColorUtil.gradient(content, startColor, endColor);
                text = text.replace(matcher.group(0), gradient);
            }
        }

        return text;
    }

    private static String processWave(String text) {
        Matcher matcher = WAVE_PATTERN.matcher(text);

        while (matcher.find()) {
            String content = matcher.group(1);
            String wave = createWaveEffect(content);
            text = text.replace(matcher.group(0), wave);
        }

        return text;
    }

    private static String processGlow(String text) {
        Matcher matcher = GLOW_PATTERN.matcher(text);

        while (matcher.find()) {
            String content = matcher.group(1);
            String glow = createGlowEffect(content);
            text = text.replace(matcher.group(0), glow);
        }

        return text;
    }

    private static String createWaveEffect(String text) {
        ChatColor[] waveColors = {
                ChatColor.DARK_BLUE, ChatColor.BLUE, ChatColor.AQUA,
                ChatColor.WHITE, ChatColor.AQUA, ChatColor.BLUE
        };

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }

            int colorIndex = (i + (int)(System.currentTimeMillis() / 200)) % waveColors.length;
            result.append(waveColors[colorIndex]).append(c);
        }

        return result.toString();
    }

    private static String createGlowEffect(String text) {
        ChatColor[] glowColors = {
                ChatColor.WHITE, ChatColor.YELLOW, ChatColor.GOLD,
                ChatColor.YELLOW, ChatColor.WHITE
        };

        StringBuilder result = new StringBuilder();
        int glowPhase = (int)((System.currentTimeMillis() / 300) % glowColors.length);

        for (char c : text.toCharArray()) {
            if (c == ' ') {
                result.append(c);
            } else {
                result.append(glowColors[glowPhase]).append(c);
            }
        }

        return result.toString();
    }

    private static ChatColor parseColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "red" -> ChatColor.RED;
            case "blue" -> ChatColor.BLUE;
            case "green" -> ChatColor.GREEN;
            case "yellow" -> ChatColor.YELLOW;
            case "purple" -> ChatColor.LIGHT_PURPLE;
            case "aqua" -> ChatColor.AQUA;
            case "white" -> ChatColor.WHITE;
            case "black" -> ChatColor.BLACK;
            case "gray", "grey" -> ChatColor.GRAY;
            case "gold" -> ChatColor.GOLD;
            case "orange" -> ChatColor.GOLD;
            case "pink" -> ChatColor.LIGHT_PURPLE;
            default -> null;
        };
    }
}