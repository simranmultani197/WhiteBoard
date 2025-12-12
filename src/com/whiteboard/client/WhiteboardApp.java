package com.whiteboard.client;

import javax.swing.*;
import com.whiteboard.client.ui.DrawCanvas;
import com.whiteboard.client.network.NetworkHandler;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main client application entry point.
 * Initializes the GUI and network connection.
 */
public class WhiteboardApp extends JFrame {
    private DrawCanvas canvas;
    private NetworkHandler networkHandler;
    private String currentTool = "PEN";
    private Color currentColor = Color.BLACK;
    private int strokeWidth = 3;
    private String currentUser = "HostClient_" + (System.currentTimeMillis() % 10000);
    private List<String> connectedUsers = new ArrayList<>();
    private DefaultListModel<String> usersListModel;
    private JButton selectedToolButton;
    private JButton selectionButton;
    private JLabel coordinateLabel;
    
    // Dark theme color scheme
    private static final Color PRIMARY_BG = new Color(45, 45, 48);
    private static final Color SECONDARY_BG = new Color(37, 37, 38);
    private static final Color PANEL_BG = new Color(30, 30, 30);
    private static final Color ACCENT_COLOR = new Color(60, 60, 60);
    private static final Color BUTTON_BG = new Color(50, 50, 50);
    private static final Color TEXT_DARK = new Color(220, 220, 220);
    private static final Color WHITE = Color.WHITE;
    private static final Color SELECTED_BG = new Color(80, 80, 80);

    public WhiteboardApp() {
        setTitle("Whiteboard - " + currentUser);
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PRIMARY_BG);

        // Initialize users list
        connectedUsers.add(currentUser);
        connectedUsers.add("User_3112");
        connectedUsers.add("Designer_89");

        // Create main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(PRIMARY_BG);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Top panel with buttons and title
        JPanel topPanel = createTopPanel();
        mainContainer.add(topPanel, BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(PRIMARY_BG);

        // Center canvas
        canvas = new DrawCanvas(this);
        canvas.setBackground(WHITE);
        canvas.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        JScrollPane canvasScroll = new JScrollPane(canvas);
        canvasScroll.setBorder(null);
        contentPanel.add(canvasScroll, BorderLayout.CENTER);

        // Right users panel
        JPanel rightPanel = createRightUsersPanel();
        contentPanel.add(rightPanel, BorderLayout.EAST);

        mainContainer.add(contentPanel, BorderLayout.CENTER);

        // Bottom panel with chat
        JPanel bottomPanel = createBottomPanel();
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);

        add(mainContainer);

        // Window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (networkHandler != null && networkHandler.isConnected()) {
                    networkHandler.disconnect();
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PANEL_BG);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_COLOR));

        // File buttons panel
        JPanel fileButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        fileButtonsPanel.setBackground(PANEL_BG);
        fileButtonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton newBtn = createFileButton("New");
        newBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Create new whiteboard?", "New", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                canvas.clear();
            }
        });

        JButton openBtn = createFileButton("Open");
        JButton saveBtn = createFileButton("Save");
        JButton saveAsBtn = createFileButton("Save As");

        fileButtonsPanel.add(newBtn);
        fileButtonsPanel.add(openBtn);
        fileButtonsPanel.add(saveBtn);
        fileButtonsPanel.add(saveAsBtn);

        topPanel.add(fileButtonsPanel, BorderLayout.NORTH);

        // Toolbar with tools
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        toolbar.setBackground(SECONDARY_BG);
        toolbar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Selection section
        JLabel selectionLabel = new JLabel("Selection");
        selectionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        selectionLabel.setForeground(TEXT_DARK);
        toolbar.add(selectionLabel);
        selectionButton = new JButton("✏️");
        selectionButton.setPreferredSize(new Dimension(35, 35));
        selectionButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        selectionButton.setToolTipText("Current Tool: Pen");
        selectionButton.setBackground(BUTTON_BG);
        selectionButton.setForeground(TEXT_DARK);
        selectionButton.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        selectionButton.setFocusPainted(false);
        selectionButton.setEnabled(false);
        toolbar.add(selectionButton);
        toolbar.add(createVerticalSeparator());

        // Tools section
        JLabel toolsLabel = new JLabel("Tools");
        toolsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        toolsLabel.setForeground(TEXT_DARK);
        toolbar.add(toolsLabel);
        
        JButton penBtn = createToolbarButton("✏️", "Pen", "PEN");
        JButton brushBtn = createToolbarButton("🖌️", "Brush", "PEN");
        JButton eraserBtn = createToolbarButton("🧼", "Eraser", "ERASER");
        
        toolbar.add(penBtn);
        toolbar.add(brushBtn);
        toolbar.add(eraserBtn);
        
        // Set Pen as default selected tool
        selectedToolButton = penBtn;
        penBtn.setBackground(SELECTED_BG);
        currentTool = "PEN";
        
        toolbar.add(createVerticalSeparator());

        // Shapes section
        JLabel shapesLabel = new JLabel("Shapes");
        shapesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        shapesLabel.setForeground(TEXT_DARK);
        toolbar.add(shapesLabel);
        toolbar.add(createToolbarButton("—", "Line", "LINE"));
        toolbar.add(createToolbarButton("□", "Rectangle", "RECTANGLE"));
        toolbar.add(createToolbarButton("○", "Circle", "CIRCLE"));
        toolbar.add(createToolbarButton("△", "Triangle", "TRIANGLE"));
        toolbar.add(createVerticalSeparator());

        // Colors section
        JLabel colorsLabel = new JLabel("Colors");
        colorsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        colorsLabel.setForeground(TEXT_DARK);
        toolbar.add(colorsLabel);
        
        // Color picker button
        JButton colorPickerBtn = new JButton("🎨");
        colorPickerBtn.setPreferredSize(new Dimension(35, 35));
        colorPickerBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        colorPickerBtn.setToolTipText("Choose Custom Color");
        colorPickerBtn.setBackground(BUTTON_BG);
        colorPickerBtn.setForeground(TEXT_DARK);
        colorPickerBtn.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        colorPickerBtn.setFocusPainted(false);
        colorPickerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        colorPickerBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
            }
        });
        toolbar.add(colorPickerBtn);
        
        // Color palette
        JPanel colorPalette = new JPanel(new GridLayout(2, 8, 2, 2));
        colorPalette.setBackground(SECONDARY_BG);
        Color[] colors = {
            Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.RED,
            new Color(200, 50, 50), new Color(255, 150, 0), Color.YELLOW, Color.GREEN,
            Color.CYAN, Color.BLUE, new Color(128, 0, 128), Color.MAGENTA,
            Color.WHITE, Color.LIGHT_GRAY, new Color(150, 75, 0), new Color(255, 200, 200)
        };
        for (Color c : colors) {
            JButton colorBtn = new JButton();
            colorBtn.setPreferredSize(new Dimension(18, 18));
            colorBtn.setBackground(c);
            colorBtn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            colorBtn.setFocusPainted(false);
            colorBtn.addActionListener(e -> currentColor = c);
            colorPalette.add(colorBtn);
        }
        toolbar.add(colorPalette);

        topPanel.add(toolbar, BorderLayout.CENTER);

        return topPanel;
    }



    private JButton createToolbarButton(String icon, String tooltip, String tool) {
        JButton btn = new JButton(icon);
        btn.setPreferredSize(new Dimension(35, 35));
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btn.setToolTipText(tooltip);
        btn.setBackground(BUTTON_BG);
        btn.setForeground(TEXT_DARK);
        btn.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedToolButton != btn) {
                    btn.setBackground(ACCENT_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedToolButton != btn) {
                    btn.setBackground(BUTTON_BG);
                }
            }
        });

        btn.addActionListener(e -> {
            if (selectedToolButton != null) {
                selectedToolButton.setBackground(BUTTON_BG);
            }
            selectedToolButton = btn;
            btn.setBackground(SELECTED_BG);
            setTool(tool);
            
            // Update selection button icon and tooltip
            if (selectionButton != null) {
                selectionButton.setText(icon);
                selectionButton.setToolTipText("Current Tool: " + tooltip);
            }
        });

        return btn;
    }

    private Component createVerticalSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 30));
        sep.setForeground(ACCENT_COLOR);
        return sep;
    }

    private JButton createFileButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(BUTTON_BG);
        btn.setForeground(TEXT_DARK);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BUTTON_BG);
            }
        });

        return btn;
    }

    private JPanel createRightUsersPanel() {
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBackground(SECONDARY_BG);
        usersPanel.setPreferredSize(new Dimension(200, 0));
        usersPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ACCENT_COLOR));

        // Title
        JLabel titleLabel = new JLabel("Online Users");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        usersPanel.add(titleLabel, BorderLayout.NORTH);

        // Users list
        usersListModel = new DefaultListModel<>();
        for (String user : connectedUsers) {
            if (user.equals(currentUser)) {
                usersListModel.addElement(user + " (Current Editor)");
            } else {
                usersListModel.addElement(user);
            }
        }

        JList<String> usersList = new JList<>(usersListModel);
        usersList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usersList.setBackground(PANEL_BG);
        usersList.setForeground(TEXT_DARK);
        usersList.setSelectionBackground(ACCENT_COLOR);
        usersList.setSelectionForeground(WHITE);
        usersList.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JScrollPane usersScroll = new JScrollPane(usersList);
        usersScroll.setBorder(null);
        usersScroll.setBackground(PANEL_BG);
        usersScroll.getViewport().setBackground(PANEL_BG);
        usersPanel.add(usersScroll, BorderLayout.CENTER);

        // User count label
        JLabel countLabel = new JLabel(connectedUsers.size() + " user(s) online");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countLabel.setForeground(TEXT_DARK);
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        countLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
        usersPanel.add(countLabel, BorderLayout.SOUTH);

        return usersPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(PANEL_BG);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT_COLOR));

        // Coordinate bar
        JPanel coordPanel = new JPanel(new BorderLayout());
        coordPanel.setBackground(PANEL_BG);
        coordPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        coordinateLabel = new JLabel("Position: 0 x 0");
        coordinateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        coordinateLabel.setForeground(TEXT_DARK);
        coordPanel.add(coordinateLabel, BorderLayout.WEST);

        JButton exitBtn = new JButton("EXIT");
        exitBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        exitBtn.setBackground(BUTTON_BG);
        exitBtn.setForeground(TEXT_DARK);
        exitBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        exitBtn.setFocusPainted(false);
        exitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> System.exit(0));
        coordPanel.add(exitBtn, BorderLayout.EAST);

        bottomPanel.add(coordPanel, BorderLayout.CENTER);

        // Mouse listener for coordinates
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                coordinateLabel.setText(String.format("Position: %d x %d", e.getX(), e.getY()));
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                coordinateLabel.setText(String.format("Position: %d x %d", e.getX(), e.getY()));
            }
        });

        return bottomPanel;
    }



    public void setTool(String tool) {
        this.currentTool = tool;
    }

    public String getCurrentTool() {
        return currentTool;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public NetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    public DrawCanvas getCanvas() {
        return canvas;
    }

    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            // Status updates can be shown in a status label if needed
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WhiteboardApp::new);
    }

}

