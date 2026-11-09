// Put in: com.catering.ui   (replaces StaffPortalFrame.java)
package com.catering.ui;

import com.catering.dao.AssignmentDAO;
import com.catering.dao.ReadyDAO;
import com.catering.model.MyShift;
import com.catering.model.User;
import com.catering.service.CheckInService;
import com.catering.service.PayrollService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaffPortalFrame extends JFrame {

    private final User user;
    private final AssignmentDAO dao = new AssignmentDAO();
    private final ReadyDAO readyDao = new ReadyDAO();
    private final PayrollService payroll = new PayrollService();
    private final CheckInService rules = new CheckInService();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Event", "Date", "Report by", "Start", "End", "Venue", "Wage", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<MyShift> shifts = new ArrayList<>();
    private Set<Integer> readyIds = new HashSet<>();

    public StaffPortalFrame(User user) {
        this.user = user;
        setTitle("Cater Connect - My Events");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 560);
        setLocationRelativeTo(null);

        // ---- banner ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setBorder(Theme.headerBorder());
        JLabel title = new JLabel("<html><span style='color:#263445'>CATER</span> <span style='color:#1E8E85'>CONNECT</span>"
                + "<span style='color:#7C8B9C'>   |   My Events</span></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.ACCENT);
        header.add(title, BorderLayout.WEST);
        header.add(Theme.userChip(user.getUsername(), "WORKER"), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ---- table ----
        JPanel centre = new JPanel(new BorderLayout(8, 8));
        centre.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        JLabel note = new JLabel("<html>Press <b>I'm Ready</b> to confirm you will work the event. "
                + "Check-in opens " + CheckInService.EARLY_MINUTES
                + " minutes before the reporting time. Checking in more than "
                + CheckInService.GRACE_MINUTES + " minutes after it is recorded as LATE.</html>");
        note.setForeground(Theme.MUTED);
        centre.add(note, BorderLayout.NORTH);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(7).setCellRenderer(Theme.statusRenderer());
        centre.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        // ---- buttons ----
        JButton btnReady = new JButton("I'm Ready");
        btnReady.putClientProperty("FlatLaf.style", Theme.BTN_INFO);
        JButton btnCheckIn = new JButton("Check In");
        btnCheckIn.putClientProperty("FlatLaf.style", Theme.BTN_PRIMARY);
        JButton btnCant = new JButton("I Can't Attend");
        btnCant.putClientProperty("FlatLaf.style", Theme.BTN_DANGER);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.putClientProperty("FlatLaf.style", Theme.BTN_OUTLINE);
        JButton btnLogout = new JButton("Logout");
        btnLogout.putClientProperty("FlatLaf.style", Theme.BTN_OUTLINE);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.add(btnReady);
        bottom.add(btnCheckIn);
        bottom.add(btnCant);
        bottom.add(btnRefresh);
        bottom.add(btnLogout);
        add(bottom, BorderLayout.SOUTH);

        btnReady.addActionListener(e -> markReady());
        btnCheckIn.addActionListener(e -> checkIn());
        btnCant.addActionListener(e -> reportUnavailable());
        btnRefresh.addActionListener(e -> load());
        btnLogout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            shifts = dao.findForStaff(user.getStaffId());
            readyIds = readyDao.readyAssignmentIds(user.getStaffId());
            for (MyShift s : shifts) {
                model.addRow(new Object[]{s.getEventName(), s.getEventDate(), s.getReportingTime(),
                        s.getStartTime(), s.getEndTime(), s.getVenue() == null ? "" : s.getVenue(),
                        s.getDailyWage(), statusText(s)});
            }
            if (shifts.isEmpty()) setTitle("Cater Connect - My Events (nothing assigned yet)");
        } catch (SQLException ex) {
            showError("Could not load your events: " + ex.getMessage());
        }
    }

    private String statusText(MyShift s) {
        if (s.isUnavailable()) return "UNAVAILABLE (reported)";
        if (s.getAttendance().equals("PENDING")) {
            return readyIds.contains(s.getAssignmentId()) ? "READY (confirmed)" : "Not checked in";
        }
        return s.getAttendance() + (s.getCheckInTime() == null ? "" : " at " + s.getCheckInTime());
    }

    private MyShift selected() {
        int row = table.getSelectedRow();
        if (row < 0) { showWarning("Select an event in the table first."); return null; }
        return shifts.get(row);
    }

    private void markReady() {
        MyShift s = selected();
        if (s == null) return;
        if (s.isUnavailable()) { showWarning("You reported that you are unavailable for this event."); return; }
        if (!s.getAttendance().equals("PENDING")) { showWarning("You have already checked in for this event."); return; }
        if (readyIds.contains(s.getAssignmentId())) {
            JOptionPane.showMessageDialog(this, "You have already confirmed that you are ready for this event.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Confirm that you are ready to work " + s.getEventName() + " on " + s.getEventDate()
                        + "?\nReport by: " + s.getReportingTime(),
                "I'm Ready", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            if (readyDao.markReady(s.getAssignmentId(), user.getStaffId()))
                JOptionPane.showMessageDialog(this, "Thank you! The manager can see that you are ready.");
            else
                showWarning("Could not confirm. You may have marked yourself unavailable or already checked in.");
        } catch (SQLException ex) {
            showError("Could not confirm: " + ex.getMessage());
        }
        load();
    }

    private void checkIn() {
        MyShift s = selected();
        if (s == null) return;

        LocalDateTime now = LocalDateTime.now();
        String problem = rules.validate(s, now);
        if (problem != null) { showWarning(problem); return; }

        String status = rules.statusFor(s, now);
        int ok = JOptionPane.showConfirmDialog(this,
                "Check in for " + s.getEventName() + "?\nReporting time: " + s.getReportingTime()
                        + "\nYou will be recorded as " + status + ".",
                "Confirm check-in", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            double wage = payroll.calculateWage(s.getDailyWage(), status);
            if (dao.checkIn(s.getAssignmentId(), user.getStaffId(), status, wage))
                JOptionPane.showMessageDialog(this, "Checked in as " + status + ".");
            else
                showWarning("Could not check in. It may already be done, or the manager changed it.");
        } catch (SQLException ex) {
            showError("Check-in failed: " + ex.getMessage());
        }
        load();
    }

    private void reportUnavailable() {
        MyShift s = selected();
        if (s == null) return;
        if (s.isUnavailable()) { showWarning("You already reported that you are unavailable."); return; }
        if (!s.getAttendance().equals("PENDING")) { showWarning("You have already checked in for this event."); return; }

        String reason = JOptionPane.showInputDialog(this,
                "Why can't you attend " + s.getEventName() + " on " + s.getEventDate() + "?\n(e.g. medical emergency)");
        if (reason == null) return;
        reason = reason.trim();
        if (reason.length() < 5) { showWarning("Please enter a reason (at least 5 characters)."); return; }

        try {
            if (dao.reportUnavailable(s.getAssignmentId(), user.getStaffId(), reason))
                JOptionPane.showMessageDialog(this, "The manager has been notified. Please also call them if it is urgent.");
            else
                showWarning("Could not save. You may already have checked in.");
        } catch (SQLException ex) {
            showError("Could not report: " + ex.getMessage());
        }
        load();
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Check", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}