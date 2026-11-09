// Put in: com.catering.util
package com.catering.util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Opens WhatsApp with a ready-to-send message. The admin presses Send. */
public class WhatsAppSender {

    private static final String COUNTRY_CODE = "91";   // India

    public static void send(Component parent, String phone, String message) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 10) digits = COUNTRY_CODE + digits;

        try {
            String text = URLEncoder.encode(message, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            URI uri = new URI("https://wa.me/" + digits + "?text=" + text);

            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
            } else {
                throw new UnsupportedOperationException("No browser available");
            }
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(message), null);
            JOptionPane.showMessageDialog(parent,
                    "Could not open WhatsApp. The message was copied to the clipboard,\n"
                  + "so you can paste it into a chat with " + phone + ".",
                    "WhatsApp", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}