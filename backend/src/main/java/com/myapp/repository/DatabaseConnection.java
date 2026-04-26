package com.myapp.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class to manage the SQL Server database connection.
 * Connects to the islamabad_real_estate database on JIBRAN-PC\SQLEXPRESS.
 *
 * Uses Windows Authentication by default.
 * If you want SQL Server Authentication instead, use the overloaded getInstance(username, password).
 */
public class DatabaseConnection {

    // ── Connection Configuration ──────────────────────────────────────
    private static final String SERVER   = "JIBRAN-PC\\SQLEXPRESS";
    private static final String DATABASE = "islamabad_real_estate";
    private static final String PORT     = "1433";

    // JDBC URL for SQL Server with Windows Authentication
    private static final String URL =
            "jdbc:sqlserver://" + SERVER + ":" + PORT
            + ";databaseName=" + DATABASE
            + ";encrypt=true"
            + ";trustServerCertificate=true"
            + ";integratedSecurity=true";

    // ── Singleton Instance ────────────────────────────────────────────
    private static DatabaseConnection instance;
    private Connection connection;

    // ── Private Constructor ───────────────────────────────────────────
    private DatabaseConnection() throws SQLException {
        try {
            // Load the SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            this.connection = DriverManager.getConnection(URL);
            System.out.println("✅ Database connection established successfully!");
            System.out.println("   Server:   " + SERVER);
            System.out.println("   Database: " + DATABASE);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found. " +
                    "Make sure mssql-jdbc-*.jar is added to the project libraries.", e);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    // ── Constructor with SQL Server Authentication ────────────────────
    private DatabaseConnection(String username, String password) throws SQLException {
        try {
            String sqlAuthUrl =
                    "jdbc:sqlserver://" + SERVER + ":" + PORT
                    + ";databaseName=" + DATABASE
                    + ";encrypt=true"
                    + ";trustServerCertificate=true";

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            this.connection = DriverManager.getConnection(sqlAuthUrl, username, password);
            System.out.println("✅ Database connection established successfully!");
            System.out.println("   Server:   " + SERVER);
            System.out.println("   Database: " + DATABASE);
            System.out.println("   User:     " + username);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found. " +
                    "Make sure mssql-jdbc-*.jar is added to the project libraries.", e);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    // ── Get Instance (Windows Authentication) ─────────────────────────
    /**
     * Returns the singleton DatabaseConnection instance using Windows Authentication.
     * Creates a new connection if one doesn't exist or if the existing one is closed.
     */
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ── Get Instance (SQL Server Authentication) ──────────────────────
    /**
     * Returns the singleton DatabaseConnection instance using SQL Server Authentication.
     * Creates a new connection if one doesn't exist or if the existing one is closed.
     */
    public static synchronized DatabaseConnection getInstance(String username, String password) throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection(username, password);
        }
        return instance;
    }

    // ── Get the JDBC Connection ───────────────────────────────────────
    /**
     * Returns the underlying JDBC Connection object.
     */
    public Connection getConnection() {
        return connection;
    }

    // ── Close Connection ──────────────────────────────────────────────
    /**
     * Closes the database connection and resets the singleton instance.
     */
    public static synchronized void closeConnection() {
        if (instance != null) {
            try {
                if (instance.connection != null && !instance.connection.isClosed()) {
                    instance.connection.close();
                    System.out.println("🔒 Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            } finally {
                instance = null;
            }
        }
    }
}
