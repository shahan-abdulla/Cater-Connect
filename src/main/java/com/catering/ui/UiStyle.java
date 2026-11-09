// Put in: com.catering.ui   (replaces UiStyle.java)
package com.catering.ui;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;

/** Colours buttons automatically, based on their text. */
public class UiStyle {

    public static void styleButtons(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                String t = b.getText() == null ? "" : b.getText().trim();
                String style = styleFor(t);
                if (style != null) b.putClientProperty("FlatLaf.style", style);
            }
            if (c instanceof Container) {
                styleButtons((Container) c);
            }
        }
    }

    private static String styleFor(String t) {
        // red: destructive
        if (t.equals("Delete") || t.equals("Cancel Event")
                || t.contains("Remove") || t.equals("Absent")) {
            return Theme.BTN_DANGER;
        }
        // amber
        if (t.equals("Late")) {
            return Theme.BTN_WARN;
        }
        // neon green: main actions
        if (t.equals("Add") || t.equals("Update") || t.startsWith("Assign")
                || t.equals("Present") || t.equals("Mark All Present")
                || t.startsWith("Pay Selected") || t.equals("Mark Event Completed")
                || t.equals("Show") || t.equals("Export CSV")
                || t.equals("Create Login") || t.startsWith("View Login")) {
            return Theme.BTN_PRIMARY;
        }
        // outlined: secondary actions
        if (t.equals("Search") || t.equals("Show All") || t.equals("Refresh")
                || t.equals("Clear") || t.contains("WhatsApp") || t.equals("Call")) {
            return Theme.BTN_OUTLINE;
        }
        return null;   // anything else keeps the theme's default button look
    }
}