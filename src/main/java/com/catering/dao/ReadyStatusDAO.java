// Put in: com.catering.dao   (new file)
package com.catering.dao;

import com.catering.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReadyStatusDAO {

    public static class EventItem {
        public final int id, required;
        public final String name, date;

        public EventItem(int id, String name, String date, int required) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.required = required;
        }

        @Override public String toString() { return name + " (" + date + ")"; }
    }

    public static class Row {
        public final String name, phone, attendance, reason;
        public final Timestamp checkInTime, readyAt;
        public final boolean unavailable;

        public Row(String name, String phone, String attendance, Timestamp checkInTime,
                   boolean unavailable, String reason, Timestamp readyAt) {
            this.name = name;
            this.phone = phone;
            this.attendance = attendance;
            this.checkInTime = checkInTime;
            this.unavailable = unavailable;
            this.reason = reason;
            this.readyAt = readyAt;
        }

        public boolean isCheckedIn() {
            return checkInTime != null
                    || "PRESENT".equals(attendance) || "LATE".equals(attendance);
        }

        public boolean isReady() { return readyAt != null && !unavailable; }

        public String status() {
            if (unavailable) return "CANNOT ATTEND";
            if (isCheckedIn()) return attendance;          // PRESENT or LATE
            if ("ABSENT".equals(attendance)) return "ABSENT";
            if (readyAt != null) return "READY";
            return "Not confirmed";
        }
    }

    /** Events, nearest to today first. */
    public List<EventItem> listEvents() throws SQLException {
        String sql = "SELECT event_id, event_name, event_date, staff_required FROM events "
                   + "WHERE status <> 'CANCELLED' "
                   + "ORDER BY ABS(DATEDIFF(event_date, CURDATE())), event_id";
        List<EventItem> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new EventItem(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4)));
            }
        }
        return list;
    }

    public List<Row> listWorkers(int eventId) throws SQLException {
        String sql = "SELECT s.name, s.phone, a.attendance, a.check_in_time, "
                   + "a.unavailable, a.unavailable_reason, a.ready_at "
                   + "FROM assignments a JOIN staff s ON a.staff_id = s.staff_id "
                   + "WHERE a.event_id = ? ORDER BY s.name";
        List<Row> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Row(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getTimestamp(4), rs.getBoolean(5), rs.getString(6),
                            rs.getTimestamp(7)));
                }
            }
        }
        return list;
    }
}