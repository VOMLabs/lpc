package com.vomlabs.lpcmmx.discord;

import com.vomlabs.lpcmmx.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Discord webhook integration for cross-platform chat mirroring.
 * Supports DiscordSRV if available, otherwise uses direct webhooks.
 */
public class DiscordWebhook {

    private final Main plugin;
    private String webhookUrl;
    private boolean enabled;
    private boolean useDiscordSRV;
    private Object discordSRVAPI;

    public DiscordWebhook(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        this.enabled = plugin.getConfig().getBoolean("discord.enabled", false);
        this.webhookUrl = plugin.getConfig().getString("discord.webhook-url", "");
        this.useDiscordSRV = plugin.getConfig().getBoolean("discord.use-discordsrv", true);

        if (enabled && useDiscordSRV) {
            try {
                discordSRVAPI = Class.forName("github.scarsz.discordsrv.DiscordSRV")
                        .getMethod("getPlugin")
                        .invoke(null);
                plugin.getLogger().info("DiscordSRV detected - using DiscordSRV for chat mirroring");
            } catch (Exception e) {
                plugin.getLogger().info("DiscordSRV not found - using direct webhooks");
                useDiscordSRV = false;
            }
        }
    }

    public boolean isEnabled() {
        return enabled && (!webhookUrl.isEmpty() || useDiscordSRV);
    }

    public void sendChatMessage(Component message, String playerName, @Nullable String avatarUrl) {
        if (!enabled) return;

        CompletableFuture.runAsync(() -> {
            try {
                String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);

                if (useDiscordSRV && discordSRVAPI != null) {
                    sendViaDiscordSRV(plainMessage, playerName, avatarUrl);
                } else {
                    sendViaWebhook(plainMessage, playerName, avatarUrl);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord message: " + e.getMessage());
            }
        });
    }

    private void sendViaDiscordSRV(String message, String playerName, String avatarUrl) {
        try {
            Class<?> channelClass = Class.forName("github.scarsz.discordsrv.dependencies.jda.core.entities.TextChannel");
            Object channel = discordSRVAPI.getClass().getMethod("getMainTextChannel")
                    .invoke(discordSRVAPI);

            if (channel != null) {
                channel.getClass().getMethod("sendMessage", String.class)
                        .invoke(channel, "**" + playerName + "**: " + message);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send via DiscordSRV: " + e.getMessage());
        }
    }

    private void sendViaWebhook(String message, String playerName, String avatarUrl) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        try {
            String jsonPayload = "{"
                    + "\"content\": \"" + escapeJson(message) + "\","
                    + "\"username\": \"" + escapeJson(playerName) + "\""
                    + (avatarUrl != null ? ",\"avatar_url\": \"" + escapeJson(avatarUrl) + "\"" : "")
                    + "}";

            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 204 && responseCode != 200) {
                plugin.getLogger().warning("Discord webhook returned code: " + responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send via webhook: " + e.getMessage());
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public void reload() {
        loadConfig();
    }
}
