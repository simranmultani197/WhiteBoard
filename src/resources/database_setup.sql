-- Database setup script for Collaborative Whiteboard application
-- Run this script to create the necessary database and tables

CREATE DATABASE IF NOT EXISTS whiteboard_db;
USE whiteboard_db;

-- Table to store whiteboard sessions
CREATE TABLE IF NOT EXISTS sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    session_name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table to store drawing events for each session
CREATE TABLE IF NOT EXISTS drawing_events (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    start_x INT NOT NULL,
    start_y INT NOT NULL,
    end_x INT NOT NULL,
    end_y INT NOT NULL,
    color_red INT NOT NULL,
    color_green INT NOT NULL,
    color_blue INT NOT NULL,
    stroke_width INT NOT NULL,
    client_id VARCHAR(255),
    timestamp BIGINT NOT NULL,
    event_order INT NOT NULL,
    FOREIGN KEY (session_id) REFERENCES sessions(session_id) ON DELETE CASCADE,
    INDEX idx_session_order (session_id, event_order)
);

-- Index for faster session lookups
CREATE INDEX IF NOT EXISTS idx_session_name ON sessions(session_name);

