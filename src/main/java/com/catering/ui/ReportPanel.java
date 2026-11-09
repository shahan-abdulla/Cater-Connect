package com.catering.ui;

import com.catering.dao.ReportDAO;
import com.catering.model.ReportResult;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class ReportPanel extends JPanel {

    private final ReportDAO dao = new ReportDAO();

    private final JComboBox<String> cmbReport = new JComboBox<>(new String[]{
            "Staff-wise earnings", "Event-wise staffing", "Pending payments"});
    private final JLabel lblInfo = new JLabel(" ");

    private final DefaultTableModel model = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Report:"));
        top.add(cmbReport);
        JButton btnRun = new JButton("Show");
        JButton btnExport = new JButton("Export CSV");
        top.add(btnRun);
        top.add(btnExport);

        JPanel north = new JPanel(new BorderLayout());
        north.add(top, BorderLayout.NORTH);
        north.add(lblInfo, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRun.addActionListener(e -> showReport());
        cmbReport.addActionListener(e -> showReport());
        btnExport.addActionListener(e -> exportCsv());

        showReport();
    }

    private void showReport() {
        try {
            ReportResult r;
            switch (cmbReport.getSelectedIndex()) {
                case 1:  r = dao.eventStaffing(); break;
                case 2:  r = dao.pendingPayments(); break;
                default: r = dao.staffEarnings();
            }
            model.setRowCount(0);
            model.setColumnIdentifiers(r.getHeaders().toArray());
            for (Object[] row : r.getRows()) model.addRow(row);
            lblInfo.setText("  " + r.getRows().size() + " row(s)");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load report: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Nothing to export.", "Check", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        String name = String.valueOf(cmbReport.getSelectedItem()).toLowerCase().replace(' ', '_') + ".csv";
        fc.setSelectedFile(new File(name));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0) sb.append(',');
                sb.append(csv(model.getColumnName(c)));
            }
            out.println(sb);
            for (int r = 0; r < model.getRowCount(); r++) {
                sb.setLength(0);
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0) sb.append(',');
                    Object v = model.getValueAt(r, c);
                    sb.append(csv(v == null ? "" : v.toString()));
                }
                out.println(sb);
            }
            JOptionPane.showMessageDialog(this, "Saved to " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Wrap in quotes and double any quotes inside, so commas in names don't break columns
    private String csv(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}