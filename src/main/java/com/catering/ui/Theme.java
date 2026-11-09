// Put in: com.catering.ui   (replaces Theme.java)
package com.catering.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/** All colours and styles in one place. Change ACCENT here to re-colour the whole app. */
public final class Theme {

    private Theme() {}

    // ---- palette (soft light, teal accent) ----
    public static final Color CREAM = new Color(0xFBF6EE);        // login background
    public static final Color BG = new Color(0xF4F7FA);           // app background
    public static final Color SURFACE = new Color(0xFFFFFF);      // cards, header
    public static final Color SURFACE_ALT = new Color(0xF3F7FA);  // table header, strips
    public static final Color LINE = new Color(0xE3EAF0);
    public static final Color ACCENT = new Color(0x1E8E85);       // teal
    public static final Color ACCENT_SOFT = new Color(0xD5EEEB);
    public static final Color TEXT = new Color(0x263445);
    public static final Color MUTED = new Color(0x7C8B9C);
    public static final Color DANGER = new Color(0xE0425F);
    public static final Color WARN = new Color(0xF5BB43);
    public static final Color INFO = new Color(0x3F7FD9);

    // ---- button styles (FlatLaf.style strings) ----
    private static final String DISABLED =
            "; disabledBackground: #E3EAF0; disabledText: #A5B1BF";

    public static final String BTN_PRIMARY =
            "background: #1E8E85; foreground: #FFFFFF; font: bold; "
          + "hoverBackground: #2BA79D; pressedBackground: #17736C" + DISABLED;

    public static final String BTN_DANGER =
            "background: #E0425F; foreground: #FFFFFF; font: bold; "
          + "hoverBackground: #EC6A80; pressedBackground: #C23350" + DISABLED;

    public static final String BTN_WARN =
            "background: #F5BB43; foreground: #3A2A00; font: bold; "
          + "hoverBackground: #F8CB6E; pressedBackground: #D9A22F" + DISABLED;

    public static final String BTN_INFO =
            "background: #3F7FD9; foreground: #FFFFFF; font: bold; "
          + "hoverBackground: #6499E3; pressedBackground: #3068B8" + DISABLED;

    public static final String BTN_OUTLINE =
            "background: #FFFFFF; foreground: #1E8E85; borderColor: #1E8E85; font: bold; "
          + "hoverBackground: #EAF6F4; pressedBackground: #D5EEEB" + DISABLED;

    /** Call once at start-up, before any window is created. */
    public static void install() {
        Map<String, String> extra = new HashMap<>();
        extra.put("@accentColor", "#1E8E85");
        extra.put("@background", "#F4F7FA");
        extra.put("@foreground", "#263445");
        extra.put("@selectionBackground", "#DDF1EE");
        extra.put("@selectionForeground", "#17504B");
        FlatLaf.setGlobalExtraDefaults(extra);

        // shape and fonts
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));

        // default (Enter) button and selected pill (toggle) buttons
        UIManager.put("Button.default.background", ACCENT);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Button.toggleSelectedBackground", ACCENT);
        UIManager.put("Button.toggleSelectedForeground", Color.WHITE);

        // focus and borders
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.focusedBorderColor", ACCENT);
        UIManager.put("Component.borderColor", new Color(0xD5DEE7));

        // tables
        UIManager.put("Table.rowHeight", 34);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.alternateRowColor", new Color(0xF8FAFC));
        UIManager.put("Table.gridColor", new Color(0xEDF1F5));
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.selectionBackground", new Color(0xDDF1EE));
        UIManager.put("Table.selectionForeground", new Color(0x17504B));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("TableHeader.background", SURFACE_ALT);
        UIManager.put("TableHeader.foreground", MUTED);
        UIManager.put("TableHeader.bottomSeparatorColor", LINE);
        UIManager.put("TableHeader.separatorColor", new Color(0, 0, 0, 0));

        // tabs (used if any screen still has a JTabbedPane)
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("TabbedPane.selectedForeground", ACCENT);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("TabbedPane.hoverColor", SURFACE_ALT);

        // scrollbars, titled borders, tooltips
        UIManager.put("ScrollBar.thumb", new Color(0xC9D4DE));
        UIManager.put("ScrollBar.hoverThumbColor", new Color(0xAEBCCB));
        UIManager.put("ScrollBar.pressedThumbColor", ACCENT);
        UIManager.put("TitledBorder.titleColor", ACCENT);
        UIManager.put("ToolTip.background", TEXT);
        UIManager.put("ToolTip.foreground", Color.WHITE);

        FlatLightLaf.setup();
    }

    /** Thin line under the top banner. */
    public static Border headerBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                BorderFactory.createEmptyBorder(12, 20, 12, 20));
    }

    // ---------- small reusable pieces ----------

    /** A rounded "pill" label, like the badges in the reference design. */
    public static JLabel pill(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setForeground(fg);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    /** Round avatar showing the first letter of a name. */
    private static class Avatar extends JComponent {
        private final String initial;

        Avatar(String name) {
            initial = (name == null || name.isEmpty()) ? "?" : name.substring(0, 1).toUpperCase();
            setPreferredSize(new Dimension(34, 34));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(ACCENT_SOFT);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(ACCENT);
            g2.setFont(getFont().deriveFont(Font.BOLD, 15f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Avatar + name + role pill, for the top-right of a window. */
    public static JComponent userChip(String name, String role) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        p.add(new Avatar(name));
        JLabel n = new JLabel(name);
        n.setFont(n.getFont().deriveFont(Font.BOLD, 14f));
        n.setForeground(TEXT);
        p.add(n);
        p.add(pill(role, ACCENT_SOFT, ACCENT));
        return p;
    }

    // ---------- status pills inside tables ----------

    private static Color[] pillColors(String s) {
        if (s.startsWith("READY")) return new Color[]{ACCENT, Color.WHITE};
        if (s.startsWith("PRESENT")) return new Color[]{INFO, Color.WHITE};
        if (s.startsWith("LATE")) return new Color[]{new Color(0xF29A4A), new Color(0x3A1F00)};
        if (s.startsWith("Not")) return new Color[]{WARN, new Color(0x3A2A00)};
        if (s.startsWith("CANNOT") || s.startsWith("UNAVAILABLE") || s.startsWith("ABSENT"))
            return new Color[]{DANGER, Color.WHITE};
        if (s.startsWith("PENDING")) return new Color[]{new Color(0xE3EAF0), new Color(0x4A5B6D)};
        return new Color[]{new Color(0xE3EAF0), TEXT};
    }

    /** Draws a status column (READY, PRESENT, LATE, ABSENT ...) as coloured pills. */
    public static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            private Color rowBg = Color.WHITE;
            private Color pillBg = Color.LIGHT_GRAY;
            private Color pillFg = Color.BLACK;
            private String shown = "";

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                shown = v == null ? "" : v.toString();
                Color alt = UIManager.getColor("Table.alternateRowColor");
                rowBg = sel ? t.getSelectionBackground()
                        : (alt != null && r % 2 == 1 ? alt : t.getBackground());
                Color[] col = pillColors(shown);
                pillBg = col[0];
                pillFg = col[1];
                setText("");
                setBorder(BorderFactory.createEmptyBorder());
                setOpaque(false);
                return this;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(rowBg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (!shown.isEmpty()) {
                    g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                    FontMetrics fm = g2.getFontMetrics();
                    int padX = 12;
                    int h = Math.min(getHeight() - 8, fm.getHeight() + 6);
                    int w = Math.min(getWidth() - 8, fm.stringWidth(shown) + padX * 2);
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    String text = shown;
                    if (fm.stringWidth(text) > w - padX * 2) {          // too long: shorten
                        while (text.length() > 1 && fm.stringWidth(text + "...") > w - padX * 2) {
                            text = text.substring(0, text.length() - 1);
                        }
                        text = text + "...";
                    }

                    g2.setColor(pillBg);
                    g2.fillRoundRect(x, y, w, h, h, h);
                    g2.setColor(pillFg);
                    g2.drawString(text, x + (w - fm.stringWidth(text)) / 2,
                            y + (h - fm.getHeight()) / 2 + fm.getAscent());
                }
                g2.dispose();
            }
        };
    }
}