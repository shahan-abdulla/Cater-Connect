package com.catering.dao;

import com.catering.model.Client;
import com.catering.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public void add(Client c) throws SQLException {
        String sql = "INSERT INTO clients(name, phone, address) VALUES(?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getAddress());
            ps.executeUpdate();
        }
    }

    public void update(Client c) throws SQLException {
        String sql = "UPDATE clients SET name=?, phone=?, address=? WHERE client_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getAddress());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM clients WHERE client_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Pass "" to get everyone
    public List<Client> search(String text) throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE name LIKE ? OR phone LIKE ? ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + text + "%");
            ps.setString(2, "%" + text + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Client c = new Client();
                    c.setId(rs.getInt("client_id"));
                    c.setName(rs.getString("name"));
                    c.setPhone(rs.getString("phone"));
                    c.setAddress(rs.getString("address"));
                    list.add(c);
                }
            }
        }
        return list;
    }
}