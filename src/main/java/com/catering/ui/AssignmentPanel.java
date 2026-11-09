// Put in: com.catering.ui   (replaces AssignmentPanel.java)
package com.catering.ui;

import com.catering.dao.AssignmentDAO;
import com.catering.dao.EventDAO;
import com.catering.model.Assignment;
import com.catering.model.Event;
import com.catering.model.Staff;
import com.catering.util.ContactHelper;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssignmentPanel extends JPanel {

    private final EventDAO eventDao = new EventDAO();
    private final AssignmentDAO dao = new AssignmentDAO();

    private final JComboBox<Event> cmbEvent = new JComboBox<>();
    private final JLabel lblInfo = new JLabel(" ");
    private final JLabel lblPicked = new JLabel("0 selected");
    private final JButton btnAssign = new JButton("Assign Selected (0)  >>");

    // column 0 is a checkbox; column 1 is the staff id
    private final DefaultTableModel availModel = new DefaultTableModel(
            new String[]{"Pick", "ID", "Name", "Phone", "Role", "Daily Wage"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return c == 0; }
        @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : Object.class; }
    };
    private final JTable availTable = new JTable(availModel);

    private final DefaultTableModel assignedModel = new DefaultTableModel(
            new String[]{"Assign ID", "Name", "Phone", "Role", "Daily Wage", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable assignedTable = new JTable(assignedModel);

    private List<Assignment> assignedList = new ArrayList<>();

    public AssignmentPanel() {
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

        // ---- centre: two tables ----
        JPanel availPanel = new JPanel(new BorderLayout());
        availPanel.setBorder(BorderFactory.createTitledBorder("Available staff (tick to select)"));
        availTable.getColumnModel().getColumn(0).setMaxWidth(50);
        availTable.getColumnModel().getColumn(1).setMaxWidth(50);
        availPanel.add(new JScrollPane(availTable), BorderLayout.CENTER);

        // select all / clear bar under the available list
        JButton btnAll = new JButton("Select All");
        JButton btnNone = new JButton("Clear ticks");
        btnAll.putClientProperty("FlatLaf.style", Theme.BTN_OUTLINE);
        btnNone.putClientProperty("FlatLaf.style", Theme.BTN_OUTLINE);
        lblPicked.setForeground(Theme.ACCENT);
        JPanel pickBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pickBar.add(btnAll);
        pickBar.add(btnNone);
        pickBar.add(lblPicked);
        availPanel.add(pickBar, BorderLayout.SOUTH);

        JPanel assignedPanel = new JPanel(new BorderLayout());
        assignedPanel.setBorder(BorderFactory.createTitledBorder("Assigned to this event"));
        assignedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assignedTable.getColumnModel().getColumn(0).setMaxWidth(70);
        assignedTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                Object st = t.getValueAt(r, 5);
                boolean un = st != null && st.toString().startsWith("UNAVAILABLE");
                comp.setForeground(sel ? t.getSelectionForeground()
                        : (un ? Theme.DANGER : t.getForeground()));
                return comp;
            }
        });
        assignedPanel.add(new JScrollPane(assignedTable), BorderLayout.CENTER);

        JPanel centre = new JPanel(new GridLayout(1, 2, 10, 10));
        centre.add(availPanel);
        centre.add(assignedPanel);
        add(centre, BorderLayout.CENTER);

        // ---- bottom: buttons ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        JButton btnRemove = new JButton("<<  Remove selected");
        JButton btnCall = new JButton("Call");
        JButton btnNotify = new JButton("Notify on WhatsApp");
        bottom.add(btnAssign);
        bottom.add(btnRemove);
        bottom.add(new JSeparator(SwingConstants.VERTICAL));
        bottom.add(btnCall);
        bottom.add(btnNotify);
        add(bottom, BorderLayout.SOUTH);

        // ---- actions ----
        cmbEvent.addActionListener(e -> refreshTables());
        btnRefresh.addActionListener(e -> loadEvents());
        btnAssign.addActionListener(e -> assignSelected());
        btnRemove.addActionListener(e -> removeSelected());
        btnCall.addActionListener(e -> callSelected());
        btnNotify.addActionListener(e -> notifySelected());
        btnAll.addActionListener(e -> setAllTicks(true));
        btnNone.addActionListener(e -> setAllTicks(false));

        // keep the count on the Assign button up to date
        availModel.addTableModelListener(e -> updateAssignButton());

        // clicking anywhere on a row also toggles its tick
        availTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = availTable.rowAtPoint(e.getPoint());
                int c = availTable.columnAtPoint(e.getPoint());
                if (r >= 0 && c > 0) {
                    boolean now = Boolean.TRUE.equals(availModel.getValueAt(r, 0));
                    availModel.setValueAt(!now, r, 0);
                }
            }
        });

        loadEvents();
    }

    private void loadEvents() {
        try {
            cmbEvent.removeAllItems();
            for (Event ev : eventDao.findOpenEvents()) cmbEvent.addItem(ev);
            refreshTables();
        } catch (SQLException ex) {
            showError("Could not load events: " + ex.getMessage());
        }
    }

    private void refreshTables() {
        availModel.setRowCount(0);
        assignedModel.setRowCount(0);
        assignedList = new ArrayList<>();
        Event ev = (Event) cmbEvent.getSelectedItem();
        if (ev == null) { lblInfo.setText("No open events. Add one in the Events tab, then press Refresh."); return; }

        try {
            List<Staff> avail = dao.findAvailableStaff(ev);
            assignedList = dao.findByEvent(ev.getId());

            for (Staff s : avail) {
                boolean already = false;
                for (Assignment a : assignedList) if (a.getStaffId() == s.getId()) already = true;
                if (!already) {
                    availModel.addRow(new Object[]{Boolean.FALSE, s.getId(), s.getName(),
                            s.getPhone(), s.getRole(), s.getDailyWage()});
                }
            }

            int coming = 0, unavailable = 0;
            for (Assignment a : assignedList) {
                if (a.isUnavailable()) unavailable++; else coming++;
                assignedModel.addRow(new Object[]{a.getId(), a.getStaffName(), a.getPhone(),
                        a.getStaffRole(), a.getDailyWage(), statusText(a)});
            }

            String info = "  " + ev.getEventName() + " at " + (ev.getVenue() == null || ev.getVenue().isEmpty() ? "-" : ev.getVenue())
                    + "  |  Report by: " + ev.getReportingOrStart()
                    + "  |  Needed: " + ev.getStaffRequired()
                    + "  |  Coming: " + coming
                    + "  |  Still needed: " + Math.max(0, ev.getStaffRequired() - coming);
            if (unavailable > 0) info += "  |  UNAVAILABLE: " + unavailable + " (remove and replace)";
            lblInfo.setText(info);
        } catch (SQLException ex) {
            showError("Could not load staff: " + ex.getMessage());
        }
        updateAssignButton();
    }

    private String statusText(Assignment a) {
        if (a.isUnavailable()) return "UNAVAILABLE: " + a.getUnavailableReason();
        return a.getAttendance() + (a.getCheckInTime() == null ? "" : " (checked in " + a.getCheckInTime() + ")");
    }

    // ---------- tick boxes ----------

    private int tickedCount() {
        int n = 0;
        for (int r = 0; r < availModel.getRowCount(); r++) {
            if (Boolean.TRUE.equals(availModel.getValueAt(r, 0))) n++;
        }
        return n;
    }

    private void updateAssignButton() {
        int n = tickedCount();
        btnAssign.setText("Assign Selected (" + n + ")  >>");
        lblPicked.setText(n + " selected");
    }

    private void setAllTicks(boolean value) {
        for (int r = 0; r < availModel.getRowCount(); r++) {
            availModel.setValueAt(value, r, 0);
        }
    }

    // ---------- assign many at once ----------

    private void assignSelected() {
        Event ev = (Event) cmbEvent.getSelectedItem();
        if (ev == null) { showWarning("Choose an event first."); return; }

        List<Integer> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int r = 0; r < availModel.getRowCount(); r++) {
            if (Boolean.TRUE.equals(availModel.getValueAt(r, 0))) {
                ids.add(((Number) availModel.getValueAt(r, 1)).intValue());
                names.add(String.valueOf(availModel.getValueAt(r, 2)));
            }
        }
        if (ids.isEmpty()) {
            showWarning("Tick the box next to one or more available staff first.");
            return;
        }

        try {
            // more people than the event needs? ask first
            int already = dao.countForEvent(ev.getId());
            int free = Math.max(0, ev.getStaffRequired() - already);
            if (ids.size() > free) {
                int ok = JOptionPane.showConfirmDialog(this,
                        "This event needs " + ev.getStaffRequired() + " staff and " + already
                                + " are already coming, so only " + free + " more are needed.\n"
                                + "You ticked " + ids.size() + ". Assign them all anyway?",
                        "More than needed", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (ok != JOptionPane.YES_OPTION) return;
            }

            // check again that each ticked worker is still free (no clashing event)
            Set<Integer> stillFree = new HashSet<>();
            for (Staff s : dao.findAvailableStaff(ev)) stillFree.add(s.getId());

            int done = 0;
            List<String> problems = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                int staffId = ids.get(i);
                String name = names.get(i);
                if (!stillFree.contains(staffId)) {
                    problems.add(name + " - no longer available (booked on another event)");
                    continue;
                }
                try {
                    dao.assign(ev.getId(), staffId);
                    done++;
                } catch (SQLException ex) {
                    String why = ex.getMessage() != null && ex.getMessage().contains("Duplicate")
                            ? "already assigned" : ex.getMessage();
                    problems.add(name + " - " + why);
                }
            }

            refreshTables();
            if (!problems.isEmpty()) {
                showWarning("Assigned " + done + " of " + ids.size() + ".\nNot assigned:\n  "
                        + String.join("\n  ", problems));
            }
        } catch (SQLException ex) {
            showError("Could not assign: " + ex.getMessage());
            refreshTables();
        }
    }

    private void removeSelected() {
        Assignment a = selectedAssigned();
        if (a == null) return;
        if (!a.isUnavailable() && !a.getAttendance().equals("PENDING")) {
            showWarning("Attendance is already marked, so this assignment can't be removed.");
            return;
        }
        try {
            dao.remove(a.getId());
            refreshTables();
        } catch (SQLException ex) {
            showError("Could not remove: " + ex.getMessage());
        }
    }

    // ---------- call and notify ----------

    private void callSelected() {
        Assignment a = selectedAssigned();
        if (a == null) return;
        if (!ContactHelper.call(a.getPhone())) {
            ContactHelper.copy(a.getPhone());
            JOptionPane.showMessageDialog(this,
                    "No calling app was found on this PC.\nThe number " + a.getPhone()
                            + " has been copied. Dial it from your phone.");
        }
    }

    private void notifySelected() {
        Assignment a = selectedAssigned();
        if (a == null) return;
        Event ev = (Event) cmbEvent.getSelectedItem();
        if (ev == null) return;

        String message = buildMessage(ev, a);
        if (!ContactHelper.whatsApp(a.getPhone(), message)) {
            ContactHelper.copy(message);
            JOptionPane.showMessageDialog(this,
                    "Could not open WhatsApp. The message has been copied, so paste it into any chat or SMS.");
        }
    }

    private String buildMessage(Event ev, Assignment a) {
        String venue = (ev.getVenue() == null || ev.getVenue().isEmpty()) ? "to be confirmed" : ev.getVenue();
        return "Hello " + a.getStaffName() + ", you are assigned to " + ev.getEventName()
                + " on " + ev.getEventDate() + ". Venue: " + venue
                + ". Reporting time: " + ev.getReportingOrStart()
                + " (event " + ev.getStartTime() + " to " + ev.getEndTime() + ")."
                + " Wage: Rs. " + String.format("%.2f", a.getDailyWage())
                + ". Please log in to the staff app and press I'm Ready to confirm. - Cater Connect";
    }

    private Assignment selectedAssigned() {
        int row = assignedTable.getSelectedRow();
        if (row < 0) { showWarning("Select someone from the assigned list first."); return null; }
        return assignedList.get(row);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Check", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}