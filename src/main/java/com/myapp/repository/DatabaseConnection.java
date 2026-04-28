package com.myapp.repository;

import java.sql.*;

public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    private static final String SERVER   = "DESKTOP-ETHK7B9\\SQLEXPRESS";
    private static final String DATABASE = "islamabad_real_estate";
    private static final String PORT     = "1433";
    
    // Using Windows Authentication (no username/password needed)
    private static final String URL = "jdbc:sqlserver://" + SERVER + ":" + PORT + ";databaseName=" + DATABASE + ";encrypt=true;trustServerCertificate=true;integratedSecurity=true;";
    private static final String USERNAME = null;
    private static final String PASSWORD = null;
    
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
    
    // NON-STATIC method - isko instance se call karna hai
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
            System.out.println("Database connected!");
        }
        return connection;
    }
    
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}