package com.whiteboard.client.ui;

import java.awt.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a drawing action performed by a user.
 * Serialized and sent over the network to synchronize drawings.
 */
public class DrawingShape implements Serializable {

    String id;
    String type;
    int x1, y1, x2, y2;
    Color color;
    int strokeWidth;

    public DrawingShape(String type, int x1, int y1, int x2, int y2, Color color, int strokeWidth) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    public DrawingShape(String id, String type, int x1, int y1, int x2, int y2, Color color, int strokeWidth) {
        this.id = id;
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
            case "CIRCLE":
                int cx = Math.min(x1, x2);
                int cy = Math.min(y1, y2);
                int cwidth = Math.abs(x2 - x1);
                int cheight = Math.abs(y2 - y1);
                // Use the larger dimension to make it a circle
                int size = Math.max(cwidth, cheight);
                g2d.drawOval(cx, cy, size, size);
                break;
            case "TRIANGLE":
                // Draw triangle: top point at min y, base at max y
                int[] xPoints = {
                    x1,
                    x2,
                    x1 + (x2 - x1) / 2
                };
                int[] yPoints = {
                    Math.max(y1, y2),
                    Math.max(y1, y2),
                    Math.min(y1, y2)
                };
                g2d.drawPolygon(xPoints, yPoints, 3);
                break;
        }
    }

    public String serialize() {
        return String.format("%s:%s:%d:%d:%d:%d:%d,%d,%d:%d",
                id, type, x1, y1, x2, y2,
                color.getRed(), color.getGreen(), color.getBlue(),
                strokeWidth);
    }

    public static DrawingShape deserialize(String data) {
        try {
            String[] parts = data.split(":");
            if (parts.length >= 8) {  // Changed from 7 to 8 (added ID field)
                String id = parts[0];      // ID is first
                String type = parts[1];    // Type is second
                int x1 = Integer.parseInt(parts[2]);
                int y1 = Integer.parseInt(parts[3]);
                int x2 = Integer.parseInt(parts[4]);
                int y2 = Integer.parseInt(parts[5]);

                String[] rgb = parts[6].split(",");  // Color moved to index 6
                Color color = new Color(
                        Integer.parseInt(rgb[0]),
                        Integer.parseInt(rgb[1]),
                        Integer.parseInt(rgb[2])
                );

                int strokeWidth = Integer.parseInt(parts[7]);  // Stroke width moved to index 7

                return new DrawingShape(id, type, x1, y1, x2, y2, color, strokeWidth);
            }
        } catch (Exception e) {
            System.err.println("Error deserializing shape: " + e.getMessage());
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
