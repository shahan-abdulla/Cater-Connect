// Put in: com.catering   (replaces Main.java)
package com.catering;

import com.catering.ui.LoginFrame;
import com.catering.ui.Theme;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}