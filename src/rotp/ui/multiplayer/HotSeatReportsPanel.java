package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.*;
import rotp.multiplayer.hotseat.HotSeatReport;
import rotp.ui.BasePanel;

public final class HotSeatReportsPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final JButton next;
    public HotSeatReportsPanel(String playerName, List<HotSeatReport> reports, Runnable done) {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        setBackground(new Color(25, 30, 40));
        JLabel heading = new JLabel(text("HOTSEAT_REPORTS", playerName));
        heading.setForeground(Color.WHITE);
        heading.setFont(heading.getFont().deriveFont(24f));
        add(heading, BorderLayout.NORTH);
        JTextArea contents = new JTextArea();
        contents.setEditable(false);
        contents.setLineWrap(true);
        contents.setWrapStyleWord(true);
        contents.setFont(contents.getFont().deriveFont(18f));
        for (var report : reports)
            contents.append(report.title() + "\n" + String.join("\n", report.lines()) + "\n\n");
        contents.setCaretPosition(0);
        add(new JScrollPane(contents), BorderLayout.CENTER);
        next = new JButton(text("HOTSEAT_CONTINUE"));
        next.addActionListener(e -> { next.setEnabled(false); done.run(); });
        add(next, BorderLayout.SOUTH);
    }
    public void acknowledge() { if (next.isEnabled()) next.doClick(0); }
}
