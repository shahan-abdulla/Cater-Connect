package com.catering.dao;

import com.catering.model.Event;
import com.catering.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    private static final String SELECT =
        "SELECT e.*, c.name AS client_name FROM events e JOIN clients c ON e.client_id = c.client_id ";

    public void add(Event e) throws SQLException {
        String sql = "INSERT INTO events(client_id, event_name, venue, event_date, start_time, end_time, "
                   + "reporting_time, staff_required, status) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, e.getClientId());
            ps.setString(2, e.getEventName());
            ps.setString(3, e.getVenue());
            ps.setDate(4, Date.valueOf(e.getEventDate()));
            ps.setTime(5, Time.valueOf(e.getStartTime() + ":00"));
            ps.setTime(6, Time.valueOf(e.getEndTime() + ":00"));
            setReporting(ps, 7, e.getReportingTime());
            ps.setInt(8, e.getStaffRequired());
            ps.setString(9, e.getStatus());
            ps.executeUpdate();
        }
    }

    public void update(Event e) throws SQLException {
        String sql = "UPDATE events SET client_id=?, event_name=?, venue=?, event_date=?, start_time=?, "
                   + "end_time=?, reporting_time=?, staff_required=?, status=? WHERE event_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, e.getClientId());
            ps.setString(2, e.getEventName());
            ps.setString(3, e.getVenue());
            ps.setDate(4, Date.valueOf(e.getEventDate()));
            ps.setTime(5, Time.valueOf(e.getStartTime() + ":00"));
            ps.setTime(6, Time.valueOf(e.getEndTime() + ":00"));
            setReporting(ps, 7, e.getReportingTime());
            ps.setInt(8, e.getStaffRequired());
            ps.setString(9, e.getStatus());
            ps.setInt(10, e.getId());
            ps.executeUpdate();
        }
    }

    private void setReporting(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) ps.setNull(index, Types.TIME);
        else ps.setTime(index, Time.valueOf(value + ":00"));
    }

    // Events are cancelled, not deleted, so assignments and payroll history stay intact
    public void cancel(int id) throws SQLException {
        setStatus(id, "CANCELLED");
    }

    public void setStatus(int id, String status) throws SQLException {
        String sql = "UPDATE events SET status = ? WHERE event_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // Pass "" to get everything
    public List<Event> search(String text) throws SQLException {
        String sql = SELECT + "WHERE e.event_name LIKE ? OR c.name LIKE ? OR e.venue LIKE ? "
                   + "ORDER BY e.event_date DESC, e.start_time";
        String like = "%" + text + "%";
        return query(sql, like, like, like);
    }

    // PLANNED or ONGOING, for the Assignments dropdown
    public List<Event> findOpenEvents() throws SQLException {
        return query(SELECT + "WHERE e.status IN ('PLANNED','ONGOING') ORDER BY e.event_date, e.start_time");
    }

    // Everything except cancelled, for the Attendance dropdown
    public List<Event> findForAttendance() throws SQLException {
        return query(SELECT + "WHERE e.status <> 'CANCELLED' ORDER BY e.event_date DESC, e.start_time");
    }

    private List<Event> query(String sql, String... params) throws SQLException {
        List<Event> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Event map(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("event_id"));
        e.setClientId(rs.getInt("client_id"));
        e.setClientName(rs.getString("client_name"));
        e.setEventName(rs.getString("event_name"));
        e.setVenue(rs.getString("venue"));
        e.setEventDate(rs.getDate("event_date").toString());
        e.setStartTime(rs.getTime("start_time").toString().substring(0, 5));
        e.setEndTime(rs.getTime("end_time").toString().substring(0, 5));
        Time rt = rs.getTime("reporting_time");
        e.setReportingTime(rt == null ? null : rt.toString().substring(0, 5));
        e.setStaffRequired(rs.getInt("staff_required"));
        e.setStatus(rs.getString("status"));
        return e;
    }
}