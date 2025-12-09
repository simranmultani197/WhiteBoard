package com.whiteboard.server;

import jdk.internal.org.jline.utils.InputStreamReader;

import java.io.BufferedReader;
import java.io.IOException;
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


    public ClientHandler(Socket socket, WhiteboardServer server) {
        this.socket = socket;
        this.server = server;
        this.clientId = socket.getInetAddress().toString() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // First message should be session join request
            String joinMessage = in.readLine();
            if (joinMessage != null && joinMessage.startsWith("JOIN:")) {
                String sessionName = joinMessage.substring(5);
                currentSession = server.getOrCreateSession(sessionName);
                currentSession.addClient(this);

                sendMessage("JOINED:" + sessionName);

                // Listen for drawing events
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("DISCONNECT")) {
                        break;
                    }
                    currentSession.broadcast(message, this);
                }
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
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
            if (currentSession != null) {
                currentSession.removeClient(this);
                server.removeEmptySession(currentSession.getSessionName());
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Client disconnected: " + clientId);
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

}

