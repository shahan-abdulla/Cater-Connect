package com.catering.dao;

import com.catering.model.Assignment;
import com.catering.model.Event;
import com.catering.model.MyShift;
import com.catering.model.Staff;
import com.catering.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO {

    // Active staff NOT booked in an overlapping, non-cancelled event.
    // Assignments the worker has marked "unavailable" do not block them.
    public List<Staff> findAvailableStaff(Event ev) throws SQLException {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT s.* FROM staff s "
                   + "WHERE s.active = TRUE "
                   + "AND s.staff_id NOT IN ("
                   + "  SELECT a.staff_id FROM assignments a "
                   + "  JOIN events e ON a.event_id = e.event_id "
                   + "  WHERE e.event_date = ? "
                   + "  AND e.status <> 'CANCELLED' "
                   + "  AND a.unavailable = FALSE "
                   + "  AND e.start_time < ? "
                   + "  AND e.end_time > ?) "
                   + "ORDER BY s.name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ev.getEventDate()));
            ps.setTime(2, Time.valueOf(ev.getEndTime() + ":00"));
            ps.setTime(3, Time.valueOf(ev.getStartTime() + ":00"));
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

    // People who are still coming (unavailable ones are not counted)
    public int countForEvent(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM assignments WHERE event_id = ? AND unavailable = FALSE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void assign(int eventId, int staffId) throws SQLException {
        String sql = "INSERT INTO assignments(event_id, staff_id) VALUES(?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, staffId);
            ps.executeUpdate();
        }
    }

    // Only allowed while attendance is still PENDING and nothing is paid
    public void remove(int assignmentId) throws SQLException {
        String sql = "DELETE FROM assignments WHERE assignment_id = ? AND attendance = 'PENDING' AND paid = FALSE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.executeUpdate();
        }
    }

    public List<Assignment> findByEvent(int eventId) throws SQLException {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT a.*, s.name, s.phone, s.role, s.daily_wage FROM assignments a "
                   + "JOIN staff s ON a.staff_id = s.staff_id "
                   + "WHERE a.event_id = ? ORDER BY s.name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Assignment a = new Assignment();
                    a.setId(rs.getInt("assignment_id"));
                    a.setEventId(rs.getInt("event_id"));
                    a.setStaffId(rs.getInt("staff_id"));
                    a.setStaffName(rs.getString("name"));
                    a.setPhone(rs.getString("phone"));
                    a.setStaffRole(rs.getString("role"));
                    a.setAttendance(rs.getString("attendance"));
                    a.setWageAmount(rs.getDouble("wage_amount"));
                    a.setPaid(rs.getBoolean("paid"));
                    a.setDailyWage(rs.getDouble("daily_wage"));
                    Timestamp t = rs.getTimestamp("check_in_time");
                    a.setCheckInTime(t == null ? null : t.toString().substring(11, 16));
                    a.setUnavailable(rs.getBoolean("unavailable"));
                    a.setUnavailableReason(rs.getString("unavailable_reason"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    // Wages already paid can't be changed
    public void markAttendance(int assignmentId, String attendance, double wage) throws SQLException {
        String sql = "UPDATE assignments SET attendance = ?, wage_amount = ? "
                   + "WHERE assignment_id = ? AND paid = FALSE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, attendance);
            ps.setDouble(2, wage);
            ps.setInt(3, assignmentId);
            ps.executeUpdate();
        }
    }

    // ---------- used by the staff portal ----------

    // Upcoming (today or later) events of one worker, excluding cancelled/completed events
    public List<MyShift> findForStaff(int staffId) throws SQLException {
        List<MyShift> list = new ArrayList<>();
        String sql = "SELECT a.assignment_id, a.attendance, a.unavailable, a.check_in_time, "
                   + "e.event_name, e.venue, e.event_date, e.start_time, e.end_time, e.reporting_time, s.daily_wage "
                   + "FROM assignments a JOIN events e ON a.event_id = e.event_id "
                   + "JOIN staff s ON a.staff_id = s.staff_id "
                   + "WHERE a.staff_id = ? AND e.status IN ('PLANNED','ONGOING') AND e.event_date >= CURDATE() "
                   + "ORDER BY e.event_date, e.start_time";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MyShift m = new MyShift();
                    m.setAssignmentId(rs.getInt("assignment_id"));
                    m.setAttendance(rs.getString("attendance"));
                    m.setUnavailable(rs.getBoolean("unavailable"));
                    Timestamp t = rs.getTimestamp("check_in_time");
                    m.setCheckInTime(t == null ? null : t.toString().substring(11, 16));
                    m.setEventName(rs.getString("event_name"));
                    m.setVenue(rs.getString("venue"));
                    m.setEventDate(rs.getDate("event_date").toString());
                    String start = rs.getTime("start_time").toString().substring(0, 5);
                    m.setStartTime(start);
                    m.setEndTime(rs.getTime("end_time").toString().substring(0, 5));
                    Time rt = rs.getTime("reporting_time");
                    m.setReportingTime(rt == null ? start : rt.toString().substring(0, 5));
                    m.setDailyWage(rs.getDouble("daily_wage"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    // Returns false if it was already checked in, reported unavailable or paid
    public boolean checkIn(int assignmentId, int staffId, String attendance, double wage) throws SQLException {
        String sql = "UPDATE assignments SET attendance = ?, wage_amount = ?, check_in_time = NOW() "
                   + "WHERE assignment_id = ? AND staff_id = ? "
                   + "AND attendance = 'PENDING' AND unavailable = FALSE AND paid = FALSE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, attendance);
            ps.setDouble(2, wage);
            ps.setInt(3, assignmentId);
            ps.setInt(4, staffId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean reportUnavailable(int assignmentId, int staffId, String reason) throws SQLException {
        String sql = "UPDATE assignments SET unavailable = TRUE, unavailable_reason = ?, reported_at = NOW() "
                   + "WHERE assignment_id = ? AND staff_id = ? AND attendance = 'PENDING' AND unavailable = FALSE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, assignmentId);
            ps.setInt(3, staffId);
            return ps.executeUpdate() > 0;
        }
    }
}