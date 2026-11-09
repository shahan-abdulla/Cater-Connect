// Put in: com.catering.ui   (replaces the old StaffPanel.java)
package com.catering.ui;

import com.catering.dao.CredentialDAO;
import com.catering.dao.StaffDAO;
import com.catering.dao.UserDAO;
import com.catering.model.Staff;
import com.catering.service.StaffService;
import com.catering.service.StaffService.Credentials;
import com.catering.util.PasswordGenerator;
import com.catering.util.PasswordVault;
import org.mindrot.jbcrypt.BCrypt;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StaffPanel extends JPanel {

    private final StaffDAO dao = new StaffDAO();
    private final StaffService staffService = new StaffService();
    private final CredentialDAO credentialDAO = new CredentialDAO();
    private final boolean isAdmin;

    private final JTextField txtName = new JTextField(15);
    private final JTextField txtPhone = new JTextField(15);
    private final JTextField txtAddress = new JTextField(15);
    private final JTextField txtAge = new JTextField(15);
    private final JComboBox<String> cmbRole =
            new JComboBox<>(new String[]{"SERVER", "HELPER", "CAPTAIN"});
    private final JTextField txtWage = new JTextField(15);
    private final JTextField txtSearch = new JTextField(15);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Phone", "Address", "Age", "Role", "Daily Wage"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private int selectedId = -1;

    public StaffPanel(boolean isAdmin) {
        this.isAdmin = isAdmin;
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
        form.setBorder(BorderFactory.createTitledBorder("Staff details"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        addRow(form, g, 0, "Name:", txtName);
        addRow(form, g, 1, "Phone:", txtPhone);
        addRow(form, g, 2, "Address:", txtAddress);
        addRow(form, g, 3, "Age:", txtAge);
        addRow(form, g, 4, "Role:", cmbRole);
        addRow(form, g, 5, "Daily wage:", txtWage);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(buttons, g);

        // For workers added before auto-login existed
        JButton btnLogin = new JButton("Create Login");
        btnLogin.setToolTipText("Create a login for a worker who does not have one yet");
        g.gridy = 7;
        form.add(btnLogin, g);

        // Shows the saved password, with Send on WhatsApp
        JButton btnViewLogin = new JButton("View Login / Send");
        btnViewLogin.setToolTipText("See this worker's username and password and send it on WhatsApp");
        g.gridy = 8;
        form.add(btnViewLogin, g);

        JPanel east = new JPanel(new BorderLayout());
        east.add(form, BorderLayout.NORTH);
        add(east, BorderLayout.EAST);

        // ---- actions ----
        btnAdd.addActionListener(e -> addStaff());
        btnUpdate.addActionListener(e -> updateStaff());
        btnDelete.addActionListener(e -> deleteStaff());
        btnClear.addActionListener(e -> clearForm());
        btnLogin.addActionListener(e -> createLogin());
        btnViewLogin.addActionListener(e -> viewLogin());
        btnSearch.addActionListener(e -> loadTable(txtSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadTable(""); });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });

        // Managers cannot delete staff or handle logins
        btnDelete.setEnabled(isAdmin);
        btnLogin.setEnabled(isAdmin);
        btnViewLogin.setEnabled(isAdmin);
        if (!isAdmin) {
            btnDelete.setToolTipText("Admin only");
            btnLogin.setToolTipText("Admin only");
            btnViewLogin.setToolTipText("Admin only");
        }

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
            List<Staff> list = dao.search(text);
            for (Staff s : list) {
                model.addRow(new Object[]{s.getId(), s.getName(), s.getPhone(),
                        s.getAddress(), s.getAge(), s.getRole(), s.getDailyWage()});
            }
        } catch (SQLException ex) {
            showError("Could not load staff: " + ex.getMessage());
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) model.getValueAt(row, 0);
        txtName.setText(String.valueOf(model.getValueAt(row, 1)));
        txtPhone.setText(String.valueOf(model.getValueAt(row, 2)));
        Object addr = model.getValueAt(row, 3);
        txtAddress.setText(addr == null ? "" : addr.toString());
        txtAge.setText(String.valueOf(model.getValueAt(row, 4)));
        cmbRole.setSelectedItem(String.valueOf(model.getValueAt(row, 5)));
        txtWage.setText(String.valueOf(model.getValueAt(row, 6)));
    }

    // Reads the form, validates it, returns null if something is wrong
    private Staff readForm() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String ageText = txtAge.getText().trim();
        String wageText = txtWage.getText().trim();

        if (name.isEmpty()) { showWarning("Name cannot be empty."); return null; }
        if (!phone.matches("\\d{10}")) { showWarning("Phone must be exactly 10 digits."); return null; }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age < 16 || age > 70) { showWarning("Age must be between 16 and 70."); return null; }
        } catch (NumberFormatException e) {
            showWarning("Age must be a number."); return null;
        }

        double wage;
        try {
            wage = Double.parseDouble(wageText);
            if (wage <= 0) { showWarning("Daily wage must be more than 0."); return null; }
        } catch (NumberFormatException e) {
            showWarning("Daily wage must be a number."); return null;
        }

        Staff s = new Staff();
        s.setName(name);
        s.setPhone(phone);
        s.setAddress(txtAddress.getText().trim());
        s.setAge(age);
        s.setRole((String) cmbRole.getSelectedItem());
        s.setDailyWage(wage);
        return s;
    }

    private void addStaff() {
        Staff s = readForm();
        if (s == null) return;
        try {
            if (isAdmin) {
                // Admin: add the worker AND create a login with an automatic password
                Credentials cred = staffService.addStaffWithLogin(s);
                loadTable("");
                clearForm();
                CredentialsDialog.show(this, "Worker Added", cred);
            } else {
                // Manager: add the worker only
                dao.add(s);
                loadTable("");
                clearForm();
                JOptionPane.showMessageDialog(this, "Staff added.");
            }
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate")) showError("This phone number is already registered.");
            else showError("Could not add: " + ex.getMessage());
        }
    }

    private void updateStaff() {
        if (selectedId < 0) { showWarning("Select a staff member from the table first."); return; }
        Staff s = readForm();
        if (s == null) return;
        s.setId(selectedId);
        try {
            dao.update(s);
            loadTable("");
            clearForm();
            JOptionPane.showMessageDialog(this, "Staff updated.");
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate")) showError("This phone number is already registered.");
            else showError("Could not update: " + ex.getMessage());
        }
    }

    private void deleteStaff() {
        if (selectedId < 0) { showWarning("Select a staff member from the table first."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Remove this staff member?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            dao.deactivate(selectedId);
            loadTable("");
            clearForm();
        } catch (SQLException ex) {
            showError("Could not delete: " + ex.getMessage());
        }
    }

    // The phone number of the selected table row (not the form, which may be edited)
    private String selectedPhone() {
        int row = table.getSelectedRow();
        return row < 0 ? "" : String.valueOf(model.getValueAt(row, 2));
    }

    // For older workers who have no login. Username = phone, password is generated.
    private void createLogin() {
        if (selectedId < 0) { showWarning("Select a staff member from the table first."); return; }
        String username = selectedPhone();
        String pw = PasswordGenerator.generate(8);
        try {
            new UserDAO().createStaffLogin(selectedId, username, pw);
            credentialDAO.saveEncrypted(selectedId, PasswordVault.encrypt(pw));
            CredentialsDialog.show(this, "Login Created", new Credentials(username, pw));
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate")) showError("This staff member already has a login.");
            else showError("Could not create login: " + ex.getMessage());
        }
    }

    // Shows the saved password so the admin can see it again or send it on WhatsApp
    private void viewLogin() {
        if (selectedId < 0) { showWarning("Select a staff member from the table first."); return; }
        String username = selectedPhone();
        try {
            if (!credentialDAO.hasLogin(selectedId)) {
                showWarning("This worker has no login yet. Use Create Login.");
                return;
            }
            String enc = credentialDAO.getEncrypted(selectedId);
            String pw;
            if (enc == null) {
                // Login was made before saved passwords existed
                int ok = JOptionPane.showConfirmDialog(this,
                        "This login's password was not saved, so it cannot be shown.\n"
                      + "Generate a new password? The old one will stop working.",
                        "Password not saved", JOptionPane.YES_NO_OPTION);
                if (ok != JOptionPane.YES_OPTION) return;
                pw = PasswordGenerator.generate(8);
                credentialDAO.resetPassword(selectedId,
                        BCrypt.hashpw(pw, BCrypt.gensalt()), PasswordVault.encrypt(pw));
            } else {
                pw = PasswordVault.decrypt(enc);
            }
            CredentialsDialog.show(this, "Worker Login", new Credentials(username, pw));
        } catch (SQLException ex) {
            showError("Could not read the login: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtAge.setText("");
        txtWage.setText("");
        cmbRole.setSelectedIndex(0);
        table.clearSelection();
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Check input", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}