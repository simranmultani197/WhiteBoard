package com.whiteboard.client.ui;

import java.awt.*;
import java.io.Serializable;

/**
 * Represents a drawing action performed by a user.
 * Serialized and sent over the network to synchronize drawings.
 */
public class DrawingShape implements Serializable {

    String type;
    int x1, y1, x2, y2;
    Color color;
    int strokeWidth;

    public DrawingShape(String type, int x1, int y1, int x2, int y2, Color color, int strokeWidth) {
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case "PEN":
            case "ERASER":
                g2d.drawLine(x1, y1, x2, y2);
                break;
            case "LINE":
                g2d.drawLine(x1, y1, x2, y2);
                break;
            case "RECTANGLE":
                int x = Math.min(x1, x2);
                int y = Math.min(y1, y2);
                int width = Math.abs(x2 - x1);
                int height = Math.abs(y2 - y1);
                g2d.drawRect(x, y, width, height);
                break;
        }
    }

    public String serialize() {
        return String.format("%s:%d:%d:%d:%d:%d,%d,%d:%d",
                type, x1, y1, x2, y2,
                color.getRed(), color.getGreen(), color.getBlue(),
                strokeWidth);
    }

    public static DrawingShape deserialize(String data) {
        try {
            String[] parts = data.split(":");
            if (parts.length >= 7) {
                String[] rgb = parts[5].split(",");
                Color color = new Color(
                        Integer.parseInt(rgb[0]),
                        Integer.parseInt(rgb[1]),
                        Integer.parseInt(rgb[2])
                );
                return new DrawingShape(
                        parts[0],
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4]),
                        color,
                        Integer.parseInt(parts[6])
                );
            }
        } catch (Exception e) {
            System.err.println("Error deserializing shape: " + e.getMessage());
        }
        return null;
    }
}
