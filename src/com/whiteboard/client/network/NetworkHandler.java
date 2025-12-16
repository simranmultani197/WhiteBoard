package com.whiteboard.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.whiteboard.client.WhiteboardApp;
import com.whiteboard.client.ui.DrawingShape;
import java.io.InputStreamReader;

/**
 * Handles communication with the server.
 * Sends drawing data and receives updates from other users.
 */
public class NetworkHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private WhiteboardApp app;
    private String sessionName;
    private String username;
    private volatile boolean connected = false;

    public NetworkHandler(String server, int port, String sessionName, String username, WhiteboardApp app)
            throws IOException {
        this.app = app;
        this.sessionName = sessionName;
        this.username = username;
        this.socket = new Socket(server, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.connected = true;

        // Send join request
        out.println("JOIN:" + sessionName + ":" + username);
    }

    @Override
    public void run() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                System.out.println("Received message: " + message);
                if (message.startsWith("JOINED:")) {
                    System.out.println("Successfully joined session: " + message.substring(7));
                } else if (message.equals("CLEAR")) {
                    app.getCanvas().clear();
                } else if (message.startsWith("DELETE:")) {
                    // Handle delete event
                    String shapeId = message.substring(7);
                    app.getCanvas().removeShapeById(shapeId);
                } else if (message.startsWith("USER_LIST:")) {
                    // Received user list from server
                    String userListStr = message.substring(10);
                    String[] users = userListStr.split(",");
                    app.updateUserList(users);
                } else if (message.startsWith("USER_JOIN:")) {
                    // New user joined
                    String newUser = message.substring(10);
                    app.addUser(newUser);
                } else if (message.startsWith("USER_LEAVE:")) {
                    // User left
                    String leftUser = message.substring(11);
                    app.removeUser(leftUser);
                } else {
                    // Drawing event from another user
                    DrawingShape shape = DrawingShape.deserialize(message);
                    if (shape != null) {
                        app.getCanvas().addRemoteShape(shape);
                    }
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Connection error: " + e.getMessage());
                app.updateStatus("Disconnected from server");
            }
        } finally {
            disconnect();
        }
    }

    public void sendDrawingEvent(DrawingShape shape) {
        if (connected && out != null) {
            out.println(shape.serialize());
        }
    }

    public void sendDeleteEvent(String shapeId) {
        if (connected && out != null) {
            out.println("DELETE:" + shapeId);
        }
    }

    public void sendClearEvent() {
        if (connected && out != null) {
            out.println("CLEAR");
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (out != null) {
                out.println("DISCONNECT");
            }
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

}
