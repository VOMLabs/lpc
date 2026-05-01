package com.vomlabs.lpcmmx.emoji;

import com.vomlabs.lpcmmx.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiManager {

    private final Main plugin;
    private final Map<String, EmojiData> customEmojis;
    private final boolean enabled;
    private final Pattern emojiPattern;
    private final Pattern customEmojiPattern;

    // Common Unicode emojis with their replacements
    private final Map<String, String> unicodeEmojis;

    public EmojiManager(Main plugin) {
        this.plugin = plugin;
        this.customEmojis = new HashMap<>();
        this.enabled = plugin.getConfig().getBoolean("emojis.enabled", true);

        // Load custom emojis from config
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("emojis.custom");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String emoji = section.getString(key + ".emoji", "");
                String hoverText = section.getString(key + ".hover", key);
                boolean requiresPermission = section.getBoolean(key + ".require-permission", false);
                customEmojis.put(key, new EmojiData(emoji, hoverText, requiresPermission));
            }
        }

        // Regex patterns
        emojiPattern = Pattern.compile(":([a-zA-Z0-9_]+):");
        customEmojiPattern = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

        // Initialize Unicode emojis
        unicodeEmojis = new HashMap<>();
        unicodeEmojis.put(":smile:", "😊");
        unicodeEmojis.put(":laugh:", "😂");
        unicodeEmojis.put(":wink:", "😉");
        unicodeEmojis.put(":heart:", "❤");
        unicodeEmojis.put(":thumbsup:", "👍");
        unicodeEmojis.put(":thumbsdown:", "👎");
        unicodeEmojis.put(":fire:", "🔥");
        unicodeEmojis.put(":check:", "✅");
        unicodeEmojis.put(":x:", "❌");
        unicodeEmojis.put(":star:", "⭐");
        unicodeEmojis.put(":warning:", "⚠");
        unicodeEmojis.put(":info:", "ℹ");
        unicodeEmojis.put(":gear:", "⚙");
        unicodeEmojis.put(":clock:", "🕐");
        unicodeEmojis.put(":mail:", "📧");
        unicodeEmojis.put(":phone:", "📱");
        unicodeEmojis.put(":globe:", "🌐");
        unicodeEmojis.put(":rocket:", "🚀");
        unicodeEmojis.put(":rainbow:", "🌈");
        unicodeEmojis.put(":skull:", "💀");
        unicodeEmojis.put(":ghost:", "👻");
        unicodeEmojis.put(":alien:", "👽");
        unicodeEmojis.put(":robot:", "🤖");
    }

    public Component processMessage(String message, Player player) {
        if (!enabled) return null;

        java.util.List<Component> components = new java.util.ArrayList<>();
        int lastEnd = 0;

        // Process both :emoji: and {emoji} patterns
        java.util.regex.Matcher matcher1 = emojiPattern.matcher(message);
        java.util.regex.Matcher matcher2 = customEmojiPattern.matcher(message);

        // Simple replacement for Unicode emojis
        String processed = message;
        for (Map.Entry<String, String> entry : unicodeEmojis.entrySet()) {
            if (player == null || player.hasPermission("lpc.emoji")) {
                processed = processed.replace(entry.getKey(), entry.getValue());
            }
        }

        // Process custom emojis with {name} pattern
        java.util.regex.Matcher customMatcher = customEmojiPattern.matcher(processed);
        while (customMatcher.find()) {
            String emojiName = customMatcher.group(1);
            EmojiData data = customEmojis.get(emojiName);

            if (data != null) {
                if (player == null || !data.requiresPermission ||
                        (player.hasPermission("lpc.emoji.custom." + emojiName))) {
                    // Add text before emoji
                    if (customMatcher.start() > lastEnd) {
                        components.add(Component.text(processed.substring(lastEnd, customMatcher.start())));
                    }

                    Component emojiComponent = Component.text(data.emoji)
                            .hoverEvent(HoverEvent.showText(Component.text(data.hoverText)))
                            .color(TextColor.color(0xFFD700));

                    components.add(emojiComponent);
                    lastEnd = customMatcher.end();
                }
            }
        }

        // Add remaining text
        if (lastEnd < processed.length()) {
            components.add(Component.text(processed.substring(lastEnd)));
        }

        if (components.isEmpty()) return null;

        return Component.empty().children(components);
    }

    public boolean hasCustomEmoji(String name) {
        return customEmojis.containsKey(name);
    }

    public EmojiData getEmojiData(String name) {
        return customEmojis.get(name);
    }

    public static class EmojiData {
        public final String emoji;
        public final String hoverText;
        public final boolean requiresPermission;

        public EmojiData(String emoji, String hoverText, boolean requiresPermission) {
            this.emoji = emoji;
            this.hoverText = hoverText;
            this.requiresPermission = requiresPermission;
        }
    }
}
