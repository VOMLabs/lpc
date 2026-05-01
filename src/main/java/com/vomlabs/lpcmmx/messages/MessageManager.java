package com.vomlabs.lpcmmx.messages;

import com.vomlabs.lpcmmx.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MessageManager {

    private final Main plugin;
    private FileConfiguration messagesConfig;
    private final File messagesFile;
    private final Map<String, String> messages;

    public MessageManager(Main plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = new HashMap<>();
        loadMessages();
    }

    private void loadMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        for (String key : messagesConfig.getKeys(true)) {
            if (messagesConfig.isString(key)) {
                messages.put(key, messagesConfig.getString(key));
            }
        }
    }

    public CompletableFuture<Void> loadMessagesAsync() {
        return CompletableFuture.runAsync(this::loadMessages);
    }

    public String getMessage(String path) {
        String prefix = messages.getOrDefault("prefix", "");
        String message = messages.getOrDefault(path, path);
        if (!prefix.isEmpty()) {
            return prefix + " " + message;
        }
        return message;
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return message;
    }

    public Component getComponent(String path) {
        return MiniMessage.miniMessage().deserialize(getMessage(path));
    }

    public Component getComponent(String path, Map<String, String> placeholders) {
        return MiniMessage.miniMessage().deserialize(getMessage(path, placeholders));
    }

    public CompletableFuture<Void> reloadAsync() {
        return CompletableFuture.runAsync(() -> {
            messages.clear();
            loadMessages();
        });
    }

    public void saveDefaultMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }
}
