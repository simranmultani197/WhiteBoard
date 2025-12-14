package com.whiteboard.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Session-related database operations.
 * Handles all CRUD operations for sessions and their drawings.
 */
public class SessionDao {

    private final DatabaseConnection dbConnection;

    public SessionDao() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Gets or creates a session ID for the given session name
     * @param sessionName The name of the session
     * @return The session ID, or -1 if error
     */
    private int getOrCreateSessionId(String sessionName) {
        Connection conn = dbConnection.getConnection();

        // First try to get existing session
        String selectSql = "SELECT session_id FROM sessions WHERE session_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, sessionName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("session_id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting session: " + e.getMessage());
        }

        // If session doesn't exist, create it
        String insertSql = "INSERT INTO sessions (session_name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, sessionName);
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error creating session: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Saves a drawing to the database
     * @param sessionName The name of the session
     * @param drawingData The serialized drawing data
     * @return true if save was successful
     */
    public boolean saveDrawing(String sessionName, String drawingData) {
        int sessionId = getOrCreateSessionId(sessionName);
        if (sessionId == -1) {
            return false;
        }

        Connection conn = dbConnection.getConnection();
        String sql = "INSERT INTO drawings (session_id, drawing_data) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            pstmt.setString(2, drawingData);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving drawing: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads all drawings for a specific session
     * @param sessionName The name of the session
     * @return List of drawing data strings
     */
    public List<String> loadSessionDrawings(String sessionName) {
        List<String> drawings = new ArrayList<>();
        Connection conn = dbConnection.getConnection();

        String sql = "SELECT d.drawing_data " +
                "FROM drawings d " +
                "JOIN sessions s ON d.session_id = s.session_id " +
                "WHERE s.session_name = ? " +
                "ORDER BY d.created_at ASC";


        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                drawings.add(rs.getString("drawing_data"));
            }

            System.out.println("Loaded " + drawings.size() + " drawings for session: " + sessionName);
        } catch (SQLException e) {
            System.err.println("Error loading session: " + e.getMessage());
        }

        return drawings;
    }

    /**
     * Clears all drawings for a specific session
     * @param sessionName The name of the session
     * @return true if successful
     */
    public boolean clearSessionDrawings(String sessionName) {
        Connection conn = dbConnection.getConnection();
        String sql = "DELETE d FROM drawings d " +
                "JOIN sessions s ON d.session_id = s.session_id " +
                "WHERE s.session_name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Cleared " + rowsAffected + " drawings from session: " + sessionName);
            return true;
        } catch (SQLException e) {
            System.err.println("Error clearing session: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a session and all its drawings
     * @param sessionName The name of the session
     * @return true if successful
     */
    public boolean deleteSession(String sessionName) {
        Connection conn = dbConnection.getConnection();
        String sql = "DELETE FROM sessions WHERE session_name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Deleted session: " + sessionName);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting session: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a list of all saved session names
     * @return List of session names
     */
    public List<String> getAllSessions() {
        List<String> sessions = new ArrayList<>();
        Connection conn = dbConnection.getConnection();
        String sql = "SELECT session_name FROM sessions ORDER BY last_modified DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sessions.add(rs.getString("session_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting sessions: " + e.getMessage());
        }

        return sessions;
    }

    /**
     * Gets the total number of drawings in a session
     * @param sessionName The name of the session
     * @return Number of drawings
     */
    public int getDrawingCount(String sessionName) {
        Connection conn = dbConnection.getConnection();
        String sql = "SELECT COUNT(*) as count " +
                "FROM drawings d " +
                "JOIN sessions s ON d.session_id = s.session_id " +
                "WHERE s.session_name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting drawing count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Checks if a session exists
     * @param sessionName The name of the session
     * @return true if session exists
     */
    public boolean sessionExists(String sessionName) {
        Connection conn = dbConnection.getConnection();
        String sql = "SELECT COUNT(*) as count FROM sessions WHERE session_name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking session existence: " + e.getMessage());
        }

        return false;
    }

    /**
     * Gets session metadata
     * @param sessionName The name of the session
     * @return SessionInfo object or null if not found
     */
    public SessionInfo getSessionInfo(String sessionName) {
        Connection conn = dbConnection.getConnection();
        String sql = "SELECT session_id, session_name, created_at, last_modified " +
                "FROM sessions " +
                "WHERE session_name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new SessionInfo(
                        rs.getInt("session_id"),
                        rs.getString("session_name"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("last_modified")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting session info: " + e.getMessage());
        }

        return null;
    }

    public boolean deleteDrawingById(String shapeId) {
        Connection conn = dbConnection.getConnection();
        String sql = "DELETE FROM drawings WHERE drawing_data LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, shapeId + ":%");  // Match shape ID at start of drawing_data
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " drawing(s) with ID: " + shapeId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting drawing by ID: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inner class to hold session metadata
     */
    public static class SessionInfo {
        private final int sessionId;
        private final String sessionName;
        private final Timestamp createdAt;
        private final Timestamp lastModified;

        public SessionInfo(int sessionId, String sessionName, Timestamp createdAt, Timestamp lastModified) {
            this.sessionId = sessionId;
            this.sessionName = sessionName;
            this.createdAt = createdAt;
            this.lastModified = lastModified;
        }

        public int getSessionId() { return sessionId; }
        public String getSessionName() { return sessionName; }
        public Timestamp getCreatedAt() { return createdAt; }
        public Timestamp getLastModified() { return lastModified; }

        @Override
        public String toString() {
            return "Session{" +
                    "id=" + sessionId +
                    ", name='" + sessionName + '\'' +
                    ", created=" + createdAt +
                    ", modified=" + lastModified +
                    '}';
        }
    }
}