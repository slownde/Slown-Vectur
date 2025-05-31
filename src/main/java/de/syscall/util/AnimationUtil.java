package de.syscall.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimationUtil {

    private static final Pattern RAINBOW_PATTERN = Pattern.compile("\\{rainbow}(.*?)\\{/rainbow}");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("\\{gradient:(\\w+)-(\\w+)}(.*?)\\{/gradient}");
    private static final Pattern WAVE_PATTERN = Pattern.compile("\\{wave}(.*?)\\{/wave}");
    private static final Pattern GLOW_PATTERN = Pattern.compile("\\{glow}(.*?)\\{/glow}");
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static Component processAnimations(String text) {
        if (text == null) return Component.empty();

        Component result = Component.text(text);

        result = processRainbow(result);
        result = processGradient(result);
        result = processWave(result);
        result = processGlow(result);

        return result;
    }

    public static String processAnimationsLegacy(String text) {
        if (text == null) return "";

        text = processRainbowLegacy(text);
        text = processGradientLegacy(text);
        text = processWaveLegacy(text);
        text = processGlowLegacy(text);

        return text;
    }

    private static Component processRainbow(Component component) {
        String text = LEGACY_SERIALIZER.serialize(component);
        Matcher matcher = RAINBOW_PATTERN.matcher(text);

        while (matcher.find()) {
            String content = matcher.group(1);
            Component rainbow = ColorUtil.rainbow(content);
            String rainbowText = LEGACY_SERIALIZER.serialize(rainbow);
            text = text.replace(matcher.group(0), rainbowText);
        }

        return LEGACY_SERIALIZER.deserialize(text);
    }

    private static Component processGradient(Component component) {
        String text = LEGACY_SERIALIZER.serialize(component);
        Matcher matcher = GRADIENT_PATTERN.matcher(text);

        while (matcher.find()) {
            String startColorName = matcher.group(1);
            String endColorName = matcher.group(2);
            String content = matcher.group(3);

            TextColor startColor = parseColor(startColorName);
            TextColor endColor = parseColor(endColorName);

            if (startColor != null && endColor != null) {
                Component gradient = ColorUtil.gradient(content, startColor, endColor);
                String gradientText = LEGACY_SERIALIZER.serialize(gradient);
                text = text.replace(matcher.group(0), gradientText);
            }
        }

        return LEGACY_SERIALIZER.deserialize(text);
    }

    private static Component processWave(Component component) {
        String text = LEGACY_SERIALIZER.serialize(component);
        Matcher matcher = WAVE_PATTERN.matcher(text);

        while (matcher.find()) {
            String content = matcher.group(1);
            Component wave = createWaveEffect(content);
            String waveText = LEGACY_SERIALIZER.serialize(wave);
            text = text.replace(matcher.group(0), waveText);
        }

        return LEGACY_SERIALIZER.deserialize(text);
    }

    private static Component processGlow(Component component) {
        String text = LEGACY_SERIALIZER.serialize(component);
        Matcher matcher = GLOW_PATTERN.matcher(text);

        while (matcher.find()) {
            String content = matcher.group(1);
            Component glow = createGlowEffect(content);
            String glowText = LEGACY_SERIALIZER.serialize(glow);
            text = text.replace(matcher.group(0), glowText);
        }

        return LEGACY_SERIALIZER.deserialize(text);
    }

    private static String processRainbowLegacy(String text) {
        Matcher matcher = RAINBOW_PATTERN.matcher(text);

        while (matcher.find()) {
            String content = matcher.group(1);
            Component rainbow = ColorUtil.rainbow(content);
            String rainbowText = LEGACY_SERIALIZER.serialize(rainbow);
            text = text.replace(matcher.group(0), rainbowText);
        }

        return text;
    }

    private static String processGradientLegacy(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);

        while (matcher.find()) {
            String startColorName = matcher.group(1);
            String endColorName = matcher.group(2);
            String content = matcher.group(3);

            TextColor startColor = parseColor(startColorName);
            TextColor endColor = parseColor(endColorName);

            if (startColor != null && endColor != null) {
                Component gradient = ColorUtil.gradient(content, startColor, endColor);
                String gradientText = LEGACY_SERIALIZER.serialize(gradient);
                text = text.replace(matcher.group(0), gradientText);
            }
        }

        return text;
    }

    private static String processWaveLegacy(String text) {
        Matcher matcher = WAVE_PATTERN.matcher(text);

        while (matcher.find()) {
            String content = matcher.group(1);
            Component wave = createWaveEffect(content);
            String waveText = LEGACY_SERIALIZER.serialize(wave);
            text = text.replace(matcher.group(0), waveText);
        }

        return text;
    }

    private static String processGlowLegacy(String text) {
        Matcher matcher = GLOW_PATTERN.matcher(text);

        while (matcher.find()) {
            String content = matcher.group(1);
            Component glow = createGlowEffect(content);
            String glowText = LEGACY_SERIALIZER.serialize(glow);
            text = text.replace(matcher.group(0), glowText);
        }

        return text;
    }

    private static Component createWaveEffect(String text) {
        TextColor[] waveColors = {
                TextColor.color(0, 0, 170),     // DARK_BLUE
                TextColor.color(85, 85, 255),   // BLUE
                TextColor.color(85, 255, 255),  // AQUA
                TextColor.color(255, 255, 255), // WHITE
                TextColor.color(85, 255, 255),  // AQUA
                TextColor.color(85, 85, 255)    // BLUE
        };

        Component result = Component.empty();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result = result.append(Component.text(c));
                continue;
            }

            int colorIndex = (i + (int)(System.currentTimeMillis() / 200)) % waveColors.length;
            result = result.append(Component.text(c).color(waveColors[colorIndex]));
        }

        return result;
    }

    private static Component createGlowEffect(String text) {
        TextColor[] glowColors = {
                TextColor.color(255, 255, 255), // WHITE
                TextColor.color(255, 255, 85),  // YELLOW
                TextColor.color(255, 170, 0),   // GOLD
                TextColor.color(255, 255, 85),  // YELLOW
                TextColor.color(255, 255, 255)  // WHITE
        };

        Component result = Component.empty();
        int glowPhase = (int)((System.currentTimeMillis() / 300) % glowColors.length);

        for (char c : text.toCharArray()) {
            if (c == ' ') {
                result = result.append(Component.text(c));
            } else {
                result = result.append(Component.text(c).color(glowColors[glowPhase]));
            }
        }

        return result;
    }

    private static TextColor parseColor(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "red" -> TextColor.color(255, 85, 85);
            case "blue" -> TextColor.color(85, 85, 255);
            case "green" -> TextColor.color(85, 255, 85);
            case "yellow" -> TextColor.color(255, 255, 85);
            case "purple" -> TextColor.color(255, 85, 255);
            case "aqua" -> TextColor.color(85, 255, 255);
            case "white" -> TextColor.color(255, 255, 255);
            case "black" -> TextColor.color(0, 0, 0);
            case "gray", "grey" -> TextColor.color(170, 170, 170);
            case "gold" -> TextColor.color(255, 170, 0);
            case "orange" -> TextColor.color(255, 170, 0);
            case "pink" -> TextColor.color(255, 85, 255);
            default -> null;
        };
    }
}