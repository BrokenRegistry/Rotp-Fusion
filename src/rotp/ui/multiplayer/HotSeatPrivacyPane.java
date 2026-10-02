package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import rotp.ui.BasePanel;

/** Opaque screen with a single-use acknowledgement, never a transparent overlay. */
public final class HotSeatPrivacyPane extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final JButton proceed;
    private boolean accepted;

    public HotSeatPrivacyPane(String playerName, Runnable confirm) {
        setOpaque(true);
        setBackground(new Color(18, 22, 30));
        setLayout(new GridBagLayout());
        JPanel content = new JPanel(new BorderLayout(20, 28));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        JLabel heading = new JLabel(text("HOTSEAT_HANDOFF", playerName), JLabel.CENTER);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        heading.setForeground(Color.WHITE);
        content.add(heading, BorderLayout.NORTH);
        JLabel description = new JLabel(text("HOTSEAT_HANDOFF_DETAIL"), JLabel.CENTER);
        description.setForeground(new Color(200, 207, 220));
        content.add(description, BorderLayout.CENTER);
        proceed = new JButton(text("HOTSEAT_CONTINUE")) {
            private static final long serialVersionUID = 1L;
            @Override protected void processMouseEvent(MouseEvent event) {
                if (event.getClickCount() > 1) { event.consume(); return; }
                super.processMouseEvent(event);
            }
        };
        proceed.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        proceed.addActionListener(event -> {
            if (accepted) return;
            accepted = true;
            proceed.setEnabled(false);
            confirm.run();
        });
        content.add(proceed, BorderLayout.SOUTH);
        add(content);
    }

    public void acknowledge() { proceed.doClick(0); }
}
