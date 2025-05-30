package de.syscall.data;

import de.syscall.SlownVectur;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final SlownVectur plugin;
    private Connection connection;

    public DatabaseManager(SlownVectur plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String url = "jdbc:sqlite:" + dataFolder + "/database.db";
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Verbinden zur Datenbank: " + e.getMessage());
        }
    }

    private void createTables() {
        String sql = """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(16) NOT NULL,
                first_join BIGINT NOT NULL,
                last_join BIGINT NOT NULL,
                play_time BIGINT DEFAULT 0,
                coins INTEGER DEFAULT 0,
                bank_coins INTEGER DEFAULT 0
            )
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Erstellen der Tabellen: " + e.getMessage());
        }
    }

    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid, String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM players WHERE uuid = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return new PlayerData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getLong("first_join"),
                            rs.getLong("last_join"),
                            rs.getLong("play_time"),
                            rs.getInt("coins"),
                            rs.getInt("bank_coins")
                    );
                } else {
                    PlayerData data = new PlayerData(uuid, name);
                    savePlayerData(data);
                    return data;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Laden der Spielerdaten: " + e.getMessage());
                return new PlayerData(uuid, name);
            }
        });
    }

    public void savePlayerData(PlayerData data) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String sql = """
                    INSERT OR REPLACE INTO players\s
                    (uuid, name, first_join, last_join, play_time, coins, bank_coins)\s
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                   \s""";

                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.setString(2, data.getName());
                    stmt.setLong(3, data.getFirstJoin());
                    stmt.setLong(4, data.getLastJoin());
                    stmt.setLong(5, data.getPlayTime());
                    stmt.setInt(6, data.getCoins());
                    stmt.setInt(7, data.getBankCoins());
                    stmt.execute();
                } catch (SQLException e) {
                    plugin.getLogger().severe("Fehler beim Speichern der Spielerdaten: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Schließen der Datenbank: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }
}