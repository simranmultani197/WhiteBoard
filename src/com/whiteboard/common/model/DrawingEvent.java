package com.whiteboard.common.model;

import java.awt.Color;
import java.io.Serializable;

/**
 * Represents a drawing action performed by a user.
 * Serialized and sent over the network to synchronize drawings.
 */
public class DrawingEvent implements Serializable {
    
    public enum EventType {
        PEN, LINE, RECTANGLE, ERASER, CLEAR
    }
    
    private EventType type;
    private int startX, startY, endX, endY;
    private Color color;
    private int strokeWidth;
    private String clientId;
    private long timestamp;
    
    // Default constructor
    public DrawingEvent() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // Full constructor
    public DrawingEvent(EventType type, int startX, int startY, int endX, int endY, 
                       Color color, int strokeWidth, String clientId) {
        this.type = type;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.clientId = clientId;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public EventType getType() {
        return type;
    }
    
    public void setType(EventType type) {
        this.type = type;
    }
    
    public int getStartX() {
        return startX;
    }
    
    public void setStartX(int startX) {
        this.startX = startX;
    }
    
    public int getStartY() {
        return startY;
    }
    
    public void setStartY(int startY) {
        this.startY = startY;
    }
    
    public int getEndX() {
        return endX;
    }
    
    public void setEndX(int endX) {
        this.endX = endX;
    }
    
    public int getEndY() {
        return endY;
    }
    
    public void setEndY(int endY) {
        this.endY = endY;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public int getStrokeWidth() {
        return strokeWidth;
    }
    
    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

