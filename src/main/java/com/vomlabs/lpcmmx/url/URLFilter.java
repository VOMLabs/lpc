package com.vomlabs.lpcmmx.url;

import com.vomlabs.lpcmmx.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLFilter {

    private final Main plugin;
    private final boolean enabled;
    private final Pattern urlPattern;

    public URLFilter(Main plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("url-filter.enabled", true);
        this.urlPattern = Pattern.compile("(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)");
    }

    public Component processMessage(String message) {
        if (!enabled) return null;

        java.util.List<Component> components = new java.util.ArrayList<>();
        Matcher matcher = urlPattern.matcher(message);
        int lastEnd = 0;

        while (matcher.find()) {
            // Add text before URL
            if (matcher.start() > lastEnd) {
                components.add(Component.text(message.substring(lastEnd, matcher.start())));
            }

            String url = matcher.group(1);
            Component link = Component.text(url)
                    .color(TextColor.color(0x3498db))
                    .clickEvent(ClickEvent.openUrl(url))
                    .hoverEvent(Component.text("Click to open: " + url));

            components.add(link);
            lastEnd = matcher.end();
        }

        // Add remaining text
        if (lastEnd < message.length()) {
            components.add(Component.text(message.substring(lastEnd)));
        }

        if (components.isEmpty()) return null;
        return Component.empty().children(components);
    }

    public boolean containsURL(String message) {
        if (!enabled) return false;
        return urlPattern.matcher(message).find();
    }

    public String stripURLs(String message) {
        if (!enabled) return message;
        return urlPattern.matcher(message).replaceAll("[URL]");
    }
}
