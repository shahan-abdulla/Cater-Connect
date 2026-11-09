package com.catering.util;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ContactHelper {

    private static final String COUNTRY_CODE = "91";   // India

    // Opens the PC's calling app. Returns false if none could be opened.
    public static boolean call(String phone) {
        return open("tel:+" + COUNTRY_CODE + phone);
    }

    // Opens WhatsApp with the message ready to send
    public static boolean whatsApp(String phone, String message) {
        String text = URLEncoder.encode(message, StandardCharsets.UTF_8).replace("+", "%20");
        return open("https://wa.me/" + COUNTRY_CODE + phone + "?text=" + text);
    }

    public static void copy(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private static boolean open(String uri) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) return false;
            Desktop.getDesktop().browse(new URI(uri));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}