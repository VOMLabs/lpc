package com.vomlabs.lpcmmx.mute;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.database.DatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MuteManager {

    private final Main plugin;
    private final Map<UUID, Set<UUID>> mutedPlayers;
    private final File muteFile;
    private FileConfiguration muteConfig;
    private final DatabaseManager databaseManager;

    public MuteManager(Main plugin) {
        this.plugin = plugin;
        this.mutedPlayers = new HashMap<>();
        this.muteFile = new File(plugin.getDataFolder(), "mutes.yml");
        this.databaseManager = new DatabaseManager(plugin);
        loadMutes();
    }

    public void mutePlayer(Player player, UUID targetUUID) {
        mutedPlayers.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(targetUUID);
        saveMutesAsync();
    }

    public void unmutePlayer(Player player, UUID targetUUID) {
        Set<UUID> muted = mutedPlayers.get(player.getUniqueId());
        if (muted != null) {
            muted.remove(targetUUID);
            if (muted.isEmpty()) {
                mutedPlayers.remove(player.getUniqueId());
            }
            saveMutesAsync();
        }
    }

    public void toggleMute(Player player, UUID targetUUID) {
        if (isMuted(player, targetUUID)) {
            unmutePlayer(player, targetUUID);
        } else {
            mutePlayer(player, targetUUID);
        }
    }

    public boolean isMuted(Player player, UUID targetUUID) {
        Set<UUID> muted = mutedPlayers.get(player.getUniqueId());
        return muted != null && muted.contains(targetUUID);
    }

    public boolean isIgnored(Player target, UUID senderUUID) {
        Set<UUID> ignored = mutedPlayers.get(target.getUniqueId());
        return ignored != null && ignored.contains(senderUUID);
    }

    public Set<UUID> getMutedPlayers(Player player) {
        return mutedPlayers.getOrDefault(player.getUniqueId(), Collections.emptySet());
    }

    public void clearMutes(Player player) {
        mutedPlayers.remove(player.getUniqueId());
        saveMutesAsync();
    }

    public void loadMutes() {
        String storageType = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        if (!storageType.equals("yaml")) {
            databaseManager.loadMutesAsync().thenAccept(mutes -> {
                mutedPlayers.putAll(mutes);
                plugin.getLogger().info("Loaded " + mutes.size() + " mute entries from database");
            }).exceptionally(e -> {
                plugin.getLogger().severe("Failed to load mutes: " + e.getMessage());
                return null;
            });
            return;
        }

        if (!muteFile.exists()) {
            return;
        }

        muteConfig = YamlConfiguration.loadConfiguration(muteFile);
        for (String key : muteConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(key);
            List<String> muted = muteConfig.getStringList(key);
            Set<UUID> mutedSet = new HashSet<>();
            for (String uuid : muted) {
                mutedSet.add(UUID.fromString(uuid));
            }
            if (!mutedSet.isEmpty()) {
                mutedPlayers.put(playerUUID, mutedSet);
            }
        }
    }

    public void saveMutesAsync() {
        String storageType = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        if (!storageType.equals("yaml")) {
            databaseManager.saveMutesAsync(mutedPlayers).exceptionally(e -> {
                plugin.getLogger().severe("Failed to save mutes: " + e.getMessage());
                return null;
            });
            return;
        }

        if (muteConfig == null) {
            muteConfig = new YamlConfiguration();
        }

        for (UUID playerUUID : mutedPlayers.keySet()) {
            List<String> muted = new ArrayList<>();
            for (UUID mutedUUID : mutedPlayers.get(playerUUID)) {
                muted.add(mutedUUID.toString());
            }
            muteConfig.set(playerUUID.toString(), muted);
        }

        try {
            muteConfig.save(muteFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mutes.yml: " + e.getMessage());
        }
    }

    public void close() {
        databaseManager.close();
    }
}
