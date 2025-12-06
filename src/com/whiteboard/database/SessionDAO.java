package com.whiteboard.database;

import java.sql.Connection;
import java.sql.SQLException;
import com.whiteboard.common.model.DrawingEvent;
import java.util.List;

/**
 * Data Access Object for managing whiteboard sessions in the database.
 * Handles saving and loading session data using JDBC.
 */
public class SessionDAO {
    
    private Connection connection;
    
    public SessionDAO(Connection connection) {
        this.connection = connection;
    }
    
    public void saveSession(String sessionName, List<DrawingEvent> events) throws SQLException {
        // TODO: Implement session saving logic
    }
    
    public List<DrawingEvent> loadSession(String sessionName) throws SQLException {
        // TODO: Implement session loading logic
        return null;
    }
    
    public void deleteSession(String sessionName) throws SQLException {
        // TODO: Implement session deletion logic
    }
    
    public List<String> getAllSessions() throws SQLException {
        // TODO: Implement method to retrieve all session names
        return null;
    }
}

