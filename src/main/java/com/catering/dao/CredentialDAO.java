// Put in: com.catering.dao   (new file; your UserDAO stays unchanged)
package com.catering.dao;

import com.catering.util.DBConnection;
import java.sql.*;

public class CredentialDAO {

    public boolean hasLogin(int staffId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE staff_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Returns the encrypted password, or null if none was saved. */
    public String getEncrypted(int staffId) throws SQLException {
        String sql = "SELECT password_enc FROM users WHERE staff_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public void saveEncrypted(int staffId, String enc) throws SQLException {
        String sql = "UPDATE users SET password_enc = ? WHERE staff_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, enc);
            ps.setInt(2, staffId);
            ps.executeUpdate();
        }
    }

    /** Sets a new password: BCrypt hash for login, encrypted copy for the admin. */
    public void resetPassword(int staffId, String hash, String enc) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, password_enc = ? WHERE staff_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, enc);
            ps.setInt(3, staffId);
            ps.executeUpdate();
        }
    }
}