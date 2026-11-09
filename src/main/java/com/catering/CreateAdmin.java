package com.catering;

import com.catering.util.DBConnection;
import com.catering.util.PasswordHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateAdmin {
    public static void main(String[] args) {
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "admin");
            ps.setString(2, PasswordHelper.hash("admin123"));
            ps.setString(3, "ADMIN");
            ps.executeUpdate();
            System.out.println("Admin user created. Username: admin, Password: admin123");
        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }
}