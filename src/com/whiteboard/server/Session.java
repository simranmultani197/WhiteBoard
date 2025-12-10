package com.whiteboard.server;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a shared whiteboard session where multiple clients can draw together.
 * Manages clients and broadcasts drawing events to all participants.
 */
public class Session {

    private final String sessionName;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final List<String> drawingHistory = new CopyOnWriteArrayList<>();
    
    public Session(String sessionName) {
        this.sessionName = sessionName;
        System.out.println("New session created: " + sessionName);
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

    public void broadcast(String message, ClientHandler sender) {
        if (message.equals("CLEAR")) {
            drawingHistory.clear();
        } else {
            drawingHistory.add(message);
        }

        // Broadcast to all clients except sender
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public boolean isEmpty() {
        return clients.isEmpty();
    }

    public String getSessionName() {
        return sessionName;
    }

}

