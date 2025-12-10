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
    private volatile boolean connected = false;

    public NetworkHandler(String server, int port, String sessionName, WhiteboardApp app) throws IOException {
        this.app = app;
        this.sessionName = sessionName;
        this.socket = new Socket(server, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.connected = true;

        // Send join request
        out.println("JOIN:" + sessionName);
    }

    @Override
    public void run() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                if (message.startsWith("JOINED:")) {
                    System.out.println("Successfully joined session: " + message.substring(7));
                } else if (message.equals("CLEAR")) {
                    app.getCanvas().clear();
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

    public void sendDrawingEvent(String type, int x1, int y1, int x2, int y2) {
        if (connected && out != null) {
            DrawingShape shape = new DrawingShape(type, x1, y1, x2, y2, app.getCurrentColor(), app.getStrokeWidth());
            out.println(shape.serialize());
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (out != null) {
                out.println("DISCONNECT");
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }


}

