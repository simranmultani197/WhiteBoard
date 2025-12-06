package com.whiteboard.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import com.whiteboard.common.model.DrawingEvent;
import com.whiteboard.common.util.Constants;

/**
 * The drawing surface that displays all user interactions.
 * Handles mouse events and drawing operations.
 */
public class DrawCanvas extends JPanel {
    
    private List<DrawingEvent> drawingEvents;
    private DrawingEvent.EventType currentTool;
    private Color currentColor;
    private int strokeWidth;
    private int startX, startY;
    private int currentX, currentY;
    private boolean isDrawing;
    
    // Listener interface for drawing events
    public interface DrawingListener {
        void onDrawingEvent(DrawingEvent event);
    }
    
    private DrawingListener drawingListener;
    
    public DrawCanvas() {
        this.drawingEvents = new ArrayList<>();
        this.currentTool = DrawingEvent.EventType.PEN;
        this.currentColor = Color.BLACK;
        this.strokeWidth = 3;
        this.isDrawing = false;
        
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT));
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        setupMouseListeners();
    }
    
    private void setupMouseListeners() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                isDrawing = true;
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDrawing) {
                    currentX = e.getX();
                    currentY = e.getY();
                    
                    if (currentTool == DrawingEvent.EventType.PEN) {
                        // For pen, create continuous events
                        DrawingEvent event = new DrawingEvent(
                            DrawingEvent.EventType.PEN,
                            startX, startY, currentX, currentY,
                            currentColor, strokeWidth, "local"
                        );
                        addDrawingEvent(event);
                        
                        // Update start position for next segment
                        startX = currentX;
                        startY = currentY;
                    } else {
                        // For shapes, just repaint preview
                        repaint();
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrawing) {
                    currentX = e.getX();
                    currentY = e.getY();
                    
                    DrawingEvent event = new DrawingEvent(
                        currentTool,
                        startX, startY, currentX, currentY,
                        currentColor, strokeWidth, "local"
                    );
                    
                    addDrawingEvent(event);
                    isDrawing = false;
                    repaint();
                }
            }
        };
        
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }
    
    public void addDrawingEvent(DrawingEvent event) {
        drawingEvents.add(event);
        
        // Notify listener if drawing event is from local user
        if (drawingListener != null && "local".equals(event.getClientId())) {
            drawingListener.onDrawingEvent(event);
        }
        
        repaint();
    }
    
    public void setDrawingListener(DrawingListener listener) {
        this.drawingListener = listener;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw all existing events
        for (DrawingEvent event : drawingEvents) {
            drawEvent(g2d, event);
        }
        
        // Draw preview of current drawing if dragging
        if (isDrawing && currentTool != DrawingEvent.EventType.PEN) {
            g2d.setColor(currentColor);
            g2d.setStroke(new BasicStroke(strokeWidth));
            drawShapePreview(g2d, currentTool, startX, startY, currentX, currentY);
        }
    }
    
    private void drawEvent(Graphics2D g2d, DrawingEvent event) {
        g2d.setColor(event.getColor());
        g2d.setStroke(new BasicStroke(event.getStrokeWidth()));
        
        switch (event.getType()) {
            case PEN:
                g2d.drawLine(event.getStartX(), event.getStartY(), 
                           event.getEndX(), event.getEndY());
                break;
            case LINE:
                g2d.drawLine(event.getStartX(), event.getStartY(), 
                           event.getEndX(), event.getEndY());
                break;
            case RECTANGLE:
                int x = Math.min(event.getStartX(), event.getEndX());
                int y = Math.min(event.getStartY(), event.getEndY());
                int width = Math.abs(event.getEndX() - event.getStartX());
                int height = Math.abs(event.getEndY() - event.getStartY());
                g2d.drawRect(x, y, width, height);
                break;
            case ERASER:
                // Eraser - draw white over area
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(event.getStrokeWidth() * 2));
                g2d.drawLine(event.getStartX(), event.getStartY(), 
                           event.getEndX(), event.getEndY());
                break;
            case CLEAR:
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                break;
        }
    }
    
    private void drawShapePreview(Graphics2D g2d, DrawingEvent.EventType tool, 
                                  int x1, int y1, int x2, int y2) {
        switch (tool) {
            case LINE:
                g2d.drawLine(x1, y1, x2, y2);
                break;
            case RECTANGLE:
                int x = Math.min(x1, x2);
                int y = Math.min(y1, y2);
                int width = Math.abs(x2 - x1);
                int height = Math.abs(y2 - y1);
                g2d.drawRect(x, y, width, height);
                break;
            case ERASER:
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(strokeWidth * 2));
                g2d.drawLine(x1, y1, x2, y2);
                break;
        }
    }
    
    // Getters and Setters
    public void setCurrentTool(DrawingEvent.EventType tool) {
        this.currentTool = tool;
    }
    
    public DrawingEvent.EventType getCurrentTool() {
        return currentTool;
    }
    
    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }
    
    public Color getCurrentColor() {
        return currentColor;
    }
    
    public void setStrokeWidth(int width) {
        this.strokeWidth = width;
    }
    
    public int getStrokeWidth() {
        return strokeWidth;
    }
    
    public void clearCanvas() {
        drawingEvents.clear();
        DrawingEvent clearEvent = new DrawingEvent(
            DrawingEvent.EventType.CLEAR, 0, 0, 0, 0,
            Color.WHITE, 0, "local"
        );
        addDrawingEvent(clearEvent);
    }
    
    public List<DrawingEvent> getDrawingEvents() {
        return new ArrayList<>(drawingEvents);
    }
}

