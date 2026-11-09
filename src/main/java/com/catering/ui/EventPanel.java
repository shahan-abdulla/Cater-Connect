package com.catering.ui;

import com.catering.dao.ClientDAO;
import com.catering.dao.EventDAO;
import com.catering.model.Client;
import com.catering.model.Event;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EventPanel extends JPanel {

    private final EventDAO dao = new EventDAO();
    private final ClientDAO clientDao = new ClientDAO();

    private final JComboBox<Client> cmbClient = new JComboBox<>();
    private final JTextField txtName = new JTextField(15);
    private final JTextField txtVenue = new JTextField(15);
    private final JTextField txtDate = new JTextField(15);
    private final JTextField txtReport = new JTextField(15);
    private final JTextField txtStart = new JTextField(15);
    private final JTextField txtEnd = new JTextField(15);
    private final JTextField txtStaff = new JTextField(15);
    private final JComboBox<String> cmbStatus =
            new JComboBox<>(new String[]{"PLANNED", "ONGOING", "COMPLETED", "CANCELLED"});
    private final JTextField txtSearch = new JTextField(15);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Event", "Client", "Venue", "Date", "Report", "Start", "End", "Staff", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private int selectedId = -1;
    private List<Event> currentList = new ArrayList<>();

    public EventPanel(boolean isAdmin) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- top: search ----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search (event, client or venue):"));
        top.add(txtSearch);
        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");
        JButton btnRefreshClients = new JButton("Refresh Clients");
        top.add(btnSearch);
        top.add(btnShowAll);
        top.add(btnRefreshClients);
        add(top, BorderLayout.NORTH);

        // ---- centre: table ----
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- right: form ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Event details"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        addRow(form, g, 0, "Client:", cmbClient);
        addRow(form, g, 1, "Event name:", txtName);
        addRow(form, g, 2, "Venue:", txtVenue);
        addRow(form, g, 3, "Date (yyyy-MM-dd):", txtDate);
        addRow(form, g, 4, "Reporting (HH:mm):", txtReport);
        addRow(form, g, 5, "Start (HH:mm):", txtStart);
        addRow(form, g, 6, "End (HH:mm):", txtEnd);
        addRow(form, g, 7, "Staff needed:", txtStaff);
        addRow(form, g, 8, "Status:", cmbStatus);
        txtReport.setToolTipText("Time staff must arrive. Leave empty to use the start time.");

        JPanel buttons = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnCancel = new JButton("Cancel Event");
        JButton btnClear = new JButton("Clear");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnCancel);
        buttons.add(btnClear);
        g.gridx = 0; g.gridy = 9; g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(buttons, g);

        JPanel east = new JPanel(new BorderLayout());
        east.add(form, BorderLayout.NORTH);
        add(east, BorderLayout.EAST);

        // ---- actions ----
        btnAdd.addActionListener(e -> addEvent());
        btnUpdate.addActionListener(e -> updateEvent());
        btnCancel.addActionListener(e -> cancelEvent());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> loadTable(txtSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadTable(""); });
        btnRefreshClients.addActionListener(e -> loadClients());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });

        btnCancel.setEnabled(isAdmin);
        if (!isAdmin) btnCancel.setToolTipText("Admin only");

        loadClients();
        loadTable("");
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        p.add(field, g);
    }

    private void loadClients() {
        try {
            cmbClient.removeAllItems();
            for (Client c : clientDao.search("")) cmbClient.addItem(c);
        } catch (SQLException ex) {
            showError("Could not load clients: " + ex.getMessage());
        }
    }

    private void loadTable(String text) {
        try {
            model.setRowCount(0);
            currentList = dao.search(text);
            for (Event e : currentList) {
                model.addRow(new Object[]{e.getId(), e.getEventName(), e.getClientName(), e.getVenue(),
                        e.getEventDate(), e.getReportingOrStart(), e.getStartTime(), e.getEndTime(),
                        e.getStaffRequired(), e.getStatus()});
            }
        } catch (SQLException ex) {
            showError("Could not load events: " + ex.getMessage());
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Event e = currentList.get(row);
        selectedId = e.getId();

        for (int i = 0; i < cmbClient.getItemCount(); i++) {
            if (cmbClient.getItemAt(i).getId() == e.getClientId()) {
                cmbClient.setSelectedIndex(i);
                break;
            }
        }
        txtName.setText(e.getEventName());
        txtVenue.setText(e.getVenue() == null ? "" : e.getVenue());
        txtDate.setText(e.getEventDate());
        txtReport.setText(e.getReportingTime() == null ? "" : e.getReportingTime());
        txtStart.setText(e.getStartTime());
        txtEnd.setText(e.getEndTime());
        txtStaff.setText(String.valueOf(e.getStaffRequired()));
        cmbStatus.setSelectedItem(e.getStatus());
    }

    // Reads the form, validates it, returns null if something is wrong
    private Event readForm() {
        Client client = (Client) cmbClient.getSelectedItem();
        if (client == null) { showWarning("Add a client first (Clients tab), then press Refresh Clients."); return null; }

        String name = txtName.getText().trim();
        if (name.isEmpty()) { showWarning("Event name cannot be empty."); return null; }

        LocalDate date;
        try {
            date = LocalDate.parse(txtDate.getText().trim());
        } catch (DateTimeParseException ex) {
            showWarning("Date must look like 2026-12-31."); return null;
        }

        LocalTime start, end;
        try {
            start = LocalTime.parse(txtStart.getText().trim());
            end = LocalTime.parse(txtEnd.getText().trim());
        } catch (DateTimeParseException ex) {
            showWarning("Times must look like 18:30 (24-hour format)."); return null;
        }
        if (!end.isAfter(start)) { showWarning("End time must be after start time."); return null; }

        String reportText = null;
        String rep = txtReport.getText().trim();
        if (!rep.isEmpty()) {
            LocalTime report;
            try {
                report = LocalTime.parse(rep);
            } catch (DateTimeParseException ex) {
                showWarning("Reporting time must look like 17:30, or leave it empty."); return null;
            }
            if (report.isAfter(start)) { showWarning("Reporting time must not be after the start time."); return null; }
            reportText = String.format("%02d:%02d", report.getHour(), report.getMinute());
        }

        int staff;
        try {
            staff = Integer.parseInt(txtStaff.getText().trim());
            if (staff <= 0) { showWarning("Staff needed must be at least 1."); return null; }
        } catch (NumberFormatException ex) {
            showWarning("Staff needed must be a number."); return null;
        }

        Event e = new Event();
        e.setClientId(client.getId());
        e.setEventName(name);
        e.setVenue(txtVenue.getText().trim());
        e.setEventDate(date.toString());
        e.setReportingTime(reportText);
        e.setStartTime(String.format("%02d:%02d", start.getHour(), start.getMinute()));
        e.setEndTime(String.format("%02d:%02d", end.getHour(), end.getMinute()));
        e.setStaffRequired(staff);
        e.setStatus((String) cmbStatus.getSelectedItem());
        return e;
    }

    private void addEvent() {
        Event e = readForm();
        if (e == null) return;
        try {
            dao.add(e);
            loadTable("");
            clearForm();
            JOptionPane.showMessageDialog(this, "Event added.");
        } catch (SQLException ex) {
            showError("Could not add: " + ex.getMessage());
        }
    }

    private void updateEvent() {
        if (selectedId < 0) { showWarning("Select an event from the table first."); return; }
        Event e = readForm();
        if (e == null) return;
        e.setId(selectedId);
        try {
            dao.update(e);
            loadTable("");
            clearForm();
            JOptionPane.showMessageDialog(this, "Event updated.");
        } catch (SQLException ex) {
            showError("Could not update: " + ex.getMessage());
        }
    }

    private void cancelEvent() {
        if (selectedId < 0) { showWarning("Select an event from the table first."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Cancel this event?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            dao.cancel(selectedId);
            loadTable("");
            clearForm();
        } catch (SQLException ex) {
            showError("Could not cancel: " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtVenue.setText("");
        txtDate.setText("");
        txtReport.setText("");
        txtStart.setText("");
        txtEnd.setText("");
        txtStaff.setText("");
        cmbStatus.setSelectedIndex(0);
        if (cmbClient.getItemCount() > 0) cmbClient.setSelectedIndex(0);
        table.clearSelection();
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Check input", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}