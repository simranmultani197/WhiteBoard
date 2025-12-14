package com.whiteboard.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import com.whiteboard.client.WhiteboardApp;
import com.whiteboard.client.network.NetworkHandler;

/**
 * The drawing surface that displays all user interactions.
 * Handles mouse events and drawing operations.
 */
public class DrawCanvas extends JPanel {

    private WhiteboardApp app;
    private List<DrawingShape> shapes = new ArrayList<>();
    private Point startPoint;
    private Point currentPoint;
    private boolean drawing = false;

    public DrawCanvas(WhiteboardApp app) {
        this.app = app;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private void handleMousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        currentPoint = e.getPoint();
        drawing = true;

        if (app.getCurrentTool().equals("SELECT")) {
            drawing = false;
            return;
        }

        if (app.getCurrentTool().equals("ERASER")) {
            // Erase shapes at this point
            eraseAtPoint(currentPoint.x, currentPoint.y, app.getStrokeWidth() * 2);
            repaint();
        } else if (app.getCurrentTool().equals("PEN")) {
            DrawingShape shape = new DrawingShape(
                    app.getCurrentTool(),
                    startPoint.x, startPoint.y,
                    currentPoint.x, currentPoint.y,
                    app.getCurrentColor(),
                    app.getStrokeWidth()
            );
            shapes.add(shape);
            repaint();
            sendShape(shape);
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (!drawing) return;

        currentPoint = e.getPoint();

        if (app.getCurrentTool().equals("SELECT")) {
            return;
        }

        if (app.getCurrentTool().equals("ERASER")) {
            // Continue erasing as mouse drags
            eraseAtPoint(currentPoint.x, currentPoint.y, app.getStrokeWidth() * 2);
            startPoint = currentPoint;
            repaint();
        } else if (app.getCurrentTool().equals("PEN")) {
            DrawingShape shape = new DrawingShape(
                    app.getCurrentTool(),
                    startPoint.x, startPoint.y,
                    currentPoint.x, currentPoint.y,
                    app.getCurrentColor(),
                    app.getStrokeWidth()
            );
            shapes.add(shape);
            startPoint = currentPoint;
            repaint();
            sendShape(shape);
        } else {
            // For other tools (LINE, RECTANGLE, CIRCLE, TRIANGLE), just repaint to show preview
            repaint();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (!drawing) return;

        currentPoint = e.getPoint();

        if (app.getCurrentTool().equals("LINE") || app.getCurrentTool().equals("RECTANGLE")
                || app.getCurrentTool().equals("CIRCLE") || app.getCurrentTool().equals("TRIANGLE")) {
            DrawingShape shape = new DrawingShape(
                    app.getCurrentTool(),
                    startPoint.x, startPoint.y,
                    currentPoint.x, currentPoint.y,
                    app.getCurrentColor(),
                    app.getStrokeWidth()
            );
            shapes.add(shape);
            repaint();
            sendShape(shape);
        }

        drawing = false;
    }

    /**
     * Erases shapes that intersect with the eraser point
     */
    private void eraseAtPoint(int x, int y, int eraserRadius) {
        List<DrawingShape> toRemove = new ArrayList<>();

        // Find all shapes that intersect with the eraser
        for (DrawingShape shape : shapes) {
            if (shapeIntersectsPoint(shape, x, y, eraserRadius)) {
                toRemove.add(shape);
            }
        }

        // Remove shapes locally and notify server to delete them
        for (DrawingShape shape : toRemove) {
            shapes.remove(shape);

            // Send delete command to server with shape ID
            NetworkHandler handler = app.getNetworkHandler();
            if (handler != null && handler.isConnected()) {
                handler.sendDeleteEvent(shape.getId());
            }
        }
    }

    /**
     * Checks if a shape intersects with a point (for eraser)
     */
    private boolean shapeIntersectsPoint(DrawingShape shape, int x, int y, int radius) {
        switch (shape.type) {
            case "PEN":
            case "LINE":
                // Check distance from point to line segment
                double dist = pointToLineDistance(x, y, shape.x1, shape.y1, shape.x2, shape.y2);
                return dist <= radius + shape.strokeWidth;

            case "RECTANGLE":
                int rectX = Math.min(shape.x1, shape.x2);
                int rectY = Math.min(shape.y1, shape.y2);
                int rectWidth = Math.abs(shape.x2 - shape.x1);
                int rectHeight = Math.abs(shape.y2 - shape.y1);
                return pointIntersectsRectangle(x, y, radius, rectX, rectY, rectWidth, rectHeight);

            case "CIRCLE":
                int circleX = Math.min(shape.x1, shape.x2);
                int circleY = Math.min(shape.y1, shape.y2);
                int circleSize = Math.max(Math.abs(shape.x2 - shape.x1), Math.abs(shape.y2 - shape.y1));
                int circleCenterX = circleX + circleSize / 2;
                int circleCenterY = circleY + circleSize / 2;
                double distToCenter = Math.sqrt(Math.pow(x - circleCenterX, 2) + Math.pow(y - circleCenterY, 2));
                return Math.abs(distToCenter - circleSize / 2) <= radius + shape.strokeWidth;

            case "TRIANGLE":
                // Simplified: check if point is near any of the three edges
                int topX = shape.x1 + (shape.x2 - shape.x1) / 2;
                int topY = Math.min(shape.y1, shape.y2);
                int bottomY = Math.max(shape.y1, shape.y2);

                // Check three edges
                double dist1 = pointToLineDistance(x, y, topX, topY, shape.x1, bottomY);
                double dist2 = pointToLineDistance(x, y, shape.x1, bottomY, shape.x2, bottomY);
                double dist3 = pointToLineDistance(x, y, shape.x2, bottomY, topX, topY);

                return dist1 <= radius + shape.strokeWidth ||
                        dist2 <= radius + shape.strokeWidth ||
                        dist3 <= radius + shape.strokeWidth;

            default:
                return false;
        }
    }

    /**
     * Calculate distance from point to line segment
     */
    private double pointToLineDistance(int px, int py, int x1, int y1, int x2, int y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? dot / lenSq : -1;

        double xx, yy;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Check if point intersects with rectangle outline
     */
    private boolean pointIntersectsRectangle(int px, int py, int radius, int rx, int ry, int rw, int rh) {
        // Check all four edges of the rectangle
        double distTop = pointToLineDistance(px, py, rx, ry, rx + rw, ry);
        double distBottom = pointToLineDistance(px, py, rx, ry + rh, rx + rw, ry + rh);
        double distLeft = pointToLineDistance(px, py, rx, ry, rx, ry + rh);
        double distRight = pointToLineDistance(px, py, rx + rw, ry, rx + rw, ry + rh);

        return distTop <= radius || distBottom <= radius || distLeft <= radius || distRight <= radius;
    }

    private void sendShape(DrawingShape shape) {
        NetworkHandler handler = app.getNetworkHandler();
        if (handler != null && handler.isConnected()) {
            handler.sendDrawingEvent(shape.type, shape.x1, shape.y1, shape.x2, shape.y2);
        }
    }

    public void addRemoteShape(DrawingShape shape) {
        shapes.add(shape);
        SwingUtilities.invokeLater(this::repaint);
    }

    public void clear() {
        shapes.clear();
        repaint();
    }

    /**
     * Remove a shape by ID (called when remote user erases)
     */
    public void removeShapeById(String shapeId) {
        shapes.removeIf(shape -> shape.getId().equals(shapeId));
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw all shapes
        for (DrawingShape shape : shapes) {
            shape.draw(g2d);
        }

        // Draw preview for LINE, RECTANGLE, CIRCLE, and TRIANGLE
        if (drawing && (app.getCurrentTool().equals("LINE") || app.getCurrentTool().equals("RECTANGLE")
                || app.getCurrentTool().equals("CIRCLE") || app.getCurrentTool().equals("TRIANGLE"))) {
            g2d.setColor(app.getCurrentColor());
            g2d.setStroke(new BasicStroke(app.getStrokeWidth()));

            if (app.getCurrentTool().equals("LINE")) {
                g2d.drawLine(startPoint.x, startPoint.y, currentPoint.x, currentPoint.y);
            } else if (app.getCurrentTool().equals("RECTANGLE")) {
                int x = Math.min(startPoint.x, currentPoint.x);
                int y = Math.min(startPoint.y, currentPoint.y);
                int width = Math.abs(currentPoint.x - startPoint.x);
                int height = Math.abs(currentPoint.y - startPoint.y);
                g2d.drawRect(x, y, width, height);
            } else if (app.getCurrentTool().equals("CIRCLE")) {
                int x = Math.min(startPoint.x, currentPoint.x);
                int y = Math.min(startPoint.y, currentPoint.y);
                int width = Math.abs(currentPoint.x - startPoint.x);
                int height = Math.abs(currentPoint.y - startPoint.y);
                int size = Math.max(width, height);
                g2d.drawOval(x, y, size, size);
            } else if (app.getCurrentTool().equals("TRIANGLE")) {
                int topX = startPoint.x + (currentPoint.x - startPoint.x) / 2;
                int topY = Math.min(startPoint.y, currentPoint.y);
                int bottomY = Math.max(startPoint.y, currentPoint.y);
                int[] xPoints = {topX, startPoint.x, currentPoint.x};
                int[] yPoints = {topY, bottomY, bottomY};
                g2d.drawPolygon(xPoints, yPoints, 3);
            }
        }
    }
}