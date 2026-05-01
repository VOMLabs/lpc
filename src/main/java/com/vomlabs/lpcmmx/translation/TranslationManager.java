package com.vomlabs.lpcmmx.translation;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.database.DatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TranslationManager {

    private final Main plugin;
    private final Map<UUID, TranslationSettings> playerSettings;
    private final File translationFile;
    private FileConfiguration translationConfig;
    private final DatabaseManager databaseManager;

    public TranslationManager(Main plugin) {
        this.plugin = plugin;
        this.playerSettings = new HashMap<>();
        this.translationFile = new File(plugin.getDataFolder(), "translations.yml");
        this.databaseManager = new DatabaseManager(plugin);
        loadSettings();
    }

    public void toggleTranslation(UUID playerUUID) {
        TranslationSettings settings = playerSettings.getOrDefault(playerUUID, new TranslationSettings());
        settings.enabled = !settings.enabled;
        playerSettings.put(playerUUID, settings);
        saveSettingsAsync();
    }

    public boolean isTranslationEnabled(UUID playerUUID) {
        TranslationSettings settings = playerSettings.get(playerUUID);
        return settings != null && settings.enabled;
    }

    public void setLanguage(UUID playerUUID, String language) {
        TranslationSettings settings = playerSettings.getOrDefault(playerUUID, new TranslationSettings());
        settings.language = language;
        settings.enabled = true;
        playerSettings.put(playerUUID, settings);
        saveSettingsAsync();
    }

    public String getLanguage(UUID playerUUID) {
        TranslationSettings settings = playerSettings.get(playerUUID);
        return settings != null ? settings.language :
                plugin.getConfig().getString("translation.default-language", "en");
    }

    public CompletableFuture<String> translate(String message, String targetLanguage) {
        return CompletableFuture.supplyAsync(() -> {
            if (!plugin.getConfig().getBoolean("translation.enabled", false)) {
                return message;
            }
            // TODO: Integrate with translation API (Google, DeepL, etc.)
            // For now, return a placeholder indicating translation would happen
            return "[Translated:" + targetLanguage + "] " + message;
        });
    }

    private void loadSettings() {
        String storageType = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        if (!storageType.equals("yaml")) {
            databaseManager.loadTranslationSettingsAsync().thenAccept(settings -> {
                playerSettings.putAll(settings);
                plugin.getLogger().info("Loaded " + settings.size() + " translation settings");
            }).exceptionally(e -> {
                plugin.getLogger().severe("Failed to load translations: " + e.getMessage());
                return null;
            });
            return;
        }

        if (!translationFile.exists()) return;

        translationConfig = YamlConfiguration.loadConfiguration(translationFile);
        for (String key : translationConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(key);
            boolean enabled = translationConfig.getBoolean(key + ".enabled", false);
            String language = translationConfig.getString(key + ".language", "en");
            playerSettings.put(playerUUID, new TranslationSettings(enabled, language));
        }
    }

    private void saveSettingsAsync() {
        String storageType = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        if (!storageType.equals("yaml")) {
            databaseManager.saveTranslationSettingsAsync(playerSettings).exceptionally(e -> {
                plugin.getLogger().severe("Failed to save translations: " + e.getMessage());
                return null;
            });
            return;
        }

        if (translationConfig == null) translationConfig = new YamlConfiguration();

        for (UUID playerUUID : playerSettings.keySet()) {
            TranslationSettings settings = playerSettings.get(playerUUID);
            String key = playerUUID.toString();
            translationConfig.set(key + ".enabled", settings.enabled);
            translationConfig.set(key + ".language", settings.language);
        }

        try {
            translationConfig.save(translationFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save translations.yml: " + e.getMessage());
        }
    }

    public void close() {
        databaseManager.close();
    }

    public static class TranslationSettings {
        public boolean enabled;
        public String language;

        public TranslationSettings() {
            this(false, "en");
        }

        public TranslationSettings(boolean enabled, String language) {
            this.enabled = enabled;
            this.language = language;
        }
    }
}
