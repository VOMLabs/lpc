package com.vomlabs.lpcmmx.suspend;

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

public class ChatSuspendManager {

    private final Main plugin;
    private final Map<UUID, SuspensionInfo> suspendedPlayers;
    private final File suspendFile;
    private FileConfiguration suspendConfig;
    private final DatabaseManager databaseManager;

    public ChatSuspendManager(Main plugin) {
        this.plugin = plugin;
        this.suspendedPlayers = new HashMap<>();
        this.suspendFile = new File(plugin.getDataFolder(), "suspensions.yml");
        this.databaseManager = new DatabaseManager(plugin);
        loadSuspensions();
    }

    public void suspendPlayer(Player suspender, UUID targetUUID, long durationMillis, String reason) {
        long endTime = System.currentTimeMillis() + durationMillis;
        SuspensionInfo info = new SuspensionInfo(endTime, reason,
                suspender != null ? suspender.getUniqueId() : null);
        suspendedPlayers.put(targetUUID, info);
        saveSuspensionsAsync();
    }

    public void unsuspendPlayer(UUID targetUUID) {
        suspendedPlayers.remove(targetUUID);
        saveSuspensionsAsync();
    }

    public boolean isSuspended(UUID playerUUID) {
        SuspensionInfo info = suspendedPlayers.get(playerUUID);
        if (info == null) return false;
        if (System.currentTimeMillis() > info.endTime) {
            suspendedPlayers.remove(playerUUID);
            saveSuspensionsAsync();
            return false;
        }
        return true;
    }

    public SuspensionInfo getSuspensionInfo(UUID playerUUID) {
        SuspensionInfo info = suspendedPlayers.get(playerUUID);
        if (info == null) return null;
        if (System.currentTimeMillis() > info.endTime) {
            suspendedPlayers.remove(playerUUID);
            saveSuspensionsAsync();
            return null;
        }
        return info;
    }

    private void loadSuspensions() {
        String storageType = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        if (!storageType.equals("yaml")) {
            databaseManager.loadSuspensionsAsync().thenAccept(suspensions -> {
                suspendedPlayers.putAll(suspensions);
                plugin.getLogger().info("Loaded " + suspensions.size() + " chat suspensions");
            }).exceptionally(e -> {
                plugin.getLogger().severe("Failed to load suspensions: " + e.getMessage());
                return null;
            });
            return;
        }

        if (!suspendFile.exists()) return;

        suspendConfig = YamlConfiguration.loadConfiguration(suspendFile);
        for (String key : suspendConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(key);
            long endTime = suspendConfig.getLong(key + ".end-time");
            String reason = suspendConfig.getString(key + ".reason", "No reason");
            String suspenderStr = suspendConfig.getString(key + ".suspended-by");
            UUID suspenderUUID = suspenderStr != null ? UUID.fromString(suspenderStr) : null;
            if (System.currentTimeMillis() < endTime) {
                suspendedPlayers.put(playerUUID, new SuspensionInfo(endTime, reason, suspenderUUID));
            }
        }
    }

    private void saveSuspensionsAsync() {
        String storageType = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        if (!storageType.equals("yaml")) {
            databaseManager.saveSuspensionsAsync(suspendedPlayers).exceptionally(e -> {
                plugin.getLogger().severe("Failed to save suspensions: " + e.getMessage());
                return null;
            });
            return;
        }

        if (suspendConfig == null) suspendConfig = new YamlConfiguration();

        for (UUID playerUUID : suspendedPlayers.keySet()) {
            SuspensionInfo info = suspendedPlayers.get(playerUUID);
            String key = playerUUID.toString();
            suspendConfig.set(key + ".end-time", info.endTime);
            suspendConfig.set(key + ".reason", info.reason);
            if (info.suspendedBy != null) {
                suspendConfig.set(key + ".suspended-by", info.suspendedBy.toString());
            }
        }

        try {
            suspendConfig.save(suspendFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save suspensions.yml: " + e.getMessage());
        }
    }

    public void close() {
        databaseManager.close();
    }

    public static class SuspensionInfo {
        public final long endTime;
        public final String reason;
        public final UUID suspendedBy;

        public SuspensionInfo(long endTime, String reason, UUID suspendedBy) {
            this.endTime = endTime;
            this.reason = reason;
            this.suspendedBy = suspendedBy;
        }
    }
}
