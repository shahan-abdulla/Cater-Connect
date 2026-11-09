package com.catering.dao;

import com.catering.model.Payment;
import com.catering.model.PayrollRow;
import com.catering.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    // One row per staff member who is owed money
    public List<PayrollRow> findPending() throws SQLException {
        List<PayrollRow> list = new ArrayList<>();
        String sql = "SELECT s.staff_id, s.name, s.phone, COUNT(*) AS shifts, SUM(a.wage_amount) AS total "
                   + "FROM assignments a JOIN staff s ON a.staff_id = s.staff_id "
                   + "WHERE a.paid = FALSE AND a.wage_amount > 0 "
                   + "GROUP BY s.staff_id, s.name, s.phone ORDER BY s.name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PayrollRow r = new PayrollRow();
                r.setStaffId(rs.getInt("staff_id"));
                r.setName(rs.getString("name"));
                r.setPhone(rs.getString("phone"));
                r.setShifts(rs.getInt("shifts"));
                r.setPendingAmount(rs.getDouble("total"));
                list.add(r);
            }
        }
        return list;
    }

    // staffId = 0 means everyone
    public List<Payment> findHistory(int staffId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.staff_id, s.name, p.amount, p.pay_date, p.note "
                   + "FROM payments p JOIN staff s ON p.staff_id = s.staff_id "
                   + "WHERE (? = 0 OR p.staff_id = ?) "
                   + "ORDER BY p.pay_date DESC, p.payment_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setInt(2, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setId(rs.getInt("payment_id"));
                    p.setStaffId(rs.getInt("staff_id"));
                    p.setStaffName(rs.getString("name"));
                    p.setAmount(rs.getDouble("amount"));
                    p.setPayDate(rs.getDate("pay_date").toString());
                    p.setNote(rs.getString("note"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    // Pays everything pending for one staff member, all or nothing.
    // Returns the amount paid (0 if there was nothing to pay).
    public double payAllPending(int staffId, String note) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                double total = 0;
                String sumSql = "SELECT COALESCE(SUM(wage_amount), 0) FROM assignments "
                              + "WHERE staff_id = ? AND paid = FALSE AND wage_amount > 0 FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(sumSql)) {
                    ps.setInt(1, staffId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        total = rs.getDouble(1);
                    }
                }
                if (total <= 0) {
                    con.rollback();
                    return 0;
                }

                String insSql = "INSERT INTO payments(staff_id, amount, pay_date, note) VALUES(?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(insSql)) {
                    ps.setInt(1, staffId);
                    ps.setDouble(2, total);
                    ps.setDate(3, Date.valueOf(java.time.LocalDate.now()));
                    ps.setString(4, note);
                    ps.executeUpdate();
                }

                String updSql = "UPDATE assignments SET paid = TRUE "
                              + "WHERE staff_id = ? AND paid = FALSE AND wage_amount > 0";
                try (PreparedStatement ps = con.prepareStatement(updSql)) {
                    ps.setInt(1, staffId);
                    ps.executeUpdate();
                }

                con.commit();
                return total;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
}