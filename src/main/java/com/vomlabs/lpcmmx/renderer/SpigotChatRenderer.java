package com.vomlabs.lpcmmx.renderer;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.integration.VaultHook;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.track.Track;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpigotChatRenderer {
    private final LuckPerms luckPerms;
    private final Main plugin;
    private final MiniMessage miniMessage;
    private final boolean hasPapi;

    public SpigotChatRenderer(Main plugin) {
        this.luckPerms = LuckPermsProvider.get();
        this.plugin = plugin;
        this.miniMessage = MiniMessage.builder().build();
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        hasPapi = pluginManager.getPlugin("PlaceholderAPI") != null;
    }

    public @NotNull Component render(Player source, String message) {
        final CachedMetaData metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(source);
        final String group = Objects.requireNonNull(metaData.getPrimaryGroup(), "Primary group cannot be null");

        String plainMessage = source.hasPermission("lpc.chatcolor") ? message : stripMiniMessageTags(message);

        String formatKey = "group-formats." + group;
        String format = plugin.getConfig().getString(formatKey);

        if (format == null) {
            ConfigurationSection trackFormatsSection = plugin.getConfig().getConfigurationSection("track-formats");
            if (trackFormatsSection != null) {
                for (String trackName : trackFormatsSection.getKeys(false)) {
                    Track track = this.luckPerms.getTrackManager().getTrack(trackName);
                    if (track == null) continue;
                    if (track.containsGroup(group)) {
                        formatKey = "track-formats." + trackName;
                        format = plugin.getConfig().getString(formatKey);
                        break;
                    }
                }
            }
        }

        if (format == null) {
            format = plugin.getConfig().getString("chat-format");
        }

        if (hasPapi) {
            format = PlaceholderAPI.setPlaceholders(source, format);
        }

        format = format.replace("%prefix%", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("<prefix>", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("%suffix%", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("<suffix>", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("%prefixes%", String.join(" ", metaData.getPrefixes().values()))
                .replace("<prefixes>", String.join(" ", metaData.getPrefixes().values()))
                .replace("%suffixes%", String.join(" ", metaData.getSuffixes().values()))
                .replace("<suffixes>", String.join(" ", metaData.getSuffixes().values()))
                .replace("%world%", source.getWorld().getName())
                .replace("<world>", source.getWorld().getName())
                .replace("%name%", source.getName())
                .replace("<name>", source.getName())
                .replace("%displayname%", source.getDisplayName())
                .replace("<displayname>", source.getDisplayName())
                .replace("%username-color%", metaData.getMetaValue("username-color") != null ? Objects.requireNonNull(metaData.getMetaValue("username-color")) : "")
                .replace("<username-color>", metaData.getMetaValue("username-color") != null ? Objects.requireNonNull(metaData.getMetaValue("username-color")) : "")
                .replace("%message-color%", metaData.getMetaValue("message-color") != null ? Objects.requireNonNull(metaData.getMetaValue("message-color")) : "")
                .replace("<message-color>", metaData.getMetaValue("message-color") != null ? Objects.requireNonNull(metaData.getMetaValue("message-color")) : "")
                .replace("%balance%", VaultHook.hasEconomy() ? String.valueOf(VaultHook.getBalance(source)) : "0")
                .replace("<balance>", VaultHook.hasEconomy() ? String.valueOf(VaultHook.getBalance(source)) : "0")
                .replace("%balance-formatted%", VaultHook.hasEconomy() ? VaultHook.getFormattedBalance(source) : "0")
                .replace("<balance-formatted>", VaultHook.hasEconomy() ? VaultHook.getFormattedBalance(source) : "0")
                .replace("%player-health%", String.valueOf(source.getHealth()))
                .replace("<player-health>", String.valueOf(source.getHealth()))
                .replace("%player-level%", String.valueOf(source.getLevel()))
                .replace("<player-level>", String.valueOf(source.getLevel()))
                .replace("%world-time%", String.valueOf(source.getWorld().getTime()))
                .replace("<world-time>", String.valueOf(source.getWorld().getTime()))
                .replace("%player-uuid%", source.getUniqueId().toString())
                .replace("<player-uuid>", source.getUniqueId().toString())
                .replace("%message%", plainMessage)
                .replace("<message>", plainMessage);

        try {
            return miniMessage.deserialize(format.replace("%%", "%"));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse MiniMessage format for " + source.getName() + ": " + e.getMessage());
            return miniMessage.deserialize(plainMessage);
        }
    }

    private String stripMiniMessageTags(String message) {
        return message.replaceAll("<[^>]*>", "");
    }
}
