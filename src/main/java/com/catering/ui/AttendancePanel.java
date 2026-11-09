// Put in: com.catering.ui   (replaces AttendancePanel.java)
package com.catering.ui;

import com.catering.dao.AssignmentDAO;
import com.catering.dao.AttendanceGuardDAO;
import com.catering.dao.AttendanceGuardDAO.Result;
import com.catering.dao.EventDAO;
import com.catering.model.Assignment;
import com.catering.model.Event;
import com.catering.service.PayrollService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AttendancePanel extends JPanel {

    private final EventDAO eventDao = new EventDAO();
    private final AssignmentDAO dao = new AssignmentDAO();
    private final AttendanceGuardDAO guard = new AttendanceGuardDAO();
    private final PayrollService payroll = new PayrollService();

    private final JComboBox<Event> cmbEvent = new JComboBox<>();
    private final JLabel lblInfo = new JLabel(" ");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Assign ID", "Name", "Phone", "Role", "Daily Wage", "Attendance", "Wage", "Paid"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JButton btnPresent = new JButton("Present");
    private final JButton btnLate = new JButton("Late");
    private final JButton btnAbsent = new JButton("Absent");
    private final JButton btnAllPresent = new JButton("Mark All Present");

    private List<Assignment> currentList = new ArrayList<>();
    private String infoBase = " ";
    private LocalDateTime opensAt = null;   // when marking becomes allowed for the chosen event

    public AttendancePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- top: choose event ----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Event:"));
        cmbEvent.setPreferredSize(new Dimension(380, 26));
        top.add(cmbEvent);
        JButton btnRefresh = new JButton("Refresh");
        top.add(btnRefresh);

        JPanel north = new JPanel(new BorderLayout());
        north.add(top, BorderLayout.NORTH);
        north.add(lblInfo, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        // ---- centre: table ----
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(5).setCellRenderer(Theme.statusRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- bottom: buttons ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnComplete = new JButton("Mark Event Completed");
        bottom.add(btnPresent);
        bottom.add(btnLate);
        bottom.add(btnAbsent);
        bottom.add(btnAllPresent);
        bottom.add(btnComplete);
        add(bottom, BorderLayout.SOUTH);

        // ---- actions ----
        cmbEvent.addActionListener(e -> loadAssignments());
        btnRefresh.addActionListener(e -> loadEvents());
        btnPresent.addActionListener(e -> mark("PRESENT"));
        btnLate.addActionListener(e -> mark("LATE"));
        btnAbsent.addActionListener(e -> mark("ABSENT"));
        btnAllPresent.addActionListener(e -> { table.selectAll(); mark("PRESENT"); });
        btnComplete.addActionListener(e -> completeEvent());

        // Every 30 seconds: unlock the buttons when the time comes, and pick up
        // worker check-ins (the table is only reloaded if nothing is selected)
        javax.swing.Timer timer = new javax.swing.Timer(30_000, null);
        timer.addActionListener(e -> {
            if (!isDisplayable()) { timer.stop(); return; }   // logged out
            if (!isShowing()) return;
            if (table.getSelectedRowCount() == 0) loadAssignments();
            else updateLock();
        });
        timer.start();

        loadEvents();
    }

    private void loadEvents() {
        try {
            cmbEvent.removeAllItems();
            for (Event ev : eventDao.findForAttendance()) cmbEvent.addItem(ev);
            loadAssignments();
        } catch (SQLException ex) {
            showError("Could not load events: " + ex.getMessage());
        }
    }

    private void loadAssignments() {
        model.setRowCount(0);
        currentList = new ArrayList<>();
        opensAt = null;
        Event ev = (Event) cmbEvent.getSelectedItem();
        if (ev == null) {
            infoBase = "No events found. Add one in the Events tab, then press Refresh.";
            lblInfo.setForeground(Theme.MUTED);
            lblInfo.setText(infoBase);
            setMarkingEnabled(false, "");
            return;
        }

        try {
            opensAt = guard.opensAt(ev.getId());
            currentList = dao.findByEvent(ev.getId());
            double total = 0;
            for (Assignment a : currentList) {
                model.addRow(new Object[]{a.getId(), a.getStaffName(), a.getPhone(), a.getStaffRole(),
                        a.getDailyWage(), a.getAttendance(), a.getWageAmount(), a.isPaid() ? "Yes" : "No"});
                total += a.getWageAmount();
            }
            infoBase = "  " + ev.getEventName() + "  |  Status: " + ev.getStatus()
                    + "  |  Staff: " + currentList.size()
                    + "  |  Total wages for this event: " + total;
        } catch (SQLException ex) {
            showError("Could not load assignments: " + ex.getMessage());
        }
        updateLock();
    }

    // ---- the "only after reporting time" rule (screen side) ----

    private boolean isOpen() {
        return opensAt != null && !LocalDateTime.now().isBefore(opensAt);
    }

    private String lockMessage() {
        if (opensAt == null) return "The event time is not set, so attendance cannot be marked.";
        return "Attendance can be marked after "
                + opensAt.format(DateTimeFormatter.ofPattern("HH:mm"))
                + " on " + opensAt.toLocalDate() + ".";
    }

    private void updateLock() {
        boolean open = isOpen();
        if (open) {
            lblInfo.setForeground(Theme.TEXT);
            lblInfo.setText(infoBase);
        } else {
            lblInfo.setForeground(Theme.DANGER);
            lblInfo.setText(infoBase + "   |   " + lockMessage());
        }
        setMarkingEnabled(open, lockMessage());
    }

    private void setMarkingEnabled(boolean enabled, String reasonWhenLocked) {
        String tip = enabled ? null : reasonWhenLocked;
        for (JButton b : new JButton[]{btnPresent, btnLate, btnAbsent, btnAllPresent}) {
            b.setEnabled(enabled);
            b.setToolTipText(tip);
        }
    }

    private void mark(String attendance) {
        if (!isOpen()) { showWarning(lockMessage()); return; }
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) { showWarning("Select one or more people in the table first."); return; }

        int paid = 0, checkedIn = 0, early = 0, changed = 0;
        try {
            for (int r : rows) {
                Assignment a = currentList.get(r);
                double wage = payroll.calculateWage(a.getDailyWage(), attendance);
                Result res = guard.mark(a.getId(), attendance, wage);
                switch (res) {
                    case OK: break;
                    case PAID: paid++; break;
                    case CHECKED_IN: checkedIn++; break;
                    case TOO_EARLY: early++; break;
                    default: changed++; break;
                }
            }
        } catch (SQLException ex) {
            showError("Could not save attendance: " + ex.getMessage());
        }
        loadAssignments();

        StringBuilder msg = new StringBuilder();
        if (paid > 0) msg.append(paid).append(" already-paid row(s) were not changed.\n");
        if (checkedIn > 0) msg.append(checkedIn)
                .append(" worker(s) checked in themselves and were not changed.\n");
        if (early > 0) msg.append(lockMessage()).append('\n');
        if (changed > 0) msg.append(changed).append(" row(s) changed meanwhile. Please refresh and try again.\n");
        if (msg.length() > 0) showWarning(msg.toString().trim());
    }

    private void completeEvent() {
        Event ev = (Event) cmbEvent.getSelectedItem();
        if (ev == null) { showWarning("Choose an event first."); return; }

        int pending = 0;
        for (Assignment a : currentList) if (a.getAttendance().equals("PENDING")) pending++;
        String msg = pending > 0
                ? pending + " person(s) still have PENDING attendance and will earn 0. Mark the event completed anyway?"
                : "Mark this event as COMPLETED?";
        int ok = JOptionPane.showConfirmDialog(this, msg, "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            eventDao.setStatus(ev.getId(), "COMPLETED");
            loadEvents();
        } catch (SQLException ex) {
            showError("Could not update event: " + ex.getMessage());
        }
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Check", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}