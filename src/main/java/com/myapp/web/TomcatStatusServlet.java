package com.myapp.web;

import com.myapp.repository.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(urlPatterns = {"/", "/health"})
public class TomcatStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!doctype html>");
            out.println("<html><head><title>IRMS Tomcat Output</title>");
            out.println("<style>");
            out.println("body{font-family:Arial,sans-serif;margin:40px;background:#f6f7f9;color:#1f2937}");
            out.println("main{max-width:760px;background:#fff;border:1px solid #d1d5db;padding:24px}");
            out.println("h1{font-size:24px;margin:0 0 16px}");
            out.println("table{border-collapse:collapse;width:100%;margin-top:16px}");
            out.println("td,th{border:1px solid #d1d5db;padding:10px;text-align:left}");
            out.println(".ok{color:#047857;font-weight:bold}.error{color:#b91c1c;font-weight:bold}");
            out.println("</style></head><body><main>");
            out.println("<h1>Islamabad Real Estate and City Management System</h1>");
            renderDatabaseStatus(out);
            out.println("</main></body></html>");
        }
    }

    private void renderDatabaseStatus(PrintWriter out) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            out.println("<p class=\"ok\">Tomcat is running and SQL Server is connected.</p>");
            out.println("<table><tr><th>Table</th><th>Rows</th></tr>");
            printCountRow(out, statement, "Users");
            printCountRow(out, statement, "Sector");
            printCountRow(out, statement, "Property");
            out.println("</table>");
        } catch (SQLException e) {
            out.println("<p class=\"error\">Database connection failed.</p>");
            out.println("<p>" + escapeHtml(e.getMessage()) + "</p>");
        }
    }

    private void printCountRow(PrintWriter out, Statement statement, String tableName) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            out.println("<tr><td>" + tableName + "</td><td>" + count + "</td></tr>");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
