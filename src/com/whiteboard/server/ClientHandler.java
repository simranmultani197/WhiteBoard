package com.whiteboard.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles communication with an individual client.
 * Each client connection runs in a separate thread.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final WhiteboardServer server;
    private BufferedReader in;
    private PrintWriter out;
    private Session currentSession;
    private String clientId;
    private String username;

    public ClientHandler(Socket socket, WhiteboardServer server) {
        this.socket = socket;
        this.server = server;
        this.clientId = socket.getInetAddress() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String joinMessage = in.readLine();
            System.out.println("RAW JOIN MESSAGE: [" + joinMessage + "]");
            if (joinMessage == null || !joinMessage.startsWith("JOIN:")) {
                System.err.println("Invalid JOIN from " + clientId + ": " + joinMessage);
                return;
            }

            // Expected: JOIN:sessionName:username
            String[] parts = joinMessage.substring(5).split(":", 2);
            if (parts.length != 2) {
                System.err.println("Malformed JOIN from " + clientId);
                return;
            }

            String sessionName = parts[0];
            username = parts[1];

            currentSession = server.getOrCreateSession(sessionName);
            currentSession.addClient(this, username);

            // Acknowledge join
            sendMessage("JOINED:" + sessionName);

            String message;
            while ((message = in.readLine()) != null) {

                if (message.equals("DISCONNECT")) {
                    break;
                }

                // Broadcast EVERYTHING else (DRAW, CLEAR, DELETE, etc.)
                currentSession.broadcast(message, this);
            }

        } catch (IOException e) {
            System.err.println("Client handler error (" + clientId + "): " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void cleanup() {
        try {
            if (currentSession != null && username != null) {
                currentSession.removeClient(username);
                server.removeEmptySession(currentSession.getSessionName());
            }

            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();

            System.out.println("Client disconnected: " + clientId);

        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}
