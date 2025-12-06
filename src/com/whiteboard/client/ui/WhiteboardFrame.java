package com.whiteboard.client.ui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import com.whiteboard.common.model.DrawingEvent;

/**
 * Main window of the whiteboard application.
 * Contains the canvas, toolbar, and menu bar.
 */
public class WhiteboardFrame extends JFrame {
    
    private DrawCanvas canvas;
    private JToolBar toolBar;
    private JMenuBar menuBar;
    private JButton penButton, lineButton, rectangleButton, eraserButton, clearButton;
    private JButton colorButton;
    private JComboBox<Integer> strokeWidthCombo;
    private JLabel statusLabel;
    private Color selectedColor;
    private String sessionName;
    
    public WhiteboardFrame(String sessionName) {
        this.sessionName = sessionName;
        this.selectedColor = Color.BLACK;
        initializeComponents();
        setupMenus();
        setupToolbar();
        setupLayout();
        setupActions();
    }
    
    private void initializeComponents() {
        setTitle("Whiteboard - " + (this.sessionName != null ? this.sessionName : "Session"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        canvas = new DrawCanvas();
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        menuBar = new JMenuBar();
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }
    
    private void setupToolbar() {
        // Tool buttons
        penButton = new JButton("Pen");
        penButton.setToolTipText("Draw with pen tool");
        lineButton = new JButton("Line");
        lineButton.setToolTipText("Draw lines");
        rectangleButton = new JButton("Rectangle");
        rectangleButton.setToolTipText("Draw rectangles");
        eraserButton = new JButton("Eraser");
        eraserButton.setToolTipText("Erase drawings");
        clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear canvas");
        
        // Color button
        colorButton = new JButton("Color");
        colorButton.setToolTipText("Select color");
        updateColorButton();
        
        // Stroke width selector
        Integer[] widths = {1, 2, 3, 5, 8, 10, 15, 20};
        strokeWidthCombo = new JComboBox<>(widths);
        strokeWidthCombo.setSelectedItem(3);
        strokeWidthCombo.setToolTipText("Stroke width");
        
        // Add to toolbar
        toolBar.add(penButton);
        toolBar.add(lineButton);
        toolBar.add(rectangleButton);
        toolBar.add(eraserButton);
        toolBar.addSeparator();
        toolBar.add(colorButton);
        toolBar.add(new JLabel(" Width: "));
        toolBar.add(strokeWidthCombo);
        toolBar.addSeparator();
        toolBar.add(clearButton);
        
        // Set pen as default
        penButton.setSelected(true);
    }
    
    private void setupMenus() {
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem clearItem = new JMenuItem("Clear Canvas");
        clearItem.setAccelerator(KeyStroke.getKeyStroke("control C"));
        clearItem.addActionListener(e -> canvas.clearCanvas());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(clearItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Tools menu
        JMenu toolsMenu = new JMenu("Tools");
        
        JMenuItem penItem = new JMenuItem("Pen");
        penItem.addActionListener(e -> selectTool(DrawingEvent.EventType.PEN));
        
        JMenuItem lineItem = new JMenuItem("Line");
        lineItem.addActionListener(e -> selectTool(DrawingEvent.EventType.LINE));
        
        JMenuItem rectItem = new JMenuItem("Rectangle");
        rectItem.addActionListener(e -> selectTool(DrawingEvent.EventType.RECTANGLE));
        
        JMenuItem eraserItem = new JMenuItem("Eraser");
        eraserItem.addActionListener(e -> selectTool(DrawingEvent.EventType.ERASER));
        
        toolsMenu.add(penItem);
        toolsMenu.add(lineItem);
        toolsMenu.add(rectItem);
        toolsMenu.add(eraserItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setSize(1000, 700);
    }
    
    private void setupActions() {
        // Tool button actions
        penButton.addActionListener(e -> selectTool(DrawingEvent.EventType.PEN));
        lineButton.addActionListener(e -> selectTool(DrawingEvent.EventType.LINE));
        rectangleButton.addActionListener(e -> selectTool(DrawingEvent.EventType.RECTANGLE));
        eraserButton.addActionListener(e -> selectTool(DrawingEvent.EventType.ERASER));
        clearButton.addActionListener(e -> canvas.clearCanvas());
        
        // Color button action
        colorButton.addActionListener(e -> selectColor());
        
        // Stroke width change
        strokeWidthCombo.addActionListener(e -> {
            int width = (Integer) strokeWidthCombo.getSelectedItem();
            canvas.setStrokeWidth(width);
        });
        
        // Set initial tool and stroke width
        canvas.setCurrentTool(DrawingEvent.EventType.PEN);
        canvas.setStrokeWidth((Integer) strokeWidthCombo.getSelectedItem());
    }
    
    private void selectTool(DrawingEvent.EventType tool) {
        canvas.setCurrentTool(tool);
        
        // Update button states
        penButton.setSelected(false);
        lineButton.setSelected(false);
        rectangleButton.setSelected(false);
        eraserButton.setSelected(false);
        
        switch (tool) {
            case PEN:
                penButton.setSelected(true);
                statusLabel.setText("Tool: Pen");
                break;
            case LINE:
                lineButton.setSelected(true);
                statusLabel.setText("Tool: Line");
                break;
            case RECTANGLE:
                rectangleButton.setSelected(true);
                statusLabel.setText("Tool: Rectangle");
                break;
            case ERASER:
                eraserButton.setSelected(true);
                statusLabel.setText("Tool: Eraser");
                break;
            case CLEAR:
                // CLEAR is handled separately
                break;
        }
    }
    
    private void selectColor() {
        Color newColor = JColorChooser.showDialog(
            this,
            "Choose Color",
            selectedColor
        );
        
        if (newColor != null) {
            selectedColor = newColor;
            canvas.setCurrentColor(newColor);
            updateColorButton();
            statusLabel.setText("Color: " + String.format("#%02X%02X%02X", 
                newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
        }
    }
    
    private void updateColorButton() {
        colorButton.setBackground(selectedColor);
        colorButton.setForeground(getContrastColor(selectedColor));
    }
    
    private Color getContrastColor(Color color) {
        // Returns black or white depending on which contrasts better
        int brightness = (int) (color.getRed() * 0.299 + 
                               color.getGreen() * 0.587 + 
                               color.getBlue() * 0.114);
        return brightness > 128 ? Color.BLACK : Color.WHITE;
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
            this,
            "Real-Time Collaborative Whiteboard\n\n" +
            "CS9053: Introduction to Java - Fall 2025\n" +
            "Authors: Simranjeet Singh (xs2797), Aryan Yadav (ay3140)",
            "About Whiteboard",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    public DrawCanvas getCanvas() {
        return canvas;
    }
    
    public void setStatus(String status) {
        statusLabel.setText(status);
    }
}

