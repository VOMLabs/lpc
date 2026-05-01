package com.vomlabs.lpcmmx.format;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.database.DatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerFormatManager {

    private final Main plugin;
    private final Map<UUID, String> playerFormats;
    private final File formatFile;
    private FileConfiguration formatConfig;
    private final DatabaseManager databaseManager;

    public PlayerFormatManager(Main plugin) {
        this.plugin = plugin;
        this.playerFormats = new HashMap<>();
        this.formatFile = new File(plugin.getDataFolder(), "player-formats.yml");
        this.databaseManager = new DatabaseManager(plugin);
        loadFormats();
    }

    public void setPlayerFormat(Player player, String formatName) {
        playerFormats.put(player.getUniqueId(), formatName);
        saveFormatsAsync();
    }

    public void clearPlayerFormat(Player player) {
        playerFormats.remove(player.getUniqueId());
        saveFormatsAsync();
    }

    public String getPlayerFormat(Player player) {
        return playerFormats.get(player.getUniqueId());
    }

    public boolean hasCustomFormat(Player player) {
        return playerFormats.containsKey(player.getUniqueId());
    }

    public String getFormatByName(String formatName) {
        return plugin.getConfig().getString("player-formats." + formatName);
    }

    public boolean formatExists(String formatName) {
        return plugin.getConfig().contains("player-formats." + formatName);
    }

    private void loadFormats() {
        String storageType = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        if (!storageType.equals("yaml")) {
            databaseManager.loadPlayerFormatsAsync().thenAccept(formats -> {
                playerFormats.putAll(formats);
                plugin.getLogger().info("Loaded " + formats.size() + " player format entries from database");
            }).exceptionally(e -> {
                plugin.getLogger().severe("Failed to load player formats: " + e.getMessage());
                return null;
            });
            return;
        }

        if (!formatFile.exists()) {
            return;
        }

        formatConfig = YamlConfiguration.loadConfiguration(formatFile);
        for (String key : formatConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(key);
            String formatName = formatConfig.getString(key);
            if (formatName != null) {
                playerFormats.put(playerUUID, formatName);
            }
        }
    }

    private void saveFormatsAsync() {
        String storageType = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        if (!storageType.equals("yaml")) {
            databaseManager.savePlayerFormatsAsync(playerFormats).exceptionally(e -> {
                plugin.getLogger().severe("Failed to save player formats: " + e.getMessage());
                return null;
            });
            return;
        }

        if (formatConfig == null) {
            formatConfig = new YamlConfiguration();
        }

        for (UUID playerUUID : playerFormats.keySet()) {
            formatConfig.set(playerUUID.toString(), playerFormats.get(playerUUID));
        }

        try {
            formatConfig.save(formatFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player-formats.yml: " + e.getMessage());
        }
    }

    public void close() {
        databaseManager.close();
    }
}
