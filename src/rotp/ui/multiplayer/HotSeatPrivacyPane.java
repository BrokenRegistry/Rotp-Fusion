package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import rotp.ui.BasePanel;

/** Opaque screen with a single-use acknowledgement, never a transparent overlay. */
public final class HotSeatPrivacyPane extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final JButton proceed;
    private boolean accepted;

    public HotSeatPrivacyPane(String playerName, Runnable confirm) {
        setOpaque(true);
        setLayout(new GridBagLayout());
        JPanel content = new JPanel(new BorderLayout(20, 28));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        content.add(HotSeatStyle.title(text("HOTSEAT_HANDOFF", playerName)), BorderLayout.NORTH);
        content.add(HotSeatStyle.body(text("HOTSEAT_HANDOFF_DETAIL"), 20), BorderLayout.CENTER);
        proceed = new JButton(text("HOTSEAT_CONTINUE")) {
            private static final long serialVersionUID = 1L;
            @Override protected void processMouseEvent(MouseEvent event) {
                if (event.getClickCount() > 1) { event.consume(); return; }
                super.processMouseEvent(event);
            }
        };
        HotSeatStyle.button(proceed, 24);
        proceed.addActionListener(event -> {
            if (accepted) return;
            accepted = true;
            proceed.setEnabled(false);
            confirm.run();
        });
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.add(proceed);
        content.add(footer, BorderLayout.SOUTH);
        add(content);
    }

    @Override public void paintComponent(java.awt.Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
    public void acknowledge() { proceed.doClick(0); }
}
