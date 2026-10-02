package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.*;
import rotp.multiplayer.hotseat.HotSeatSetup;
import rotp.multiplayer.session.MatchOutcome;
import rotp.ui.BasePanel;

/** Public results only; private final reports are acknowledged before this panel. */
public final class HotSeatResultPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    public HotSeatResultPanel(MatchOutcome outcome, HotSeatSetup setup, Runnable menu) {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        add(HotSeatStyle.title(text("HOTSEAT_RESULT_" + outcome.cause())), BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(0, 1, 15, 15));
        rows.setOpaque(false);
        for (var human : setup.humans()) {
            rows.add(HotSeatStyle.body(human.displayName() + ": "
                    + text("HOTSEAT_STANDING_" + outcome.resultsByPlayer().get(human.playerId())), 26));
        }
        add(rows, BorderLayout.CENTER);
        JButton back = HotSeatStyle.button(new JButton(text("HOTSEAT_MENU")), 22);
        back.addActionListener(e -> { back.setEnabled(false); menu.run(); });
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.add(back);
        add(footer, BorderLayout.SOUTH);
    }
    @Override public void paintComponent(java.awt.Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
}
