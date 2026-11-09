// Put in: com.catering.ui   (replaces the previous UnavailableAlert.java)
package com.catering.ui;

import com.catering.dao.UnavailabilityDAO;
import com.catering.dao.UnavailabilityDAO.EventStats;
import com.catering.dao.UnavailabilityDAO.Report;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks the database every 30 seconds. When workers report they cannot attend,
 * shows ONE popup with, per event: how many are ready, checked in and unable to
 * attend, how many replacements are needed, and each new report with its reason.
 */
public class UnavailableAlert {

    private static final int INTERVAL_MS = 30_000;
    private static boolean showing = false;

    private static class Result {
        final List<Report> reports;
        final Map<Integer, EventStats> stats;

        Result(List<Report> reports, Map<Integer, EventStats> stats) {
            this.reports = reports;
            this.stats = stats;
        }
    }

    public static void start(JFrame frame) {
        UnavailabilityDAO dao = new UnavailabilityDAO();
        javax.swing.Timer timer = new javax.swing.Timer(INTERVAL_MS, null);
        timer.setInitialDelay(1500);
        timer.addActionListener(e -> {
            if (!frame.isDisplayable()) { timer.stop(); return; }
            if (showing) return;
            check(frame, dao);
        });
        timer.start();
    }

    private static void check(JFrame frame, UnavailabilityDAO dao) {
        new SwingWorker<Result, Void>() {
            @Override protected Result doInBackground() throws Exception {
                List<Report> reports = dao.getUnseen();
                Map<Integer, EventStats> stats = new LinkedHashMap<>();
                for (Report r : reports) {
                    if (!stats.containsKey(r.eventId)) {
                        stats.put(r.eventId, dao.getEventStats(r.eventId));
                    }
                }
                return new Result(reports, stats);
            }

            @Override protected void done() {
                try {
                    Result res = get();
                    List<Report> reports = res.reports;
                    if (reports.isEmpty() || !frame.isDisplayable()) return;

                    showing = true;

                    // Group the new reports by event
                    Map<Integer, List<Report>> byEvent = new LinkedHashMap<>();
                    for (Report r : reports) {
                        byEvent.computeIfAbsent(r.eventId, k -> new ArrayList<>()).add(r);
                    }

                    int totReady = 0, totChecked = 0, totCannot = 0;
                    for (EventStats st : res.stats.values()) {
                        totReady += st.ready;
                        totChecked += st.checkedIn;
                        totCannot += st.unavailable;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("NEW: ").append(reports.size())
                      .append(reports.size() == 1 ? " worker has" : " workers have")
                      .append(" marked cannot attend.\n");
                    sb.append("Overall for ").append(byEvent.size())
                      .append(byEvent.size() == 1 ? " event: " : " events: ")
                      .append(totReady).append(" ready, ")
                      .append(totChecked).append(" checked in, ")
                      .append(totCannot).append(" cannot attend.\n\n");

                    for (Map.Entry<Integer, List<Report>> en : byEvent.entrySet()) {
                        List<Report> list = en.getValue();
                        Report first = list.get(0);
                        EventStats st = res.stats.get(en.getKey());

                        sb.append(first.eventName).append(" (").append(first.eventDate).append(")\n");
                        sb.append("   Needed: ").append(st.staffRequired)
                          .append("   Assigned: ").append(st.assigned)
                          .append("   Ready: ").append(st.ready)
                          .append("   Checked in: ").append(st.checkedIn)
                          .append("   Cannot attend: ").append(st.unavailable).append('\n');
                        sb.append("   Replacements needed: ").append(st.replacementsNeeded()).append('\n');
                        sb.append("   New reports:\n");
                        for (Report r : list) {
                            sb.append("      • ").append(r.staffName)
                              .append(" (").append(r.phone).append(")")
                              .append("  -  ")
                              .append(r.reason == null || r.reason.isBlank()
                                      ? "no reason given" : r.reason)
                              .append('\n');
                        }
                        sb.append('\n');
                    }
                    sb.append("Open Assignments to remove them and assign replacements.");

                    JTextArea area = new JTextArea(sb.toString());
                    area.setEditable(false);
                    area.setLineWrap(true);
                    area.setWrapStyleWord(true);
                    area.setCaretPosition(0);
                    JScrollPane scroll = new JScrollPane(area);
                    scroll.setPreferredSize(new Dimension(620, 360));

                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(frame, scroll,
                            "Workers cannot attend", JOptionPane.WARNING_MESSAGE);

                    dao.markSeen(reports);
                } catch (Exception ignored) {
                    // Never interrupt the manager with a background check failure
                } finally {
                    showing = false;
                }
            }
        }.execute();
    }
}