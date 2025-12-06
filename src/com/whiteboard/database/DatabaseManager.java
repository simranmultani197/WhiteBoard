package com.whiteboard.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connections using JDBC.
 * Provides connection pooling and initialization.
 */
public class DatabaseManager {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/whiteboard_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password"; // TODO: Load from config file
    
    public static Connection getConnection() throws SQLException {
        // TODO: Implement connection retrieval
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    public static void initializeDatabase() throws SQLException {
        // TODO: Create tables if they don't exist
    }
    
    public static void closeConnection(Connection conn) throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}

