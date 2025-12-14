package com.whiteboard.client;

import javax.swing.*;
import javax.swing.Icon;
import com.whiteboard.client.ui.DrawCanvas;
import com.whiteboard.client.network.NetworkHandler;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
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
    private DefaultListModel<String> usersListModel;
    private JButton selectedToolButton;
    private JButton selectionButton;
    private JLabel coordinateLabel;
    private String username;

    // Status tracking variables
    private String connectionStatus = "Not Connected";
    private String mousePositionText = "Position: 0 x 0";

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
        setTitle("Digital Whiteboard - " + currentUser);
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PRIMARY_BG);
        username = "User_" + (System.currentTimeMillis() % 10000);
        setTitle("Digital Whiteboard - " + username);

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

        // Trigger connection dialog on startup ---
        SwingUtilities.invokeLater(this::showConnectionDialog);
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
                // --- NETWORK ADDITION: Send Clear Command ---
                if (networkHandler != null && networkHandler.isConnected()) {
                    networkHandler.sendClearEvent();
                }
            }
        });

        JButton openBtn = createFileButton("Open");

        // --- NETWORK ADDITION: Connect Button ---
        JButton connectBtn = createFileButton("Connect");
        connectBtn.addActionListener(e -> showConnectionDialog());

        fileButtonsPanel.add(newBtn);
        fileButtonsPanel.add(openBtn);
        fileButtonsPanel.add(connectBtn);

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
        selectionButton = createIconButton(createSelectionIcon(), "Selection Tool");
        selectionButton.setEnabled(false);
        toolbar.add(selectionButton);
        toolbar.add(createVerticalSeparator());

        // Tools section
        JLabel toolsLabel = new JLabel("Tools");
        toolsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        toolsLabel.setForeground(TEXT_DARK);
        toolbar.add(toolsLabel);

        JButton penBtn = createToolbarButtonWithIcon(createPenIcon(), "Pen", "PEN");
        JButton eraserBtn = createToolbarButtonWithIcon(createEraserIcon(), "Eraser", "ERASER");

        toolbar.add(penBtn);
        toolbar.add(eraserBtn);

        // Set Pen as default selected tool
        selectedToolButton = penBtn;
        penBtn.setBackground(SELECTED_BG);
        currentTool = "PEN";
        selectionButton.setIcon(createPenIcon());

        toolbar.add(createVerticalSeparator());

        // Shapes section
        JLabel shapesLabel = new JLabel("Shapes");
        shapesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        shapesLabel.setForeground(TEXT_DARK);
        toolbar.add(shapesLabel);
        toolbar.add(createToolbarButtonWithIcon(createLineIcon(), "Line", "LINE"));
        toolbar.add(createToolbarButtonWithIcon(createRectangleIcon(), "Rectangle", "RECTANGLE"));
        toolbar.add(createToolbarButtonWithIcon(createCircleIcon(), "Circle", "CIRCLE"));
        toolbar.add(createToolbarButtonWithIcon(createTriangleIcon(), "Triangle", "TRIANGLE"));
        toolbar.add(createVerticalSeparator());

        // Colors section
        JLabel colorsLabel = new JLabel("Colors");
        colorsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        colorsLabel.setForeground(TEXT_DARK);
        toolbar.add(colorsLabel);

        // Color picker button
        JButton colorPickerBtn = createIconButton(createColorPickerIcon(), "Choose Custom Color");
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
                Color.BLACK, new Color(64, 64, 64), Color.GRAY, Color.WHITE,
                Color.RED, new Color(255, 165, 0), Color.YELLOW, new Color(144, 238, 144),
                new Color(173, 216, 230), Color.BLUE, new Color(128, 0, 128), Color.MAGENTA,
                new Color(165, 42, 42), new Color(255, 192, 203), new Color(128, 128, 128), new Color(128, 128, 128)
        };

        JButton[] colorButtons = new JButton[16];
        for (int i = 0; i < colors.length; i++) {
            Color c = colors[i];
            JButton colorBtn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(getBackground());
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                }
            };
            colorBtn.setPreferredSize(new Dimension(20, 20));
            colorBtn.setMinimumSize(new Dimension(20, 20));
            colorBtn.setMaximumSize(new Dimension(20, 20));
            colorBtn.setBackground(c);
            colorBtn.setOpaque(true);
            colorBtn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            colorBtn.setFocusPainted(false);
            colorBtn.setContentAreaFilled(true);
            final int index = i;
            colorBtn.addActionListener(e -> {
                currentColor = c;
                for (JButton btn : colorButtons) {
                    if (btn != null) btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
                }
                if (index < colorButtons.length && colorButtons[index] != null) {
                    colorButtons[index].setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
                }
            });
            colorButtons[i] = colorBtn;
            colorPalette.add(colorBtn);
        }
        colorButtons[8].setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        currentColor = colors[8];

        toolbar.add(colorPalette);

        topPanel.add(toolbar, BorderLayout.CENTER);

        return topPanel;
    }

    // --- NETWORK METHODS START ---

    private void showConnectionDialog() {
        JTextField serverField = new JTextField("localhost", 15);
        JTextField portField = new JTextField("8000", 5);
        JTextField sessionField = new JTextField("default", 15);
        JTextField usernameField = new JTextField(username, 15);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Server:"));
        panel.add(serverField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(new JLabel("Session:"));
        panel.add(sessionField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Connect to Whiteboard Server", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String server = serverField.getText().trim();
            int port;
            try {
                port = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid port number!");
                return;
            }
            String session = sessionField.getText().trim();
            String enteredUsername = usernameField.getText().trim();

            if (server.isEmpty() || session.isEmpty() || enteredUsername.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!");
                return;
            }

            username = enteredUsername;
            setTitle("Digital Whiteboard - " + username);
            connectToServer(server, port, session);
        }
    }

    private void connectToServer(String server, int port, String session) {
        try {
            networkHandler = new NetworkHandler(server, port, session, username,this);
            new Thread(networkHandler).start();
            updateStatus("Connected to session: " + session);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to connect: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            updateStatus("Connection failed");
        }
    }
    // --- NETWORK METHODS END ---

    public void updateUserList(String[] users) {
        System.out.println("Received user list: " + Arrays.toString(users));
        SwingUtilities.invokeLater(() -> {
            usersListModel.clear();
            for (String user : users) {
                System.out.println("Adding user: " + user);
                if (user.equals(username)) {
                    usersListModel.addElement(user + " (You)");
                } else {
                    usersListModel.addElement(user);
                }
            }
            System.out.println("Total users in list: " + usersListModel.size());
            updateUserCount();
        });
    }

    public void addUser(String user) {
        System.out.println("Adding new user: " + user);
        SwingUtilities.invokeLater(() -> {
            if (user.equals(username)) {
                return;
            } else {
                usersListModel.addElement(user);
            }
            updateUserCount();
        });
    }

    public void removeUser(String user) {
        SwingUtilities.invokeLater(() -> {
            String userToRemove = user.equals(username) ? user + " (You)" : user;
            usersListModel.removeElement(userToRemove);
            updateUserCount();
        });
    }

    private void updateUserCount() {
    }

    // Icon creation methods (Keeping all your custom icons)
    private Icon createPenIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TEXT_DARK);
                g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x + 8, y + 20, x + 22, y + 8);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(x + 22, y + 8, x + 25, y + 5);
                g2d.dispose();
            }
            @Override
            public int getIconWidth() { return 32; }
            @Override
            public int getIconHeight() { return 32; }
        };
    }

    private Icon createEraserIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(180, 180, 180));
                g2d.fillRect(x + 8, y + 10, 16, 10);
                g2d.setColor(TEXT_DARK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(x + 8, y + 10, 16, 10);
                g2d.setColor(new Color(150, 100, 50));
                g2d.fillRect(x + 20, y + 10, 3, 10);
                g2d.dispose();
            }
            @Override
            public int getIconWidth() { return 32; }
            @Override
            public int getIconHeight() { return 32; }
        };
    }

    private Icon createLineIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TEXT_DARK);
                g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x + 6, y + 16, x + 26, y + 16);
                g2d.dispose();
            }
            @Override
            public int getIconWidth() { return 32; }
            @Override
            public int getIconHeight() { return 32; }
        };
    }

    private Icon createRectangleIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TEXT_DARK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(x + 8, y + 8, 16, 16);
                g2d.dispose();
            }
            @Override
            public int getIconWidth() { return 32; }
            @Override
            public int getIconHeight() { return 32; }
        };
    }

    private Icon createCircleIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TEXT_DARK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(x + 8, y + 8, 16, 16);
                g2d.dispose();
            }
            @Override
            public int getIconWidth() { return 32; }
            @Override
            public int getIconHeight() { return 32; }
        };
    }

    private Icon createTriangleIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TEXT_DARK);
                g2d.setStroke(new BasicStroke(2));
                int[] xPoints = {x + 16, x + 8, x + 24};
                int[] yPoints = {y + 8, y + 22, y + 22};
                g2d.drawPolygon(xPoints, yPoints, 3);
                g2d.dispose();
            }
            @Override
            public int getIconWidth() { return 32; }
            @Override
            public int getIconHeight() { return 32; }
        };
    }

    private Icon createSelectionIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TEXT_DARK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(x + 8, y + 8, x + 22, y + 22);
                g2d.drawLine(x + 22, y + 22, x + 18, y + 22);
                g2d.drawLine(x + 22, y + 22, x + 22, y + 18);
                g2d.dispose();
            }
            @Override
            public int getIconWidth() { return 32; }
            @Override
            public int getIconHeight() { return 32; }
        };
    }

    private Icon createColorPickerIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.RED);
                g2d.fillOval(x + 8, y + 8, 8, 8);
                g2d.setColor(Color.GREEN);
                g2d.fillOval(x + 18, y + 8, 8, 8);
                g2d.setColor(Color.BLUE);
                g2d.fillOval(x + 13, y + 16, 8, 8);
                g2d.setColor(TEXT_DARK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(x + 8, y + 8, 8, 8);
                g2d.drawOval(x + 18, y + 8, 8, 8);
                g2d.drawOval(x + 13, y + 16, 8, 8);
            }
            @Override
            public int getIconWidth() { return 32; }
            @Override
            public int getIconHeight() { return 32; }
        };
    }

    private JButton createIconButton(Icon icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setPreferredSize(new Dimension(35, 35));
        btn.setToolTipText(tooltip);
        btn.setBackground(BUTTON_BG);
        btn.setForeground(TEXT_DARK);
        btn.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createToolbarButtonWithIcon(Icon icon, String tooltip, String tool) {
        JButton btn = createIconButton(icon, tooltip);

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

            if (selectionButton != null) {
                selectionButton.setIcon(icon);
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

        JLabel titleLabel = new JLabel("Online Users");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        usersPanel.add(titleLabel, BorderLayout.NORTH);

        usersListModel = new DefaultListModel<>();
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

        JLabel countLabel = new JLabel("0 user(s) online");
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

        JPanel coordPanel = new JPanel(new BorderLayout());
        coordPanel.setBackground(PANEL_BG);
        coordPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Initial label state
        coordinateLabel = new JLabel(connectionStatus + " | " + mousePositionText);
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

        // Mouse listener for coordinates (Updated to preserve status)
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePositionText = String.format("Position: %d x %d", e.getX(), e.getY());
                updateCombinedStatusLabel();
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                mousePositionText = String.format("Position: %d x %d", e.getX(), e.getY());
                updateCombinedStatusLabel();
            }
        });

        return bottomPanel;
    }

    // Helper to keep status and coords cleanly separated
    private void updateCombinedStatusLabel() {
        coordinateLabel.setText(connectionStatus + " | " + mousePositionText);
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

    // --- NETWORK ADDITION: Implementation of updateStatus ---
    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            this.connectionStatus = message;
            updateCombinedStatusLabel();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WhiteboardApp::new);
    }
}