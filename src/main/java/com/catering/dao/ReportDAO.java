package com.catering.dao;

import com.catering.model.ReportResult;
import com.catering.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public ReportResult staffEarnings() throws SQLException {
        return run(
            "SELECT s.name AS 'Name', s.phone AS 'Phone', "
          + " COALESCE(SUM(a.attendance IN ('PRESENT','LATE')), 0) AS 'Shifts worked', "
          + " COALESCE(SUM(a.wage_amount), 0) AS 'Total earned', "
          + " COALESCE(SUM(CASE WHEN a.paid = TRUE THEN a.wage_amount ELSE 0 END), 0) AS 'Paid', "
          + " COALESCE(SUM(CASE WHEN a.paid = FALSE THEN a.wage_amount ELSE 0 END), 0) AS 'Pending' "
          + "FROM staff s LEFT JOIN assignments a ON s.staff_id = a.staff_id "
          + "WHERE s.active = TRUE "
          + "GROUP BY s.staff_id, s.name, s.phone ORDER BY s.name");
    }

    public ReportResult eventStaffing() throws SQLException {
        return run(
            "SELECT e.event_name AS 'Event', c.name AS 'Client', e.event_date AS 'Date', "
          + " e.status AS 'Status', e.staff_required AS 'Needed', "
          + " COUNT(a.assignment_id) AS 'Assigned', "
          + " COALESCE(SUM(a.attendance = 'PRESENT'), 0) AS 'Present', "
          + " COALESCE(SUM(a.attendance = 'LATE'), 0) AS 'Late', "
          + " COALESCE(SUM(a.attendance = 'ABSENT'), 0) AS 'Absent', "
          + " COALESCE(SUM(a.wage_amount), 0) AS 'Total wages' "
          + "FROM events e JOIN clients c ON e.client_id = c.client_id "
          + "LEFT JOIN assignments a ON e.event_id = a.event_id "
          + "GROUP BY e.event_id, e.event_name, c.name, e.event_date, e.status, e.staff_required "
          + "ORDER BY e.event_date DESC");
    }

    public ReportResult pendingPayments() throws SQLException {
        return run(
            "SELECT s.name AS 'Name', s.phone AS 'Phone', COUNT(*) AS 'Unpaid shifts', "
          + " SUM(a.wage_amount) AS 'Amount owed' "
          + "FROM assignments a JOIN staff s ON a.staff_id = s.staff_id "
          + "WHERE a.paid = FALSE AND a.wage_amount > 0 "
          + "GROUP BY s.staff_id, s.name, s.phone ORDER BY s.name");
    }

    // Runs any query and uses the column aliases as table headers
    private ReportResult run(String sql) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            List<String> headers = new ArrayList<>();
            for (int i = 1; i <= cols; i++) headers.add(md.getColumnLabel(i));

            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 1; i <= cols; i++) row[i - 1] = rs.getObject(i);
                rows.add(row);
            }
            return new ReportResult(headers, rows);
        }
    }
}