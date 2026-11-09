// Put in: com.catering.dao   (replaces the previous UnavailabilityDAO.java)
package com.catering.dao;

import com.catering.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnavailabilityDAO {

    public static class Report {
        public final int assignmentId, eventId;
        public final String staffName, phone, eventName, eventDate, reason;

        public Report(int assignmentId, int eventId, String staffName, String phone,
                      String eventName, String eventDate, String reason) {
            this.assignmentId = assignmentId;
            this.eventId = eventId;
            this.staffName = staffName;
            this.phone = phone;
            this.eventName = eventName;
            this.eventDate = eventDate;
            this.reason = reason;
        }
    }

    /** Headcount for one event. */
    public static class EventStats {
        public final int staffRequired, assigned, checkedIn, unavailable, ready;

        public EventStats(int staffRequired, int assigned, int checkedIn,
                          int unavailable, int ready) {
            this.staffRequired = staffRequired;
            this.assigned = assigned;
            this.checkedIn = checkedIn;
            this.unavailable = unavailable;
            this.ready = ready;
        }

        /** How many more workers must be assigned to replace the unavailable ones. */
        public int replacementsNeeded() {
            return Math.max(0, staffRequired - (assigned - unavailable));
        }
    }

    /** Reports the manager has not been alerted about yet. */
    public List<Report> getUnseen() throws SQLException {
        String sql = "SELECT a.assignment_id, a.event_id, s.name, s.phone, e.event_name, "
                   + "e.event_date, a.unavailable_reason "
                   + "FROM assignments a "
                   + "JOIN staff s ON a.staff_id = s.staff_id "
                   + "JOIN events e ON a.event_id = e.event_id "
                   + "WHERE a.unavailable = TRUE AND a.manager_seen = FALSE "
                   + "ORDER BY e.event_date, e.event_id, a.reported_at";
        List<Report> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Report(
                        rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7)));
            }
        }
        return list;
    }

    public EventStats getEventStats(int eventId) throws SQLException {
        String sql = "SELECT e.staff_required, COUNT(a.assignment_id), "
                   + "COALESCE(SUM(a.check_in_time IS NOT NULL "
                   + "            OR a.attendance IN ('PRESENT','LATE')), 0), "
                   + "COALESCE(SUM(a.unavailable), 0), "
                   + "COALESCE(SUM(a.ready_at IS NOT NULL AND a.unavailable = FALSE), 0) "
                   + "FROM events e LEFT JOIN assignments a ON a.event_id = e.event_id "
                   + "WHERE e.event_id = ? GROUP BY e.event_id, e.staff_required";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new EventStats(rs.getInt(1), rs.getInt(2), rs.getInt(3),
                            rs.getInt(4), rs.getInt(5));
                }
            }
        }
        return new EventStats(0, 0, 0, 0, 0);
    }

    public void markSeen(List<Report> reports) throws SQLException {
        String sql = "UPDATE assignments SET manager_seen = TRUE WHERE assignment_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (Report r : reports) {
                ps.setInt(1, r.assignmentId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}