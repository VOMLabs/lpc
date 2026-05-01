package com.vomlabs.lpcmmx.database;

import com.vomlabs.lpcmmx.Main;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {

    private final Main plugin;
    private Connection connection;
    private String storageType;
    private final ExecutorService asyncExecutor;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        this.storageType = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        this.asyncExecutor = Executors.newFixedThreadPool(2);
        initDatabase();
    }

    private void initDatabase() {
        switch (storageType) {
            case "h2":
                initH2();
                break;
            case "sqlite":
                initSQLite();
                break;
            case "mongodb":
                initMongoDB();
                break;
            case "mysql":
                initMySQL();
                break;
            case "mariadb":
                initMariaDB();
                break;
            case "json":
                initJSON();
                break;
            case "yaml":
            default:
                plugin.getLogger().info("Using YAML storage (mutes.yml)");
                return;
        }
        createTables();
    }

    private void initH2() {
        try {
            Class.forName("org.h2.Driver");
            String path = new File(plugin.getDataFolder(), "database.h2").getAbsolutePath();
            connection = DriverManager.getConnection("jdbc:h2:" + path, "sa", "");
            plugin.getLogger().info("Connected to H2 database");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to H2: " + e.getMessage());
        }
    }

    private void initSQLite() {
        try {
            Class.forName("org.sqlite.JDBC");
            String path = new File(plugin.getDataFolder(), "database.db").getAbsolutePath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            plugin.getLogger().info("Connected to SQLite database");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to SQLite: " + e.getMessage());
        }
    }

    private void initMongoDB() {
        plugin.getLogger().info("MongoDB support requires additional setup - using YAML fallback");
    }

    private void initMySQL() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("storage.mysql");
        if (section == null) {
            plugin.getLogger().warning("MySQL configuration not found!");
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String host = section.getString("host", "localhost");
            int port = section.getInt("port", 3306);
            String database = section.getString("database", "lpc");
            String username = section.getString("username", "root");
            String password = section.getString("password", "");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Connected to MySQL database");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to MySQL: " + e.getMessage());
        }
    }

    private void initMariaDB() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("storage.mariadb");
        if (section == null) {
            plugin.getLogger().warning("MariaDB configuration not found!");
            return;
        }
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            String host = section.getString("host", "localhost");
            int port = section.getInt("port", 3306);
            String database = section.getString("database", "lpc");
            String username = section.getString("username", "root");
            String password = section.getString("password", "");
            String url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Connected to MariaDB database");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to MariaDB: " + e.getMessage());
        }
    }

    private void initJSON() {
        plugin.getLogger().info("JSON storage initialized (mutes.json)");
    }

    private void createTables() {
        if (connection == null) return;
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS mutes (" +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "muted_uuid VARCHAR(36) NOT NULL, " +
                    "PRIMARY KEY (player_uuid, muted_uuid))");
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
        }
    }

    public CompletableFuture<Void> saveMutesAsync(Map<UUID, Set<UUID>> mutedPlayers) {
        return CompletableFuture.runAsync(() -> saveMutes(mutedPlayers), asyncExecutor);
    }

    public void saveMutes(Map<UUID, Set<UUID>> mutedPlayers) {
        if (connection == null) {
            return;
        }

        try {
            PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM mutes WHERE player_uuid = ?");
            PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO mutes (player_uuid, muted_uuid) VALUES (?, ?)");

            for (UUID playerUUID : mutedPlayers.keySet()) {
                deleteStmt.setString(1, playerUUID.toString());
                deleteStmt.executeUpdate();

                for (UUID mutedUUID : mutedPlayers.get(playerUUID)) {
                    insertStmt.setString(1, playerUUID.toString());
                    insertStmt.setString(2, mutedUUID.toString());
                    insertStmt.executeUpdate();
                }
            }

            deleteStmt.close();
            insertStmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save mutes to database: " + e.getMessage());
        }
    }

    public CompletableFuture<Map<UUID, Set<UUID>>> loadMutesAsync() {
        return CompletableFuture.supplyAsync(this::loadMutes, asyncExecutor);
    }

    public Map<UUID, Set<UUID>> loadMutes() {
        Map<UUID, Set<UUID>> mutedPlayers = new HashMap<>();

        if (connection == null) {
            return mutedPlayers;
        }

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM mutes");
            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                UUID mutedUUID = UUID.fromString(rs.getString("muted_uuid"));
                mutedPlayers.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(mutedUUID);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load mutes from database: " + e.getMessage());
        }

        return mutedPlayers;
    }

    public CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(this::close, asyncExecutor);
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
        }
        asyncExecutor.shutdown();
    }
}
