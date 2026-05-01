package com.vomlabs.lpcmmx.report;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.discord.DiscordWebhook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportManager {

    private final Main plugin;
    private final Map<UUID, List<Report>> playerReports;
    private final File reportFile;
    private FileConfiguration reportConfig;
    private final boolean discordEnabled;
    private final String discordWebhookUrl;

    public ReportManager(Main plugin) {
        this.plugin = plugin;
        this.playerReports = new HashMap<>();
        this.reportFile = new File(plugin.getDataFolder(), "reports.yml");
        this.discordEnabled = plugin.getConfig().getBoolean("reporting.discord.enabled", true);
        this.discordWebhookUrl = plugin.getConfig().getString("reporting.discord.webhook-url", "");

        loadReports();
    }

    public boolean reportPlayer(Player reporter, UUID targetUUID, String reason) {
        if (reporter != null && !reporter.hasPermission("lpc.report")) {
            reporter.sendMessage(plugin.getMessageManager().getComponent("report.no-permission"));
            return false;
        }

        Report report = new Report(
                reporter != null ? reporter.getUniqueId() : null,
                targetUUID,
                reason,
                System.currentTimeMillis()
        );

        playerReports.computeIfAbsent(targetUUID, k -> new ArrayList<>()).add(report);
        saveReportsAsync();

        // Send to Discord
        if (discordEnabled && !discordWebhookUrl.isEmpty()) {
            sendToDiscord(report);
        }

        if (reporter != null) {
            reporter.sendMessage(plugin.getMessageManager().getComponent("report.submitted",
                    "player", Bukkit.getOfflinePlayer(targetUUID).getName() != null ?
                            Bukkit.getOfflinePlayer(targetUUID).getName() : "Unknown"));
        }

        // Notify staff with permission
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("lpc.report.staff")) {
                player.sendMessage(plugin.getMessageManager().getComponent("report.notify-staff",
                        "reporter", reporter != null ? reporter.getName() : "Console",
                        "player", Bukkit.getOfflinePlayer(targetUUID).getName() != null ?
                                Bukkit.getOfflinePlayer(targetUUID).getName() : "Unknown",
                        "reason", reason));
            }
        }

        return true;
    }

    private void sendToDiscord(Report report) {
        new Thread(() -> {
            try {
                String playerName = "Unknown";
                String reporterName = "Console";

                if (report.targetUUID != null) {
                    org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(report.targetUUID);
                    if (target.getName() != null) playerName = target.getName();
                }

                if (report.reporterUUID != null) {
                    org.bukkit.OfflinePlayer reporter = Bukkit.getOfflinePlayer(report.reporterUUID);
                    if (reporter.getName() != null) reporterName = reporter.getName();
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault());
                String time = formatter.format(Instant.ofEpochMilli(report.timestamp));

                String jsonPayload = String.format(
                        "{\"embeds\":[{\"title\":\"Chat Report\",\"color\":16711680,\"fields\":[" +
                                "{\"name\":\"Reported Player\",\"value\":\"%s\",\"inline\":true}," +
                                "{\"name\":\"Reporter\",\"value\":\"%s\",\"inline\":true}," +
                                "{\"name\":\"Reason\",\"value\":\"%s\"}," +
                                "{\"name\":\"Time\",\"value\":\"%s\"}]}]}",
                        playerName, reporterName, report.reason, time
                );

                java.net.URL url = new java.net.URL(discordWebhookUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                if (code != 204 && code != 200) {
                    plugin.getLogger().warning("Failed to send report to Discord: HTTP " + code);
                }

                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().severe("Error sending report to Discord: " + e.getMessage());
            }
        }).start();
    }

    public List<Report> getReports(UUID playerUUID) {
        return playerReports.getOrDefault(playerUUID, new ArrayList<>());
    }

    public boolean clearReports(UUID playerUUID) {
        if (playerReports.remove(playerUUID) != null) {
            saveReportsAsync();
            return true;
        }
        return false;
    }

    private void loadReports() {
        if (!reportFile.exists()) return;

        reportConfig = YamlConfiguration.loadConfiguration(reportFile);
        for (String key : reportConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(key);
            List<Map<?, ?>> reportsList = reportConfig.getMapList(key);
            List<Report> reports = new ArrayList<>();

            for (Map<?, ?> map : reportsList) {
                UUID reporterUUID = map.get("reporter") != null ?
                        UUID.fromString(map.get("reporter").toString()) : null;
                String reason = map.get("reason").toString();
                long timestamp = Long.parseLong(map.get("timestamp").toString());

                reports.add(new Report(reporterUUID, playerUUID, reason, timestamp));
            }

            if (!reports.isEmpty()) {
                playerReports.put(playerUUID, reports);
            }
        }
    }

    private void saveReportsAsync() {
        if (reportConfig == null) reportConfig = new YamlConfiguration();

        for (UUID playerUUID : playerReports.keySet()) {
            List<Map<String, Object>> reportsList = new ArrayList<>();
            for (Report report : playerReports.get(playerUUID)) {
                Map<String, Object> map = new HashMap<>();
                if (report.reporterUUID != null) map.put("reporter", report.reporterUUID.toString());
                map.put("reason", report.reason);
                map.put("timestamp", report.timestamp);
                reportsList.add(map);
            }
            reportConfig.set(playerUUID.toString(), reportsList);
        }

        new Thread(() -> {
            try {
                reportConfig.save(reportFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save reports.yml: " + e.getMessage());
            }
        }).start();
    }

    public static class Report {
        public final UUID reporterUUID;
        public final UUID targetUUID;
        public final String reason;
        public final long timestamp;

        public Report(UUID reporterUUID, UUID targetUUID, String reason, long timestamp) {
            this.reporterUUID = reporterUUID;
            this.targetUUID = targetUUID;
            this.reason = reason;
            this.timestamp = timestamp;
        }
    }
}
