// Put in: com.catering.ui   (replaces DashboardFrame.java)
package com.catering.ui;

import com.catering.model.User;
import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    public DashboardFrame(User user) {
        setTitle("Cater Connect - " + user.getUsername() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        boolean isAdmin = user.getRole().equals("ADMIN");

        // ---- white header: logo left, user chip + logout right ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setBorder(Theme.headerBorder());

        JLabel logo = new JLabel("<html><span style='color:#263445'>CATER</span> "
                + "<span style='color:#1E8E85'>CONNECT</span></html>");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(logo, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.putClientProperty("FlatLaf.style", Theme.BTN_OUTLINE);
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.add(Theme.userChip(user.getUsername(), user.getRole()));
        right.add(btnLogout);
        header.add(right, BorderLayout.EAST);

        // ---- pages (shown one at a time) ----
        String[] names = {"Staff", "Clients", "Events", "Assignments",
                "Ready Status", "Attendance", "Payroll", "Reports"};
        JComponent[] pages = {
                new StaffPanel(isAdmin), new ClientPanel(isAdmin), new EventPanel(isAdmin),
                new AssignmentPanel(), new ReadyStatusPanel(), new AttendancePanel(),
                new PayrollPanel(isAdmin), new ReportPanel()
        };

        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);
        cards.setBackground(Theme.BG);

        // ---- pill navigation ----
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nav.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            cards.add(pages[i], name);

            JToggleButton pill = new JToggleButton(name);
            pill.putClientProperty("JButton.buttonType", "roundRect");
            pill.putClientProperty("FlatLaf.style",
                    "borderColor: #D5DEE7; font: bold; hoverBackground: #EAF3F2");
            pill.setMargin(new Insets(6, 16, 6, 16));
            pill.setFocusable(false);
            pill.addActionListener(e -> cardLayout.show(cards, name));
            group.add(pill);
            nav.add(pill);
            if (i == 0) pill.setSelected(true);
        }
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(Theme.BG);
        navBar.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        navBar.add(nav, BorderLayout.WEST);

        JPanel top = new JPanel(new BorderLayout());
        top.add(header, BorderLayout.NORTH);
        top.add(navBar, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Theme.BG);
        body.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
        cards.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        body.add(cards, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // colour all buttons inside the pages, by their text
        UiStyle.styleButtons(cards);

        // alert the admin/manager when a worker reports they cannot attend
        UnavailableAlert.start(this);
    }
}