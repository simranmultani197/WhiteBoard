package com.whiteboard.client.network;

import java.net.Socket;
import com.whiteboard.common.model.DrawingEvent;

/**
 * Handles communication with the server.
 * Sends drawing data and receives updates from other users.
 */
public class NetworkHandler {
    
    private Socket socket;
    private String sessionName;
    
    public NetworkHandler(String serverAddress, int port, String sessionName) {
        // TODO: Initialize connection to server
    }
    
    public void sendDrawingEvent(DrawingEvent event) {
        // TODO: Send drawing event to server
    }
    
    public void startListening() {
        // TODO: Start listening thread for incoming messages
    }
    
    public void disconnect() {
        // TODO: Close connection
    }
}

