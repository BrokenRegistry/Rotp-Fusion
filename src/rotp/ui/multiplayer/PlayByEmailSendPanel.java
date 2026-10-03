package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import javax.swing.*;
import rotp.ui.BasePanel;

/** Shown after a turn file is written for someone else; never reveals the map. */
public final class PlayByEmailSendPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final JButton primary;
    private final JButton secondary;
    private final boolean finalResult;

    /** A turn for one player: return to the menu, or hand over if they are present. */
    public static PlayByEmailSendPanel forPlayer(String playerName, String fileName,
            Runnable toMenu, Runnable playerHere) {
        return new PlayByEmailSendPanel(playerName, fileName, toMenu, playerHere);
    }

    /** The finished match, for everyone; results show on this computer next. */
    public static PlayByEmailSendPanel finalResult(String fileName, Runnable showResults) {
        return new PlayByEmailSendPanel(null, fileName, showResults, null);
    }

    private PlayByEmailSendPanel(String playerName, String fileName, Runnable primaryAction,
            Runnable secondaryAction) {
        finalResult = playerName == null;
        String title = finalResult ? text("PBEM_FINAL_TITLE") : text("PBEM_SEND_TITLE", playerName);
        String body = finalResult ? text("PBEM_FINAL_DETAIL", fileName)
                : text("PBEM_SEND_DETAIL", fileName, playerName);
        String primaryLabel = text(finalResult ? "PBEM_SHOW_RESULTS" : "PBEM_RETURN_MENU");
        String secondaryLabel = finalResult ? null : text("PBEM_HERE", playerName);
        setLayout(new BorderLayout(15, 25));
        setBorder(BorderFactory.createEmptyBorder(80, 120, 60, 120));
        add(HotSeatStyle.title(title), BorderLayout.NORTH);
        JTextArea detail = new JTextArea(body);
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        HotSeatStyle.body(detail, 22);
        add(detail, BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        footer.setOpaque(false);
        primary = HotSeatStyle.button(new JButton(primaryLabel), 22);
        footer.add(primary);
        secondary = secondaryLabel == null ? null : HotSeatStyle.button(new JButton(secondaryLabel), 22);
        if (secondary != null) footer.add(secondary);
        primary.addActionListener(e -> { lockButtons(); primaryAction.run(); });
        if (secondary != null) secondary.addActionListener(e -> { lockButtons(); secondaryAction.run(); });
        add(footer, BorderLayout.SOUTH);
    }
    private void lockButtons() {
        primary.setEnabled(false);
        if (secondary != null) secondary.setEnabled(false);
    }
    @Override public void paintComponent(Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
    public boolean isFinalResult() { return finalResult; }
    /** Return to menu, or show results for the finished match. */
    public void primary() { if (primary.isEnabled()) primary.doClick(0); }
    /** The named player is here and continues on this computer. */
    public void playerIsHere() { if (secondary != null && secondary.isEnabled()) secondary.doClick(0); }
}
