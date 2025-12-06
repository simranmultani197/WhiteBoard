package com.whiteboard.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.whiteboard.common.util.Constants;

/**
 * Dialog for entering server address and session name.
 * Displayed when the client application starts.
 */
public class ConnectionDialog extends JDialog {
    
    private JTextField serverAddressField;
    private JTextField sessionNameField;
    private JButton connectButton;
    private JButton cancelButton;
    private boolean connected;
    
    public ConnectionDialog(Frame parent) {
        super(parent, "Connect to Whiteboard Server", true);
        this.connected = false;
        initializeComponents();
        setupLayout();
        setupActions();
    }
    
    private void initializeComponents() {
        serverAddressField = new JTextField(Constants.DEFAULT_SERVER_HOST + ":" + Constants.DEFAULT_SERVER_PORT, 20);
        sessionNameField = new JTextField("Session1", 20);
        connectButton = new JButton("Connect");
        cancelButton = new JButton("Cancel");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Main panel with fields
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Server address
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Server Address:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(serverAddressField, gbc);
        
        // Session name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Session Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(sessionNameField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(connectButton);
        buttonPanel.add(cancelButton);
        
        // Add to dialog
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void setupActions() {
        connectButton.addActionListener(e -> {
            if (validateInput()) {
                connected = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> {
            connected = false;
            dispose();
        });
        
        // Enter key submits
        getRootPane().setDefaultButton(connectButton);
    }
    
    private boolean validateInput() {
        String serverAddress = serverAddressField.getText().trim();
        String sessionName = sessionNameField.getText().trim();
        
        if (serverAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter server address.", 
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (sessionName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter session name.", 
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    public String getServerAddress() {
        return serverAddressField.getText().trim();
    }
    
    public String getSessionName() {
        return sessionNameField.getText().trim();
    }
    
    public boolean isConnected() {
        return connected;
    }
}

