package com.vomlabs.lpcmmx.logging;

import com.vomlabs.lpcmmx.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chat logging system for server moderation.
 * Logs all chat messages to daily rotating log files.
 */
public class ChatLogger {

    private final Main plugin;
    private final ExecutorService logExecutor;
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter timeFormatter;
    private BufferedWriter currentWriter;
    private String currentDate;
    private File logFile;

    public ChatLogger(Main plugin) {
        this.plugin = plugin;
        this.logExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "LPC-ChatLogger");
            t.setDaemon(true);
            return t;
        });
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        openLogFile();
    }

    private void openLogFile() {
        try {
            if (currentWriter != null) {
                currentWriter.close();
            }

            File logDir = new File(plugin.getDataFolder(), "logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            currentDate = LocalDateTime.now().format(dateFormatter);
            logFile = new File(logDir, "chat-" + currentDate + ".log");

            currentWriter = new BufferedWriter(new FileWriter(logFile, true));

            // Write session start marker
            String sessionStart = String.format("[%s] === Log session started ===%n",
                    LocalDateTime.now().format(timeFormatter));
            currentWriter.write(sessionStart);
            currentWriter.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to open log file: " + e.getMessage());
        }
    }

    public void logMessage(Player player, Component message) {
        if (!plugin.getConfig().getBoolean("logging.enabled", true)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String time = LocalDateTime.now().format(timeFormatter);
                String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);
                String logEntry = String.format("[%s] %s (%s): %s%n",
                        time,
                        player.getName(),
                        player.getUniqueId().toString(),
                        plainMessage);

                // Check if date changed (daily rotation)
                String today = LocalDateTime.now().format(dateFormatter);
                if (!today.equals(currentDate)) {
                    openLogFile();
                }

                if (currentWriter != null) {
                    currentWriter.write(logEntry);
                    currentWriter.flush();
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to write chat log: " + e.getMessage());
            }
        }, logExecutor);
    }

    public void logJoin(Player player) {
        if (!plugin.getConfig().getBoolean("logging.log-connections", true)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String time = LocalDateTime.now().format(timeFormatter);
                String logEntry = String.format("[%s] [JOIN] %s (%s) joined the server%n",
                        time,
                        player.getName(),
                        player.getUniqueId().toString());

                if (currentWriter != null) {
                    currentWriter.write(logEntry);
                    currentWriter.flush();
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to write connection log: " + e.getMessage());
            }
        }, logExecutor);
    }

    public void logQuit(Player player) {
        if (!plugin.getConfig().getBoolean("logging.log-connections", true)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String time = LocalDateTime.now().format(timeFormatter);
                String logEntry = String.format("[%s] [QUIT] %s (%s) left the server%n",
                        time,
                        player.getName(),
                        player.getUniqueId().toString());

                if (currentWriter != null) {
                    currentWriter.write(logEntry);
                    currentWriter.flush();
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to write connection log: " + e.getMessage());
            }
        }, logExecutor);
    }

    public void logCommand(Player player, String command) {
        if (!plugin.getConfig().getBoolean("logging.log-commands", false)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String time = LocalDateTime.now().format(timeFormatter);
                String logEntry = String.format("[%s] [CMD] %s (%s): /%s%n",
                        time,
                        player.getName(),
                        player.getUniqueId().toString(),
                        command);

                if (currentWriter != null) {
                    currentWriter.write(logEntry);
                    currentWriter.flush();
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to write command log: " + e.getMessage());
            }
        }, logExecutor);
    }

    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (currentWriter != null) {
                    currentWriter.write(String.format("[%s] === Log session ended ===%n",
                            LocalDateTime.now().format(timeFormatter)));
                    currentWriter.flush();
                    currentWriter.close();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to close log file: " + e.getMessage());
            }
            logExecutor.shutdown();
        }, logExecutor);
    }
}
