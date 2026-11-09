// Put in: com.catering.dao   (new file)
package com.catering.dao;

import com.catering.util.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Saves manager attendance, but only when the rules allow it:
 *  - the event's reporting time (or start time if none) has passed
 *  - the worker has not checked in themselves
 *  - the row has not been paid
 * The rules are repeated inside the UPDATE statement itself, so they cannot be skipped.
 */
public class AttendanceGuardDAO {

    public enum Result { OK, TOO_EARLY, CHECKED_IN, PAID, NOT_FOUND, CHANGED }

    private static final String OPEN_TIME =
            "TIMESTAMP(e.event_date, COALESCE(e.reporting_time, e.start_time))";

    /** When the manager may start marking attendance for this event. */
    public LocalDateTime opensAt(int eventId) throws SQLException {
        String sql = "SELECT TIMESTAMP(event_date, COALESCE(reporting_time, start_time)) "
                   + "FROM events WHERE event_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Timestamp t = rs.getTimestamp(1);
                return t == null ? null : t.toLocalDateTime();
            }
        }
    }

    public Result mark(int assignmentId, String attendance, double wage) throws SQLException {
        String check = "SELECT a.paid, (a.check_in_time IS NOT NULL), (" + OPEN_TIME + " <= NOW()) "
                     + "FROM assignments a JOIN events e ON a.event_id = e.event_id "
                     + "WHERE a.assignment_id = ?";
        String update = "UPDATE assignments a JOIN events e ON a.event_id = e.event_id "
                      + "SET a.attendance = ?, a.wage_amount = ? "
                      + "WHERE a.assignment_id = ? AND a.paid = FALSE "
                      + "AND a.check_in_time IS NULL AND " + OPEN_TIME + " <= NOW()";

        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(check)) {
                ps.setInt(1, assignmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Result.NOT_FOUND;
                    if (rs.getBoolean(1)) return Result.PAID;
                    if (rs.getBoolean(2)) return Result.CHECKED_IN;
                    if (!rs.getBoolean(3)) return Result.TOO_EARLY;
                }
            }
            try (PreparedStatement ps = c.prepareStatement(update)) {
                ps.setString(1, attendance);
                ps.setDouble(2, wage);
                ps.setInt(3, assignmentId);
                return ps.executeUpdate() > 0 ? Result.OK : Result.CHANGED;
            }
        }
    }
}