package io.github.townyadvanced.flagwar.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class DatabaseManager {
    /** Holds the directory path of the database.*/
    private final Path PATH;

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds the connection to make SQL queries.*/
    private Connection connection;

    /** Holds the DDL creation query of every table and index in the database. */
    private static final List<String> QUERIES = List.of("""
        CREATE TABLE IF NOT EXISTS Battle (
            ContestedTown TEXT PRIMARY KEY NOT NULL,
            Attacker TEXT NOT NULL,
            Defender TEXT NOT NULL,
            HomeX INTEGER NOT NULL,
            HomeZ INTEGER NOT NULL,
            StageStartTime INTEGER NOT NULL,
            CityState BOOLEAN NOT NULL,
            Stage TEXT NOT NULL,
            World TEXT NOT NULL,
            TownBlocks TEXT NOT NULL,
            InitialMayor TEXT NOT NULL
        );
        """,
        """
        CREATE TABLE IF NOT EXISTS BannerPlacer (
            Town TEXT PRIMARY KEY NOT NULL,
            AttackDay INTEGER NOT NULL
        );
        """,
        """
        CREATE TABLE IF NOT EXISTS TrackedBattle (
            ContestedTown TEXT PRIMARY KEY NOT NULL,
            Attacker TEXT NOT NULL,
            Defender TEXT NOT NULL,
            StartTime INTEGER NOT NULL,
            DamageLogs STRING NOT NULL
        """,
        """
        CREATE TABLE IF NOT EXISTS TrackedPlayer (
            IncID TEXT PRIMARY KEY NOT NULL,
            UUID TEXT NOT NULL,
            BattleTown TEXT NOT NULL,
            Affiliation TEXT NOT NULL,
            DamageDealt REAL NOT NULL,
            DamageTaken REAL NOT NULL,
            Kills TEXT NOT NULL,
            Deaths TEXT NOT NULL,
            ConsumedGaps INTEGER NOT NULL,
            ConsumedPots INTEGER NOT NULL,
        );
"""
    );

    /**
     * The constructor of the {@link DatabaseManager}.
     * @param plugin the plugin instance
     */
    public DatabaseManager(JavaPlugin plugin) {
        this.PLUGIN = plugin;
        this.PATH = plugin.getDataFolder().toPath().resolve("BannerWarDatabase.db");
        try {init();} catch (SQLException e) {e.printStackTrace();}
    }

    /** Performs various initialization steps on the {@link DatabaseManager} object, where the directories are created, connection is opened, pragmas are applied and tables are created.*/
    private void init() throws SQLException {
        createDirectories();
        openConnection();
        applyPragmas();
        createTables();
    }

    /**
     * Gets the connection of the database to perform SQL queries and access the database with.
     * @return database connection.
     */
    public Connection getConnection() {
        try
        {
            if (connection == null || connection.isClosed()) {
                openConnection();
            }
            return connection;
        }
        catch (SQLException e) {e.printStackTrace(); return null;}
    }

    /**
     * Closes the connection if it isn't already closed.
     * @throws SQLException
     */
    public void shutdown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Creates the directories required to store the database if they don't exist.
     * Also gets the connection and opens it.
     * @throws SQLException {@link DriverManager#getConnection(String)} is called here.
     */
    private void openConnection() throws SQLException {
        String url = "jdbc:sqlite:" + PATH.toAbsolutePath();
        connection = DriverManager.getConnection(url);
    }

    /**
     * Applies pragmas to ensure optimization and the use of foreign keys.
     * @throws SQLException
     */
    private void applyPragmas() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA synchronous=NORMAL;");
            stmt.execute("PRAGMA foreign_keys=ON;");
        }
    }

    /**
     * Creates all necessary tables for BannerWar.
     * @throws SQLException
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            for (var TQ : QUERIES)
                stmt.execute(TQ);
        }
    }

    /**
     * Creates the required directories.
     */
    private void createDirectories() {
        try {
            Files.createDirectories(PATH.getParent());}
        catch (IOException e) {PLUGIN.getLogger().severe("Unable to create database directory");}
    }
}
