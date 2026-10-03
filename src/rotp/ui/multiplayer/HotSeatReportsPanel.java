package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.List;
import javax.swing.*;
import rotp.multiplayer.hotseat.HotSeatReport;
import rotp.ui.BasePanel;

/** Reports the game has no screen of its own for, such as auto-resolved battles. */
public final class HotSeatReportsPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final JButton next;
    public HotSeatReportsPanel(String playerName, List<HotSeatReport> reports, Runnable done) {
        setLayout(new BorderLayout(15, 25));
        setBorder(BorderFactory.createEmptyBorder(50, 120, 40, 120));
        add(HotSeatStyle.title(text("HOTSEAT_REPORTS", playerName)), BorderLayout.NORTH);
        JTextArea contents = new JTextArea();
        contents.setEditable(false);
        contents.setLineWrap(true);
        contents.setWrapStyleWord(true);
        HotSeatStyle.body(contents, 22);
        for (var report : reports)
            contents.append(report.title() + "\n    " + String.join("\n    ", report.lines()) + "\n\n");
        contents.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(contents);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        next = HotSeatStyle.button(new JButton(text("HOTSEAT_CONTINUE")), 22);
        next.addActionListener(e -> { next.setEnabled(false); done.run(); });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(next);
        add(footer, BorderLayout.SOUTH);
    }
    @Override public void paintComponent(Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
    public void acknowledge() { if (next.isEnabled()) next.doClick(0); }
}
