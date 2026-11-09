package com.catering.dao;

import com.catering.model.User;
import com.catering.util.DBConnection;
import com.catering.util.PasswordHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Returns the User if login is correct, otherwise null.
    // A staff login stops working once that staff member is deactivated.
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.password_hash, u.role, u.staff_id "
                   + "FROM users u LEFT JOIN staff s ON u.staff_id = s.staff_id "
                   + "WHERE u.username = ? AND (u.staff_id IS NULL OR s.active = TRUE)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (PasswordHelper.verify(password, hash)) {
                        return new User(rs.getInt("user_id"), rs.getString("username"),
                                        rs.getString("role"), rs.getInt("staff_id"));
                    }
                }
            }
        }
        return null;
    }

    public void createStaffLogin(int staffId, String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, role, staff_id) VALUES(?,?,'STAFF',?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordHelper.hash(password));
            ps.setInt(3, staffId);
            ps.executeUpdate();
        }
    }
}