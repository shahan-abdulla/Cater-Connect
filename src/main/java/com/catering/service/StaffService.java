// Put in: com.catering.service   (replaces the old StaffService.java)
package com.catering.service;

import com.catering.model.Staff;
import com.catering.util.DBConnection;
import com.catering.util.PasswordGenerator;
import com.catering.util.PasswordVault;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class StaffService {

    public static class Credentials {
        public final String username;
        public final String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String asMessage() {
            return "Username: " + username + "\nPassword: " + password;
        }
    }

    /** Adds the worker and creates the login in one transaction. */
    public Credentials addStaffWithLogin(Staff s) throws SQLException {
        String staffSql = "INSERT INTO staff(name,phone,address,age,role,daily_wage) "
                        + "VALUES(?,?,?,?,?,?)";
        String userSql = "INSERT INTO users(username, password_hash, password_enc, role, staff_id) "
                       + "VALUES(?, ?, ?, 'STAFF', ?)";

        String password = PasswordGenerator.generate(8);
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String enc = PasswordVault.encrypt(password);

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int staffId;
                try (PreparedStatement ps =
                             c.prepareStatement(staffSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, s.getName());
                    ps.setString(2, s.getPhone());
                    ps.setString(3, s.getAddress());
                    ps.setInt(4, s.getAge());
                    ps.setString(5, s.getRole());
                    ps.setDouble(6, s.getDailyWage());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Could not read the new staff id.");
                        }
                        staffId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(userSql)) {
                    ps.setString(1, s.getPhone());
                    ps.setString(2, hash);
                    ps.setString(3, enc);
                    ps.setInt(4, staffId);
                    ps.executeUpdate();
                }

                c.commit();
                return new Credentials(s.getPhone(), password);

            } catch (SQLException e) {
                c.rollback();
                if (e.getErrorCode() == 1062) {
                    throw new IllegalStateException(
                            "A worker or login with this phone number already exists.");
                }
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}