package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.Color;
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
        setBackground(new Color(20, 25, 35));
        JLabel title = new JLabel(text("HOTSEAT_RESULT_" + outcome.cause()), JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(28f));
        add(title, BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(0, 1, 15, 15));
        rows.setOpaque(false);
        for (var human : setup.humans()) {
            JLabel row = new JLabel(human.displayName() + ": "
                    + text("HOTSEAT_STANDING_" + outcome.resultsByPlayer().get(human.playerId())), JLabel.CENTER);
            row.setForeground(Color.WHITE);
            row.setFont(row.getFont().deriveFont(22f));
            rows.add(row);
        }
        add(rows, BorderLayout.CENTER);
        JButton back = new JButton(text("HOTSEAT_MENU"));
        back.addActionListener(e -> { back.setEnabled(false); menu.run(); });
        add(back, BorderLayout.SOUTH);
    }
}
