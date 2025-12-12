package com.whiteboard.server;

import com.whiteboard.database.SessionDao;
import java.util.*;
import java.util.concurrent.*;

/**
 * Represents a shared whiteboard session where multiple clients can draw together.
 * Manages clients, broadcasts drawing events, and persists data to database.
 */
public class Session {

    private final String sessionName;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final List<String> drawingHistory = new CopyOnWriteArrayList<>();
    private final SessionDao sessionDao;
    private final boolean persistToDatabase;

    /**
     * Creates a new session with optional database persistence
     * @param sessionName Name of the session
     * @param sessionDao Data access object for database operations (can be null)
     * @param persistToDatabase Whether to save drawings to database
     */
    public Session(String sessionName, SessionDao sessionDao, boolean persistToDatabase) {
        this.sessionName = sessionName;
        this.sessionDao = sessionDao;
        this.persistToDatabase = persistToDatabase;
        System.out.println("New session created: " + sessionName +
                (persistToDatabase ? " (with database)" : " (in-memory only)"));

        // Load existing drawings from database if persistence is enabled
        if (persistToDatabase && sessionDao != null) {
            loadFromDatabase();
        }
    }

    /**
     * Creates a session without database (backward compatible)
     * @param sessionName Name of the session
     */
    public Session(String sessionName) {
        this(sessionName, null, false);
    }

    /**
     * Loads drawings from database into memory
     */
    private void loadFromDatabase() {
        try {
            List<String> savedDrawings = sessionDao.loadSessionDrawings(sessionName);
            drawingHistory.addAll(savedDrawings);
            System.out.println("Loaded " + savedDrawings.size() +
                    " drawings from database for session: " + sessionName);
        } catch (Exception e) {
            System.err.println("Error loading session from database: " + e.getMessage());
        }
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
        System.out.println("Client joined session '" + sessionName + "'. Total clients: " + clients.size());

        // Send drawing history to new client
        for (String event : drawingHistory) {
            client.sendMessage(event);
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client left session '" + sessionName + "'. Remaining clients: " + clients.size());
    }

    /**
     * Broadcasts a drawing message to all clients and optionally saves to database
     * @param message The drawing data or CLEAR command
     * @param sender The client who sent the message
     */
    public void broadcast(String message, ClientHandler sender) {
        // Handle clear command
        if (message.equals("CLEAR")) {
            drawingHistory.clear();

            // Clear from database if persistence enabled
            if (persistToDatabase && sessionDao != null) {
                try {
                    sessionDao.clearSessionDrawings(sessionName);
                } catch (Exception e) {
                    System.err.println("Error clearing session from database: " + e.getMessage());
                }
            }
        } else {
            // Add to memory
            drawingHistory.add(message);

            // Save to database if persistence enabled
            if (persistToDatabase && sessionDao != null) {
                try {
                    sessionDao.saveDrawing(sessionName, message);
                } catch (Exception e) {
                    System.err.println("Error saving drawing to database: " + e.getMessage());
                }
            }
        }

        // Broadcast to all clients except sender
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Manually save current session to database
     */
    public void saveToDatabase() {
        if (sessionDao != null) {
            try {
                // Clear existing and save all current drawings
                sessionDao.clearSessionDrawings(sessionName);
                for (String drawing : drawingHistory) {
                    sessionDao.saveDrawing(sessionName, drawing);
                }
                System.out.println("Session manually saved to database: " + sessionName);
            } catch (Exception e) {
                System.err.println("Error manually saving session: " + e.getMessage());
            }
        }
    }

    public boolean isEmpty() {
        return clients.isEmpty();
    }

    public String getSessionName() {
        return sessionName;
    }

    public int getDrawingCount() {
        return drawingHistory.size();
    }
}
