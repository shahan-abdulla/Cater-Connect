// Put in: com.catering.ui   (replaces ReadyStatusPanel.java)
package com.catering.ui;

import com.catering.dao.ReadyStatusDAO;
import com.catering.dao.ReadyStatusDAO.EventItem;
import com.catering.dao.ReadyStatusDAO.Row;
import com.catering.util.WhatsAppSender;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Shows, per event, which workers are ready, checked in, or cannot attend. */
public class ReadyStatusPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    private final ReadyStatusDAO dao = new ReadyStatusDAO();
    private final JComboBox<EventItem> combo = new JComboBox<>();
    private final JLabel summary = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Worker", "Phone", "Status", "Ready at", "Checked in at", "Reason"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Row> rows = new ArrayList<>();
    private boolean updating = false;

    public ReadyStatusPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- top: event picker + buttons + summary ----
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Event:"));
        combo.setPreferredSize(new Dimension(380, 28));
        controls.add(combo);
        JButton btnRefresh = new JButton("Refresh");
        JButton btnRemind = new JButton("Remind on WhatsApp");
        btnRemind.setToolTipText("Remind the selected worker to press I'm Ready");
        controls.add(btnRefresh);
        controls.add(btnRemind);

        summary.setFont(summary.getFont().deriveFont(Font.BOLD, 13f));
        summary.setForeground(Theme.ACCENT);
        summary.setBorder(BorderFactory.createEmptyBorder(0, 6, 4, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.add(controls, BorderLayout.NORTH);
        top.add(summary, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // ---- table ----
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(2).setCellRenderer(Theme.statusRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- actions ----
        combo.addActionListener(e -> { if (!updating) loadWorkers(); });
        btnRefresh.addActionListener(e -> loadEvents());
        btnRemind.addActionListener(e -> remind());

        // Refresh the worker list every 30 seconds while this tab is visible
        javax.swing.Timer timer = new javax.swing.Timer(30_000, null);
        timer.addActionListener(e -> {
            if (!isDisplayable()) { timer.stop(); return; }   // logged out
            if (isShowing()) loadWorkers();
        });
        timer.start();

        loadEvents();
    }

    private void loadEvents() {
        EventItem keep = (EventItem) combo.getSelectedItem();
        updating = true;
        try {
            combo.removeAllItems();
            EventItem toSelect = null;
            for (EventItem ev : dao.listEvents()) {
                combo.addItem(ev);
                if (keep != null && ev.id == keep.id) toSelect = ev;
            }
            if (toSelect != null) combo.setSelectedItem(toSelect);
        } catch (SQLException ex) {
            showError("Could not load events: " + ex.getMessage());
        } finally {
            updating = false;
        }
        loadWorkers();
    }

    private void loadWorkers() {
        EventItem ev = (EventItem) combo.getSelectedItem();
        int keepRow = table.getSelectedRow();
        model.setRowCount(0);
        rows = new ArrayList<>();
        if (ev == null) { summary.setText("No events yet."); return; }

        try {
            rows = dao.listWorkers(ev.id);
            int ready = 0, checked = 0, cannot = 0, notConfirmed = 0;
            for (Row r : rows) {
                model.addRow(new Object[]{r.name, r.phone, r.status(), fmt(r.readyAt),
                        fmt(r.checkInTime), r.reason == null ? "" : r.reason});
                if (r.unavailable) cannot++;
                if (r.isReady()) ready++;
                if (r.isCheckedIn()) checked++;
                if (r.status().equals("Not confirmed")) notConfirmed++;
            }
            int replacements = Math.max(0, ev.required - (rows.size() - cannot));
            summary.setText("Needed: " + ev.required + "    Assigned: " + rows.size()
                    + "    Ready: " + ready + "    Not confirmed: " + notConfirmed
                    + "    Checked in: " + checked + "    Cannot attend: " + cannot
                    + "    Replacements needed: " + replacements);
            if (keepRow >= 0 && keepRow < model.getRowCount()) {
                table.setRowSelectionInterval(keepRow, keepRow);
            }
        } catch (SQLException ex) {
            showError("Could not load workers: " + ex.getMessage());
        }
    }

    private void remind() {
        EventItem ev = (EventItem) combo.getSelectedItem();
        int row = table.getSelectedRow();
        if (ev == null || row < 0) {
            JOptionPane.showMessageDialog(this, "Select a worker in the table first.");
            return;
        }
        Row r = rows.get(row);
        if (!r.status().equals("Not confirmed")) {
            JOptionPane.showMessageDialog(this,
                    r.name + " is already: " + r.status() + ". No reminder needed.");
            return;
        }
        String msg = "Hello " + r.name + "! Reminder for " + ev.name + " on " + ev.date
                + ". Please open the staff app, log in and press I'm Ready.";
        WhatsAppSender.send(this, r.phone, msg);
    }

    private static String fmt(Timestamp t) {
        return t == null ? "" : t.toLocalDateTime().format(FMT);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}