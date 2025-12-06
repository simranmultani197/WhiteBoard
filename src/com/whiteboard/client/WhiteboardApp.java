package com.whiteboard.client;

import javax.swing.UIManager;
import com.whiteboard.client.ui.WhiteboardFrame;
import com.whiteboard.client.ui.ConnectionDialog;
import com.whiteboard.client.network.NetworkHandler;

/**
 * Main client application entry point.
 * Initializes the GUI and network connection.
 */
public class WhiteboardApp {
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            String sessionName = "Local Test";
            
            // Check for test/local mode argument
            boolean localMode = args.length > 0 && ("--local".equals(args[0]) || "-l".equals(args[0]));
            
            if (!localMode) {
                // Show connection dialog first
                ConnectionDialog dialog = new ConnectionDialog(null);
                dialog.setVisible(true);
                
                if (dialog.isConnected()) {
                    String serverAddress = dialog.getServerAddress();
                    sessionName = dialog.getSessionName();
                    
                    // Parse server address (format: host:port)
                    String[] addressParts = serverAddress.split(":");
                    String host = addressParts.length > 0 ? addressParts[0] : "localhost";
                    int port = addressParts.length > 1 ? Integer.parseInt(addressParts[1]) : 8888;
                    
                    // Create and show main frame
                    WhiteboardFrame frame = new WhiteboardFrame(sessionName);
                    frame.setVisible(true);
                    
                    // TODO: Initialize network handler and connect to server
                    // NetworkHandler networkHandler = new NetworkHandler(host, port, sessionName);
                    // frame.getCanvas().setDrawingListener(event -> networkHandler.sendDrawingEvent(event));
                    // networkHandler.startListening();
                } else {
                    System.exit(0);
                }
            } else {
                // Local test mode - skip connection dialog
                WhiteboardFrame frame = new WhiteboardFrame(sessionName);
                frame.setVisible(true);
                frame.setStatus("Local Test Mode - Drawing works without server");
                System.out.println("Running in local test mode. GUI is ready for testing!");
            }
        });
    }
}

