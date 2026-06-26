package scratch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class InspectUsers {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:app-backend/vuavuive_v2.db";
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("--- USERS TABLE ---");
                String query = "SELECT id, full_name, email, phone, role, password_hash, is_active FROM users";
                try (ResultSet rs = stmt.executeQuery(query)) {
                    while (rs.next()) {
                        System.out.printf("ID: %s | Name: %s | Email: %s | Phone: %s | Role: %s | PasswordHash: %s | Active: %b%n",
                                rs.getString("id"),
                                rs.getString("full_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("role"),
                                rs.getString("password_hash"),
                                rs.getBoolean("is_active"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
