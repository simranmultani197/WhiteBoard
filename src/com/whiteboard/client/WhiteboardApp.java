package com.whiteboard.client;

import javax.swing.*;

import com.whiteboard.client.ui.DrawCanvas;
import com.whiteboard.client.network.NetworkHandler;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main client application entry point.
 * Initializes the GUI and network connection.
 */
public class WhiteboardApp extends JFrame {
    private DrawCanvas canvas;
    private NetworkHandler networkHandler;
    private JButton connectButton;
    private JLabel statusLabel;
    private String currentTool = "PEN";
    private Color currentColor = Color.BLACK;
    private int strokeWidth = 3;

    public WhiteboardApp() {
        setTitle("Collaborative Whiteboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create canvas
        canvas = new DrawCanvas(this);
        add(canvas, BorderLayout.CENTER);

        // Create toolbar
        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);

        // Create status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);

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

        // Show connection dialog
        showConnectionDialog();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(240, 240, 240));

        // Connect button
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> showConnectionDialog());
        toolbar.add(connectButton);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Drawing tools
        JButton penBtn = new JButton("Pen");
        penBtn.addActionListener(e -> setTool("PEN"));
        toolbar.add(penBtn);

        JButton lineBtn = new JButton("Line");
        lineBtn.addActionListener(e -> setTool("LINE"));
        toolbar.add(lineBtn);

        JButton rectBtn = new JButton("Rectangle");
        rectBtn.addActionListener(e -> setTool("RECTANGLE"));
        toolbar.add(rectBtn);

        JButton eraserBtn = new JButton("Eraser");
        eraserBtn.addActionListener(e -> setTool("ERASER"));
        toolbar.add(eraserBtn);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Color picker
        JButton colorBtn = new JButton("Color");
        colorBtn.setBackground(currentColor);
        colorBtn.setOpaque(true);
        colorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
                colorBtn.setBackground(currentColor);
            }
        });
        toolbar.add(colorBtn);

        // Stroke width
        toolbar.add(new JLabel("  Width: "));
        JComboBox<Integer> widthCombo = new JComboBox<>(new Integer[]{1, 3, 5, 8, 12});
        widthCombo.setSelectedItem(strokeWidth);
        widthCombo.addActionListener(e -> strokeWidth = (Integer) widthCombo.getSelectedItem());
        toolbar.add(widthCombo);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Clear button
        JButton clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Clear the entire whiteboard for all users?",
                    "Confirm Clear",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                canvas.clear();
                if (networkHandler != null && networkHandler.isConnected()) {
                    networkHandler.sendDrawingEvent("CLEAR", 0, 0, 0, 0);
                }
            }
        });
        toolbar.add(clearBtn);

        return toolbar;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(new Color(220, 220, 220));
        statusLabel = new JLabel("Not connected");
        statusBar.add(statusLabel);
        return statusBar;
    }

    private void showConnectionDialog() {
        JTextField serverField = new JTextField("localhost", 15);
        JTextField portField = new JTextField("8000", 5);
        JTextField sessionField = new JTextField("default", 15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Server:"));
        panel.add(serverField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(new JLabel("Session:"));
        panel.add(sessionField);

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

            if (server.isEmpty() || session.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Server and Session cannot be empty!");
                return;
            }

            connectToServer(server, port, session);
        }
    }

    private void connectToServer(String server, int port, String session) {
        try {
            networkHandler = new NetworkHandler(server, port, session, this);
            new Thread(networkHandler).start();
            statusLabel.setText("Connected to session: " + session);
            connectButton.setText("Reconnect");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to connect: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Connection failed");
        }
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
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WhiteboardApp::new);
    }

}

