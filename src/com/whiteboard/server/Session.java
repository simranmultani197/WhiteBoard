package com.whiteboard.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Represents a shared whiteboard session where multiple clients can draw together.
 * Manages clients and broadcasts drawing events to all participants.
 */
public class Session {
    
    private String sessionName;
    private ConcurrentHashMap<String, ClientHandler> clients;
    
    public Session(String sessionName) {
        this.sessionName = sessionName;
        this.clients = new ConcurrentHashMap<>();
    }
    
    public void addClient(String clientId, ClientHandler handler) {
        // TODO: Implement client addition logic
    }
    
    public void removeClient(String clientId) {
        // TODO: Implement client removal logic
    }
    
    public void broadcastToAll(String message) {
        // TODO: Implement message broadcasting to all clients in session
    }
    
    public String getSessionName() {
        return sessionName;
    }
    
    public Set<String> getClientIds() {
        return clients.keySet();
    }
}

