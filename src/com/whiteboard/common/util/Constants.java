package com.whiteboard.common.util;

/**
 * Constants used throughout the application.
 */
public class Constants {
    
    // Network constants
    public static final int DEFAULT_SERVER_PORT = 8888;
    public static final String DEFAULT_SERVER_HOST = "localhost";
    
    // Protocol constants
    public static final String PROTOCOL_JOIN_SESSION = "JOIN_SESSION";
    public static final String PROTOCOL_LEAVE_SESSION = "LEAVE_SESSION";
    public static final String PROTOCOL_DRAWING_EVENT = "DRAWING_EVENT";
    public static final String PROTOCOL_CLIENT_CONNECTED = "CLIENT_CONNECTED";
    public static final String PROTOCOL_CLIENT_DISCONNECTED = "CLIENT_DISCONNECTED";
    
    // GUI constants
    public static final int CANVAS_WIDTH = 800;
    public static final int CANVAS_HEIGHT = 600;
    
    private Constants() {
        // Utility class - prevent instantiation
    }
}

