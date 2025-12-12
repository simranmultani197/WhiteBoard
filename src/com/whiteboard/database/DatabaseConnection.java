package com.whiteboard.database;

import java.sql.*;

/**
 * Manages database connection as a singleton.
 * Provides connection to MySQL database.
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/whiteboard_db";
    private static final String DB_USER = "whiteboard_user";
    private static final String DB_PASSWORD = "whiteboard_pass";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connected successfully!");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Gets the singleton instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Gets the database connection
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Creates the necessary tables if they don't exist
     */
    private void initializeDatabase() {
        String createSessionsTable = "CREATE TABLE IF NOT EXISTS sessions (" +
                "session_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "session_name VARCHAR(255) UNIQUE NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "INDEX idx_session_name (session_name)" +
                ")";

        String createDrawingsTable = "CREATE TABLE IF NOT EXISTS drawings (" +
                "drawing_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "session_id INT NOT NULL, " +
                "drawing_data TEXT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (session_id) REFERENCES sessions(session_id) ON DELETE CASCADE, " +
                "INDEX idx_session_id (session_id)" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSessionsTable);
            stmt.execute(createDrawingsTable);
            System.out.println("Database tables initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Checks if the database connection is active
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Closes the database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}