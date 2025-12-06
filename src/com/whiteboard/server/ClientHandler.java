package com.whiteboard.server;

import java.net.Socket;

/**
 * Handles communication with an individual client.
 * Each client connection runs in a separate thread.
 */
public class ClientHandler extends Thread {
    
    private Socket clientSocket;
    private Session session;
    
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run() {
        // TODO: Implement client communication handling
    }
}

