package com.vomlabs.lpcmmx.filter;

import com.vomlabs.lpcmmx.Main;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ChatFilter {

    private final Main plugin;
    private final List<String> filteredWords;
    private final String replacement;
    private final boolean enabled;
    private final boolean filterItemNames;
    private final Map<UUID, ChatHistory> playerChatHistory;
    private final int minTimeBetweenMessages;
    private final int maxMessagesPerWindow;
    private final int timeWindowSeconds;
    private final boolean antiSpamEnabled;

    private static class ChatHistory {
        final List<Long> messageTimes;
        ChatHistory() {
            this.messageTimes = new ArrayList<>();
        }
    }

    public ChatFilter(Main plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("filter.swear.enabled", true);
        this.filterItemNames = plugin.getConfig().getBoolean("filter.swear.filter-item-names", true);
        this.replacement = plugin.getConfig().getString("filter.swear.replacement", "*");
        this.filteredWords = new ArrayList<>();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("filter.swear.words");
        if (section != null) {
            this.filteredWords.addAll(section.getStringList("filter.swear.words"));
        } else {
            this.filteredWords = plugin.getConfig().getStringList("filter.swear.words");
        }

        if (this.filteredWords.isEmpty()) {
            this.filteredWords = new ArrayList<>(Arrays.asList("shit", "fuck", "damn", "bitch", "asshole", "bastard", "cunt", "dick", "piss", "slut", "whore"));
        }

        this.antiSpamEnabled = plugin.getConfig().getBoolean("filter.anti-spam.enabled", true);
        this.minTimeBetweenMessages = plugin.getConfig().getInt("filter.anti-spam.min-time-between-messages", 500);
        this.maxMessagesPerWindow = plugin.getConfig().getInt("filter.anti-spam.max-messages-per-window", 5);
        this.timeWindowSeconds = plugin.getConfig().getInt("filter.anti-spam.time-window-seconds", 5);

        this.playerChatHistory = new HashMap<>();
    }

    public String filterSwearWords(String message) {
        if (!enabled || message == null) return message;

        String filtered = message;
        for (String word : filteredWords) {
            if (word == null || word.isEmpty()) continue;
            String regex = "(?i)\\b" + word + "\\b";
            filtered = filtered.replaceAll(regex, replacement.repeat(word.length()));
        }
        return filtered;
    }

    public String filterItemName(String itemName) {
        if (!enabled || !filterItemNames || itemName == null) return itemName;
        return filterSwearWords(itemName);
    }

    public boolean isSpamming(UUID playerUUID) {
        if (!antiSpamEnabled) return false;

        long now = System.currentTimeMillis();
        ChatHistory history = playerChatHistory.computeIfAbsent(playerUUID, k -> new ChatHistory());

        history.messageTimes.removeIf(time -> (now - time) > (timeWindowSeconds * 1000L));

        if (!history.messageTimes.isEmpty()) {
            long timeSinceLast = now - history.messageTimes.get(history.messageTimes.size() - 1);
            if (timeSinceLast < minTimeBetweenMessages) {
                return true;
            }
        }

        history.messageTimes.add(now);

        return history.messageTimes.size() > maxMessagesPerWindow;
    }

    public void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        playerChatHistory.entrySet().removeIf(entry ->
                entry.getValue().messageTimes.stream()
                        .noneMatch(time -> (now - time) <= (timeWindowSeconds * 1000L))
        );
    }

    public boolean isSwearFilterEnabled() {
        return enabled;
    }

    public boolean isAntiSpamEnabled() {
        return antiSpamEnabled;
    }
}
