package com.catering.ui;

import com.catering.dao.PaymentDAO;
import com.catering.model.Payment;
import com.catering.model.PayrollRow;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PayrollPanel extends JPanel {

    private final PaymentDAO dao = new PaymentDAO();

    private final JLabel lblTotal = new JLabel(" ");

    private final DefaultTableModel pendingModel = new DefaultTableModel(
            new String[]{"Staff ID", "Name", "Phone", "Unpaid shifts", "Amount owed"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable pendingTable = new JTable(pendingModel);

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Pay ID", "Staff", "Amount", "Date", "Note"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable historyTable = new JTable(historyModel);

    private List<PayrollRow> pendingList = new ArrayList<>();

    public PayrollPanel(boolean isAdmin){
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- top: pending wages ----
        JPanel pendingPanel = new JPanel(new BorderLayout(5, 5));
        pendingPanel.setBorder(BorderFactory.createTitledBorder("Pending wages"));
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingTable.getColumnModel().getColumn(0).setMaxWidth(70);
        pendingPanel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);

        JPanel pendingBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnPay = new JButton("Pay Selected (all pending)");
        JButton btnRefresh = new JButton("Refresh");
        pendingBottom.add(btnPay);
        pendingBottom.add(btnRefresh);
        pendingBottom.add(lblTotal);
        pendingPanel.add(pendingBottom, BorderLayout.SOUTH);

        // ---- bottom: payment history ----
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBorder(BorderFactory.createTitledBorder("Payment history"));
        historyTable.getColumnModel().getColumn(0).setMaxWidth(60);
        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JPanel historyBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnShowAll = new JButton("Show All History");
        historyBottom.add(new JLabel("Select a person above to see only their payments."));
        historyBottom.add(btnShowAll);
        historyPanel.add(historyBottom, BorderLayout.SOUTH);

        JPanel centre = new JPanel(new GridLayout(2, 1, 10, 10));
        centre.add(pendingPanel);
        centre.add(historyPanel);
        add(centre, BorderLayout.CENTER);

        // ---- actions ----
        btnPay.addActionListener(e -> paySelected());
        btnRefresh.addActionListener(e -> reload());
        btnShowAll.addActionListener(e -> { pendingTable.clearSelection(); loadHistory(0); });

        pendingTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = pendingTable.getSelectedRow();
            if (row >= 0) loadHistory(pendingList.get(row).getStaffId());
        });
        btnPay.setEnabled(isAdmin);
        if (!isAdmin) btnPay.setToolTipText("Admin only");
        reload();
    }

    private void reload() {
        loadPending();
        loadHistory(0);
    }

    private void loadPending() {
        try {
            pendingModel.setRowCount(0);
            pendingList = dao.findPending();
            double total = 0;
            for (PayrollRow r : pendingList) {
                pendingModel.addRow(new Object[]{r.getStaffId(), r.getName(), r.getPhone(),
                        r.getShifts(), r.getPendingAmount()});
                total += r.getPendingAmount();
            }
            lblTotal.setText("   Total owed to everyone: " + total);
        } catch (SQLException ex) {
            showError("Could not load pending wages: " + ex.getMessage());
        }
    }

    private void loadHistory(int staffId) {
        try {
            historyModel.setRowCount(0);
            for (Payment p : dao.findHistory(staffId)) {
                historyModel.addRow(new Object[]{p.getId(), p.getStaffName(), p.getAmount(),
                        p.getPayDate(), p.getNote() == null ? "" : p.getNote()});
            }
        } catch (SQLException ex) {
            showError("Could not load payment history: " + ex.getMessage());
        }
    }

    private void paySelected() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) { showWarning("Select a person in the Pending wages table first."); return; }
        PayrollRow r = pendingList.get(row);

        int ok = JOptionPane.showConfirmDialog(this,
                "Pay " + r.getName() + " an amount of " + r.getPendingAmount()
                        + " for " + r.getShifts() + " shift(s)?",
                "Confirm payment", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        String note = JOptionPane.showInputDialog(this, "Note (optional, e.g. Cash / UPI):");
        if (note == null) note = "";   // pressing Cancel just means no note

        try {
            double paid = dao.payAllPending(r.getStaffId(), note.trim());
            if (paid <= 0) showWarning("Nothing was pending for this person.");
            else JOptionPane.showMessageDialog(this, "Payment of " + paid + " recorded.");
        } catch (SQLException ex) {
            showError("Payment failed, nothing was saved: " + ex.getMessage());
        }
        reload();
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Check", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}