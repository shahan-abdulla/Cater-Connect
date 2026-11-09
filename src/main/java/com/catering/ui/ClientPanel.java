package com.catering.ui;

import com.catering.dao.ClientDAO;
import com.catering.model.Client;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ClientPanel extends JPanel {

    private final ClientDAO dao = new ClientDAO();

    private final JTextField txtName = new JTextField(15);
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtAddress = new JTextField(15);
    private final JTextField txtSearch = new JTextField(15);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Phone", "Address"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private int selectedId = -1;

    public ClientPanel(boolean isAdmin){
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- top: search ----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search (name or phone):"));
        top.add(txtSearch);
        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");
        top.add(btnSearch);
        top.add(btnShowAll);
        add(top, BorderLayout.NORTH);

        // ---- centre: table ----
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- right: form ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Client details"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        addRow(form, g, 0, "Name:", txtName);
        addRow(form, g, 1, "Phone:", txtPhone);
        addRow(form, g, 2, "Address:", txtAddress);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(buttons, g);

        JPanel east = new JPanel(new BorderLayout());
        east.add(form, BorderLayout.NORTH);
        add(east, BorderLayout.EAST);

        // ---- actions ----
        btnAdd.addActionListener(e -> addClient());
        btnUpdate.addActionListener(e -> updateClient());
        btnDelete.addActionListener(e -> deleteClient());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> loadTable(txtSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadTable(""); });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });
        btnDelete.setEnabled(isAdmin);
        if (!isAdmin) btnDelete.setToolTipText("Admin only");
        loadTable("");
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        p.add(field, g);
    }

    private void loadTable(String text) {
        try {
            model.setRowCount(0);
            List<Client> list = dao.search(text);
            for (Client c : list) {
                model.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), c.getAddress()});
            }
        } catch (SQLException ex) {
            showError("Could not load clients: " + ex.getMessage());
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) model.getValueAt(row, 0);
        txtName.setText(String.valueOf(model.getValueAt(row, 1)));
        Object phone = model.getValueAt(row, 2);
        txtPhone.setText(phone == null ? "" : phone.toString());
        Object addr = model.getValueAt(row, 3);
        txtAddress.setText(addr == null ? "" : addr.toString());
    }

    // Reads the form, validates it, returns null if something is wrong
    private Client readForm() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) { showWarning("Name cannot be empty."); return null; }
        if (!phone.matches("\\d{10}")) { showWarning("Phone must be exactly 10 digits."); return null; }

        Client c = new Client();
        c.setName(name);
        c.setPhone(phone);
        c.setAddress(txtAddress.getText().trim());
        return c;
    }

    private void addClient() {
        Client c = readForm();
        if (c == null) return;
        try {
            dao.add(c);
            loadTable("");
            clearForm();
            JOptionPane.showMessageDialog(this, "Client added.");
        } catch (SQLException ex) {
            showError("Could not add: " + ex.getMessage());
        }
    }

    private void updateClient() {
        if (selectedId < 0) { showWarning("Select a client from the table first."); return; }
        Client c = readForm();
        if (c == null) return;
        c.setId(selectedId);
        try {
            dao.update(c);
            loadTable("");
            clearForm();
            JOptionPane.showMessageDialog(this, "Client updated.");
        } catch (SQLException ex) {
            showError("Could not update: " + ex.getMessage());
        }
    }

    private void deleteClient() {
        if (selectedId < 0) { showWarning("Select a client from the table first."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete this client?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(selectedId);
            loadTable("");
            clearForm();
        } catch (SQLException ex) {
            // A client who already has events can't be removed (foreign key)
            if (ex.getMessage().toLowerCase().contains("foreign key"))
                showError("This client has events booked, so it can't be deleted.");
            else
                showError("Could not delete: " + ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        table.clearSelection();
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Check input", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}