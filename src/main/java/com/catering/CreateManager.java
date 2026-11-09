package com.catering;

import com.catering.util.DBConnection;
import com.catering.util.PasswordHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateManager {
    public static void main(String[] args) {
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "manager");
            ps.setString(2, PasswordHelper.hash("manager123"));
            ps.setString(3, "MANAGER");
            ps.executeUpdate();
            System.out.println("Manager created. Username: manager, Password: manager123");
        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }
}