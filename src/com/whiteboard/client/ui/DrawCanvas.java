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

        if (app.getCurrentTool().equals("PEN") || app.getCurrentTool().equals("ERASER")) {
            DrawingShape shape = new DrawingShape(
                    app.getCurrentTool(),
                    startPoint.x, startPoint.y,
                    currentPoint.x, currentPoint.y,
                    app.getCurrentTool().equals("ERASER") ? Color.WHITE : app.getCurrentColor(),
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

        if (app.getCurrentTool().equals("PEN") || app.getCurrentTool().equals("ERASER")) {
            DrawingShape shape = new DrawingShape(
                    app.getCurrentTool(),
                    startPoint.x, startPoint.y,
                    currentPoint.x, currentPoint.y,
                    app.getCurrentTool().equals("ERASER") ? Color.WHITE : app.getCurrentColor(),
                    app.getStrokeWidth()
            );
            shapes.add(shape);
            startPoint = currentPoint;
            repaint();
            sendShape(shape);
        } else {
            repaint();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (!drawing) return;

        currentPoint = e.getPoint();

        if (app.getCurrentTool().equals("LINE") || app.getCurrentTool().equals("RECTANGLE")) {
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw all shapes
        for (DrawingShape shape : shapes) {
            shape.draw(g2d);
        }

        // Draw preview for LINE and RECTANGLE
        if (drawing && (app.getCurrentTool().equals("LINE") || app.getCurrentTool().equals("RECTANGLE"))) {
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
            }
        }
    }
}

