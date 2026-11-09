package com.catering.dao;

import com.catering.model.Staff;
import com.catering.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public void add(Staff s) throws SQLException {
        String sql = "INSERT INTO staff(name, phone, address, age, role, daily_wage) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setString(3, s.getAddress());
            ps.setInt(4, s.getAge());
            ps.setString(5, s.getRole());
            ps.setDouble(6, s.getDailyWage());
            ps.executeUpdate();
        }
    }

    public void update(Staff s) throws SQLException {
        String sql = "UPDATE staff SET name=?, phone=?, address=?, age=?, role=?, daily_wage=? WHERE staff_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setString(3, s.getAddress());
            ps.setInt(4, s.getAge());
            ps.setString(5, s.getRole());
            ps.setDouble(6, s.getDailyWage());
            ps.setInt(7, s.getId());
            ps.executeUpdate();
        }
    }

    // Soft delete: keeps history, just hides the person
    public void deactivate(int id) throws SQLException {
        String sql = "UPDATE staff SET active = FALSE WHERE staff_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Pass "" to get everyone
    public List<Staff> search(String text) throws SQLException {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff WHERE active = TRUE AND (name LIKE ? OR phone LIKE ?) ORDER BY name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + text + "%");
            ps.setString(2, "%" + text + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Staff s = new Staff();
                    s.setId(rs.getInt("staff_id"));
                    s.setName(rs.getString("name"));
                    s.setPhone(rs.getString("phone"));
                    s.setAddress(rs.getString("address"));
                    s.setAge(rs.getInt("age"));
                    s.setRole(rs.getString("role"));
                    s.setDailyWage(rs.getDouble("daily_wage"));
                    list.add(s);
                }
            }
        }
        return list;
    }
}