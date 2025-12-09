package com.whiteboard.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static jdk.internal.misc.VM.shutdown;

/**
 * Main server class that manages client connections and drawing sessions.
 * Acts as the central hub for all communication between clients.
 */
public class WhiteboardServer {
    private static final int PORT = 8000;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private volatile boolean running = true;

    public static void main(String[] args) {
        WhiteboardServer server = new WhiteboardServer();
        server.start();
    }

    public void start() {
        System.out.println("Whiteboard Server starting on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started successfully. Waiting for clients...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());

                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    threadPool.execute(handler);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public Session getOrCreateSession(String sessionName) {
        return sessions.computeIfAbsent(sessionName, Session::new);
    }

    public void removeEmptySession(String sessionName) {
        Session session = sessions.get(sessionName);
        if (session != null && session.isEmpty()) {
            sessions.remove(sessionName);
            System.out.println("Session removed: " + sessionName);
        }
    }

    public void shutdown() {
        running = false;
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        System.out.println("Server shutdown complete.");
    }
}

