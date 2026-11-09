// Put in: com.catering.dao   (replaces the earlier ReadyDAO.java)
package com.catering.dao;

import com.catering.util.DBConnection;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ReadyDAO {

    /** Assignment ids this worker has already confirmed as ready. */
    public Set<Integer> readyAssignmentIds(int staffId) throws SQLException {
        String sql = "SELECT assignment_id FROM assignments "
                   + "WHERE staff_id = ? AND ready_at IS NOT NULL";
        Set<Integer> ids = new HashSet<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    /**
     * Marks the worker as ready for this assignment.
     * Returns false if it is not theirs, they marked themselves unavailable,
     * or they have already checked in.
     */
    public boolean markReady(int assignmentId, int staffId) throws SQLException {
        String sql = "UPDATE assignments SET ready_at = NOW() "
                   + "WHERE assignment_id = ? AND staff_id = ? "
                   + "AND unavailable = FALSE AND attendance = 'PENDING'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.setInt(2, staffId);
            return ps.executeUpdate() > 0;
        }
    }
}