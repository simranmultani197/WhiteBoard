package com.whiteboard.common.model;

import java.awt.Color;
import java.io.Serializable;

/**
 * Represents a drawing action performed by a user.
 * Serialized and sent over the network to synchronize drawings.
 */
public class DrawingEvent implements Serializable {

    private String type; // DRAW, LINE, RECTANGLE, ERASE, CLEAR, COLOR
    private int x1, y1, x2, y2;
    private String color;
    private int strokeWidth;

    public DrawingEvent(String type, int x1, int y1, int x2, int y2, String color, int strokeWidth) {
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    public String serialize() {
        return String.format("%s:%d:%d:%d:%d:%s:%d", type, x1, y1, x2, y2, color, strokeWidth);
    }

    public static DrawingEvent deserialize(String message) {
        try {
            String[] parts = message.split(":");
            if (parts.length >= 7) {
                return new DrawingEvent(
                        parts[0],
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4]),
                        parts[5],
                        Integer.parseInt(parts[6])
                );
            }
        } catch (Exception e) {
            System.err.println("Error deserializing event: " + e.getMessage());
        }
        return null;
    }

    public String getType() {
        return type;
    }

}

