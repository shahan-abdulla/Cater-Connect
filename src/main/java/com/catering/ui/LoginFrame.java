// Put in: com.catering.ui   (replaces LoginFrame.java)
package com.catering.ui;

import com.catering.dao.UserDAO;
import com.catering.model.User;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final JTextField txtUser = new JTextField(20);
    private final JPasswordField txtPass = new JPasswordField(20);

    public LoginFrame() {
        setTitle("Cater Connect - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Theme.CREAM);

        txtUser.putClientProperty("JTextField.placeholderText", "Username or phone number");
        txtPass.putClientProperty("JTextField.placeholderText", "Password");
        txtPass.putClientProperty("JPasswordField.showRevealButton", true);
        txtUser.setPreferredSize(new Dimension(290, 42));
        txtPass.setPreferredSize(new Dimension(290, 42));
        txtUser.setFont(txtUser.getFont().deriveFont(15f));
        txtPass.setFont(txtPass.getFont().deriveFont(15f));

        // ---- white card with a soft shadow ----
        JPanel card = new ShadowPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        JLabel logo = new JLabel("<html><span style='color:#263445'>CATER</span> "
                + "<span style='color:#1E8E85'>CONNECT</span></html>", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        g.gridy = row++;
        g.insets = new Insets(0, 0, 0, 0);
        card.add(logo, g);

        JLabel sub = new JLabel("Catering staff management", SwingConstants.CENTER);
        sub.setForeground(Theme.MUTED);
        g.gridy = row++;
        g.insets = new Insets(4, 0, 30, 0);
        card.add(sub, g);

        g.gridy = row++;
        g.insets = new Insets(0, 0, 6, 0);
        card.add(smallLabel("USERNAME"), g);
        g.gridy = row++;
        g.insets = new Insets(0, 0, 18, 0);
        card.add(txtUser, g);

        g.gridy = row++;
        g.insets = new Insets(0, 0, 6, 0);
        card.add(smallLabel("PASSWORD"), g);
        g.gridy = row++;
        g.insets = new Insets(0, 0, 24, 0);
        card.add(txtPass, g);

        JButton btnLogin = new JButton("LOGIN");
        btnLogin.putClientProperty("FlatLaf.style", Theme.BTN_PRIMARY);
        btnLogin.setFont(btnLogin.getFont().deriveFont(Font.BOLD, 15f));
        btnLogin.setPreferredSize(new Dimension(290, 46));
        g.gridy = row++;
        g.insets = new Insets(0, 0, 0, 0);
        card.add(btnLogin, g);

        JLabel hint = new JLabel("Workers: sign in with your phone number", SwingConstants.CENTER);
        hint.setForeground(Theme.MUTED);
        hint.setFont(hint.getFont().deriveFont(12f));
        g.gridy = row++;
        g.insets = new Insets(18, 0, 0, 0);
        card.add(hint, g);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(Theme.CREAM);
        wrap.add(card);
        add(wrap);

        btnLogin.addActionListener(e -> doLogin());
        txtPass.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(btnLogin);
    }

    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Theme.MUTED);
        return l;
    }

    private void doLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = new UserDAO().login(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Wrong username or password.",
                        "Login failed", JOptionPane.ERROR_MESSAGE);
                txtPass.setText("");
            } else {
                // Workers get their own small window; admin and manager get the dashboard
                if (user.getRole().equals("STAFF")) {
                    new StaffPortalFrame(user).setVisible(true);
                } else {
                    new DashboardFrame(user).setVisible(true);
                }
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** White rounded card with a soft shadow. */
    private static class ShadowPanel extends JPanel {
        ShadowPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), m = 10, arc = 26;

            for (int k = 1; k <= m; k++) {                       // soft shadow
                g2.setColor(new Color(20, 50, 80, 6));
                g2.fillRoundRect(m - k, m - k + 3, w - 2 * (m - k), h - 2 * (m - k) - 3, arc + k, arc + k);
            }
            g2.setColor(Theme.SURFACE);                          // card body
            g2.fillRoundRect(m, m, w - 2 * m, h - 2 * m - 3, arc, arc);
            g2.setColor(Theme.LINE);                             // hairline edge
            g2.drawRoundRect(m, m, w - 2 * m - 1, h - 2 * m - 4, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}