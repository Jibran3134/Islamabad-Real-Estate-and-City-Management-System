package com.myapp.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    
    private static DatabaseConnection instance;

    private static final Properties CONFIG = loadConfig();
    private static final String SERVER = getConfig("db.server", "localhost\\SQLEXPRESS");
    private static final String DATABASE = getConfig("db.name", "islamabad_real_estate");
    private static final String PORT = getConfig("db.port", "1433");
    private static final String AUTH_MODE = getConfig("db.authMode", "windows");
    private static final String USERNAME = getConfig("db.username", "");
    private static final String PASSWORD = getConfig("db.password", "");
    private static final String URL = buildUrl();
    
    private DatabaseConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            loadWindowsAuthLibrary();
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
        if ("sql".equalsIgnoreCase(AUTH_MODE)) {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        return DriverManager.getConnection(URL);
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

    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("[WARN] Could not load application.properties: " + e.getMessage());
        }
        return properties;
    }

    private static String getConfig(String key, String defaultValue) {
        String value = CONFIG.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String buildUrl() {
        StringBuilder url = new StringBuilder("jdbc:sqlserver://")
            .append(SERVER)
            .append(":")
            .append(PORT)
            .append(";databaseName=")
            .append(DATABASE)
            .append(";encrypt=")
            .append(getConfig("db.encrypt", "true"))
            .append(";trustServerCertificate=")
            .append(getConfig("db.trustServerCertificate", "true"))
            .append(";");

        if ("windows".equalsIgnoreCase(AUTH_MODE)) {
            url.append("integratedSecurity=true;");
        }

        return url.toString();
    }

    private static void loadWindowsAuthLibrary() {
        if (!"windows".equalsIgnoreCase(AUTH_MODE)) {
            return;
        }

        Path authDll = Path.of("mssql-jdbc_auth-12.6.4.x64.dll").toAbsolutePath();
        if (Files.exists(authDll)) {
            System.load(authDll.toString());
        }
    }
}
