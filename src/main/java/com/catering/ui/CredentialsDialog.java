// Put in: com.catering.ui   (replaces the old CredentialsDialog.java)
package com.catering.ui;

import com.catering.service.StaffService.Credentials;
import com.catering.util.WhatsAppSender;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class CredentialsDialog {

    public static void show(Component parent, String title, Credentials cred) {
        JTextArea area = new JTextArea(cred.asMessage());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Worker login details"), BorderLayout.NORTH);
        panel.add(area, BorderLayout.CENTER);

        String[] options = {"Send on WhatsApp", "Copy & Close", "Close"};
        int choice = JOptionPane.showOptionDialog(parent, panel, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            String msg = "Hello! Your login for the catering staff app:\n\n"
                    + cred.asMessage()
                    + "\n\nPlease keep this private.";
            WhatsAppSender.send(parent, cred.username, msg);
        } else if (choice == 1) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(cred.asMessage()), null);
        }
    }
}