package com.myapp.repository;

import java.sql.*;

public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    
    private static final String SERVER   = "DESKTOP-ETHK7B9\\SQLEXPRESS";
    private static final String DATABASE = "islamabad_real_estate";
    private static final String PORT     = "1433";
    
    // Using Windows Authentication (no username/password needed)
    private static final String URL = "jdbc:sqlserver://" + SERVER + ":" + PORT + ";databaseName=" + DATABASE + ";encrypt=true;trustServerCertificate=true;integratedSecurity=true;";
    
    private DatabaseConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Returns a NEW connection each time.
     * This is safe for try-with-resources usage in repositories.
     * Each repository method opens and closes its own connection.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        return conn;
    }
    
    /**
     * Test method - checks if database is reachable
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("[INFO] Database connected successfully to: " + DATABASE);
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] Database connection failed: " + e.getMessage());
            return false;
        }
    }
}